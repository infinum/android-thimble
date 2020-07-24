package com.infinum.thimble

import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.database.MatrixCursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.media.ThumbnailUtils
import android.os.Build
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.provider.MediaStore
import android.util.Size
import android.webkit.MimeTypeMap
import androidx.annotation.RestrictTo
import com.infinum.thimble.ui.utils.FileUtils
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.PriorityQueue
import kotlin.math.min

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class RecorderDocumentsProvider : DocumentsProvider() {

    companion object {

        private val DEFAULT_ROOT_PROJECTION: Array<String> = arrayOf(
            DocumentsContract.Root.COLUMN_ROOT_ID,
            DocumentsContract.Root.COLUMN_MIME_TYPES,
            DocumentsContract.Root.COLUMN_FLAGS,
            DocumentsContract.Root.COLUMN_ICON,
            DocumentsContract.Root.COLUMN_TITLE,
            DocumentsContract.Root.COLUMN_SUMMARY,
            DocumentsContract.Root.COLUMN_DOCUMENT_ID,
            DocumentsContract.Root.COLUMN_AVAILABLE_BYTES
        )
        private val DEFAULT_DOCUMENT_PROJECTION: Array<String> = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
            DocumentsContract.Document.COLUMN_FLAGS,
            DocumentsContract.Document.COLUMN_SIZE
        )

        private const val ROOT = "thimble"

        private const val MAX_LAST_MODIFIED: Int = 5

        private const val JPEG_QUALITY = 90
    }

    private lateinit var baseDir: File

    override fun onCreate(): Boolean {

        baseDir = context?.let {
            FileUtils.createOrUseRoot(it)
        } ?: throw NullPointerException()

        return true
    }

    override fun queryRoots(projection: Array<String>?): Cursor =
        MatrixCursor(resolveRootProjection(projection))
            .apply {
                newRow()
                    .apply {
                        add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT)
                        add(
                            DocumentsContract.Root.COLUMN_FLAGS,
                            DocumentsContract.Root.FLAG_SUPPORTS_CREATE or
                                    DocumentsContract.Root.FLAG_SUPPORTS_RECENTS or
                                    DocumentsContract.Root.FLAG_SUPPORTS_SEARCH
                        )
                        add(
                            DocumentsContract.Root.COLUMN_TITLE,
                            context?.getString(R.string.thimble_name)
                        )
                        add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, documentIdForFile(baseDir))
                        add(DocumentsContract.Root.COLUMN_MIME_TYPES, supportedMimeTypes())
                        add(DocumentsContract.Root.COLUMN_ICON, R.drawable.thimble_ic_logo_provider)
                    }
            }

    override fun queryDocument(documentId: String?, projection: Array<String>?): Cursor =
        MatrixCursor(resolveDocumentProjection(projection))
            .apply {
                includeFile(this, documentId, null)
            }

    override fun queryChildDocuments(
        parentDocumentId: String,
        projection: Array<String>?,
        sortOrder: String?
    ): Cursor =
        MatrixCursor(resolveDocumentProjection(projection))
            .apply {
                val parent: File = fileForDocumentId(parentDocumentId)
                parent.listFiles().orEmpty().forEach { file ->
                    includeFile(this, null, file)
                }
            }

    override fun openDocument(
        documentId: String,
        mode: String?,
        signal: CancellationSignal?
    ): ParcelFileDescriptor =
        ParcelFileDescriptor.open(
            fileForDocumentId(documentId),
            ParcelFileDescriptor.MODE_READ_ONLY
        )

    override fun openDocumentThumbnail(
        documentId: String,
        sizeHint: Point,
        signal: CancellationSignal?
    ): AssetFileDescriptor =
        AssetFileDescriptor(
            ParcelFileDescriptor.open(
                thumbnailFileForDocumentId(fileForDocumentId(documentId), sizeHint),
                ParcelFileDescriptor.MODE_READ_ONLY
            ),
            0,
            AssetFileDescriptor.UNKNOWN_LENGTH
        )

    override fun queryRecentDocuments(rootId: String, projection: Array<String>?): Cursor {
        val result = MatrixCursor(resolveDocumentProjection(projection))

        val parent: File = fileForDocumentId(rootId)

        val lastModifiedFiles = PriorityQueue(
            MAX_LAST_MODIFIED,
            Comparator<File> { i, j ->
                compareValues(i.lastModified(), j.lastModified())
            }
        )

        val pending: MutableList<File> = mutableListOf()
        pending.add(parent)
        while (pending.isNotEmpty()) {
            val file: File = pending.removeAt(0)
            if (file.isDirectory) {
                pending += file.listFiles().orEmpty()
            } else {
                lastModifiedFiles.add(file)
            }
        }

        for (i in 0 until min(MAX_LAST_MODIFIED + 1, lastModifiedFiles.size)) {
            val file: File = lastModifiedFiles.remove()
            includeFile(result, null, file)
        }

        return result
    }

    private fun thumbnailFileForDocumentId(file: File, sizeHint: Point): File {
        val mimeType = typeForFile(file)
        val tempFile: File = context?.cacheDir?.let { File.createTempFile("thumbnail", null, it) }
            ?: throw NullPointerException()
        val size = Size(2 * sizeHint.x, 2 * sizeHint.y)

        val bitmap = if (mimeType.startsWith("image/")) {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(file.absolutePath, options)
            val targetWidth: Int = size.width
            val targetHeight: Int = size.height
            val width = options.outWidth
            val height = options.outHeight
            options.inSampleSize = 1
            if (height > targetHeight || width > targetWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while (halfHeight / options.inSampleSize > targetHeight ||
                    halfWidth / options.inSampleSize > targetWidth
                ) {
                    options.inSampleSize *= 2
                }
            }
            options.inJustDecodeBounds = false

            BitmapFactory.decodeFile(file.absolutePath, options)
        } else if (mimeType.startsWith("video/")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ThumbnailUtils.createVideoThumbnail(file, size, null)
            } else {
                @Suppress("DEPRECATION")
                ThumbnailUtils.createVideoThumbnail(file.absolutePath, MediaStore.Video.Thumbnails.MINI_KIND)
            }
        } else {
            throw NotImplementedError()
        }

        FileOutputStream(tempFile).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it)
        }

        return tempFile
    }

    /**
     * @param projection the requested root column projection
     * @return either the requested root column projection, or the default projection if the
     * requested projection is null.
     */
    private fun resolveRootProjection(projection: Array<String>?): Array<String> =
        projection ?: DEFAULT_ROOT_PROJECTION

    /**
     * @param projection the requested root column projection
     * @return either the requested document column projection, or the default projection if the
     * requested projection is null.
     */
    private fun resolveDocumentProjection(projection: Array<String>?): Array<String> =
        projection ?: DEFAULT_DOCUMENT_PROJECTION

    /**
     * Get the document ID given a File.  The document id must be consistent across time.  Other
     * applications may save the ID and use it to reference documents later.
     *
     * @param file the File whose document ID you want
     * @return the corresponding document ID
     */
    private fun documentIdForFile(file: File): String {
        val rootPath: String = baseDir.path
        val path = when {
            rootPath == file.absolutePath -> ""
            rootPath.endsWith("/") -> file.absolutePath.substring(rootPath.length)
            else -> file.absolutePath.substring(rootPath.length + 1)
        }
        return "$ROOT://$path"
    }

    /**
     * Gets a string of unique MIME data types a directory supports, separated by newlines.  This
     * should not change.
     *
     * @param parent the File for the parent directory
     * @return a string of the unique MIME data types the parent directory supports
     */
    private fun supportedMimeTypes(): String =
        setOf(
            "image/jpeg",
            "video/mp4"
        ).let {
            StringBuilder().apply {
                for (mimeType in it) {
                    append(mimeType).append("\n")
                }
            }.toString()
        }

    /**
     * Translate your custom URI scheme into a File object.
     *
     * @param documentId the document ID representing the desired file
     * @return a File represented by the given document ID
     * @throws java.io.FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    private fun fileForDocumentId(documentId: String): File {
        var target: File = baseDir
        if (documentId == ROOT) {
            return target
        }
        val splitIndex = documentId.indexOf(':', 1)
        return if (splitIndex < 0) {
            throw FileNotFoundException("Missing root for $documentId")
        } else {
            val path = documentId.substring(splitIndex + 1)
            target = File(target, path)
            if (!target.exists()) {
                throw FileNotFoundException("Missing file for $documentId at $target")
            }
            target
        }
    }

    /**
     * Add a representation of a file to a cursor.
     *
     * @param result the cursor to modify
     * @param docId  the document ID representing the desired file (may be null if given file)
     * @param file   the File object representing the desired file (may be null if given docID)
     * @throws java.io.FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    private fun includeFile(result: MatrixCursor, documentId: String?, file: File?) {
        var desiredDocumentId: String? = documentId
        var desiredFile: File? = file

        when {
            desiredDocumentId != null -> desiredFile = fileForDocumentId(desiredDocumentId)
            else -> desiredDocumentId = file?.let { documentIdForFile(it) }
        }

        var flags = DocumentsContract.Document.FLAG_DIR_PREFERS_GRID or
                DocumentsContract.Document.FLAG_DIR_PREFERS_LAST_MODIFIED
        desiredFile?.let {
            val mimeType = typeForFile(it)
            if (mimeType.startsWith("image/") || mimeType.startsWith("video/")) {
                flags = flags or DocumentsContract.Document.FLAG_SUPPORTS_THUMBNAIL
            }
            result.newRow().apply {
                add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, desiredDocumentId)
                add(
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    if (it.isDirectory) it.name.capitalize() else it.name
                )
                add(DocumentsContract.Document.COLUMN_SIZE, it.length())
                add(DocumentsContract.Document.COLUMN_MIME_TYPE, mimeType)
                add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, it.lastModified())
                add(DocumentsContract.Document.COLUMN_FLAGS, flags)
            }
        }
    }

    /**
     * Get a file's MIME type
     *
     * @param file the File object whose type we want
     * @return the MIME type of the file
     */
    private fun typeForFile(file: File): String =
        when {
            file.isDirectory -> DocumentsContract.Document.MIME_TYPE_DIR
            else -> typeForName(file.name)
        }

    /**
     * Get the MIME data type of a document, given its filename.
     *
     * @param name the filename of the document
     * @return the MIME data type of a document
     */
    private fun typeForName(name: String): String {
        val lastDot = name.lastIndexOf('.')
        return when (lastDot >= 0) {
            true -> MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(name.substring(lastDot + 1)) ?: "application/octet-stream"
            false -> "application/octet-stream"
        }
    }
}