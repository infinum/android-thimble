package com.infinum.thimble.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import com.infinum.thimble.models.FileTarget
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

internal object FileUtils {

    private lateinit var context: Context

    fun initialise(context: Context) {
        this.context = context.applicationContext
    }

    fun saveScreenshot(byteArray: ByteArray) {
        if (::context.isInitialized.not()) {
            throw NullPointerException("Context not initialized")
        }
        createOrUseFolder(FileTarget.SCREENSHOT.folder)?.let {
            FileOutputStream(createFilename(it.absolutePath, FileTarget.SCREENSHOT))
                .use { outputStream ->
                    outputStream.write(byteArray)
                }
        }
    }

    fun createVideoFilename(): String? {
        if (::context.isInitialized.not()) {
            throw NullPointerException("Context not initialized")
        }
        return createOrUseFolder(FileTarget.VIDEO.folder)?.let {
            createFilename(it.absolutePath, FileTarget.VIDEO)
        }
    }

    fun createOrUseRoot(context: Context) =
        File(context.filesDir, "thimble").let {
            if (it.exists()) {
                it
            } else {
                if (it.mkdir()) {
                    it
                } else {
                    null
                }
            }
        }

    fun clearFolder(folder: String) {
        createOrUseFolder(folder)?.let {
            it.listFiles().orEmpty().forEach { file ->
                file.delete()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createFilename(
        path: String,
        target: FileTarget,
        denominator: String = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(Date())
    ): String = "$path${File.separator}${target.prefix}_$denominator.${target.extension}"

    private fun createOrUseFolder(child: String): File? =
        createOrUseRoot(context)?.let {
            val folder = File(it, child)
            return if (folder.exists()) {
                folder
            } else {
                if (folder.mkdir()) {
                    folder
                } else {
                    null
                }
            }
        }
}
