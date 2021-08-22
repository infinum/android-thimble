package com.infinum.thimble.ui.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RestrictTo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.infinum.thimble.R
import com.infinum.thimble.databinding.ThimbleFragmentOverlayRecorderBinding
import com.infinum.thimble.extensions.fromPercentage
import com.infinum.thimble.extensions.toPercentage
import com.infinum.thimble.models.FileTarget
import com.infinum.thimble.models.VideoQuality
import com.infinum.thimble.models.configuration.RecorderConfiguration
import com.infinum.thimble.ui.Defaults
import com.infinum.thimble.ui.ThimbleApplication
import com.infinum.thimble.ui.contracts.RecorderPermissionContract
import com.infinum.thimble.ui.contracts.ScreenshotPickerContract
import com.infinum.thimble.ui.contracts.VideoPickerContract
import com.infinum.thimble.ui.fragments.shared.AbstractOverlayFragment
import com.infinum.thimble.ui.shared.viewBinding
import com.infinum.thimble.ui.utils.FileUtils
import java.util.Locale
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class RecorderOverlayFragment :
    AbstractOverlayFragment<RecorderConfiguration>(R.layout.thimble_fragment_overlay_recorder) {

    private lateinit var contract: ActivityResultLauncher<Intent>
    private lateinit var screenshotPickerContract: ActivityResultLauncher<Intent>
    private lateinit var videoPickerContract: ActivityResultLauncher<Intent>

    override val binding: ThimbleFragmentOverlayRecorderBinding by viewBinding(
        ThimbleFragmentOverlayRecorderBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            cardView.shapeAppearanceModel = Defaults.createShapeAppearanceModel()

            screenshotCompressionSlider.isEnabled = false

            contract = registerForActivityResult(RecorderPermissionContract()) { output ->
                if (output.success) {
                    (context?.applicationContext as? ThimbleApplication)
                        ?.setMediaProjectionPermissionData(
                            output.resultCode,
                            output.data
                        )
                    serviceActivity?.toggleRecorder(output.success)
                } else {
                    if (recorderSwitch.isChecked) {
                        recorderSwitch.isChecked = output.success
                    }
                }
            }

            screenshotPickerContract = registerForActivityResult(ScreenshotPickerContract()) { output ->
                output.data?.clipData?.let {
                    val uris = mutableListOf<Uri>()
                    for (i in 0 until it.itemCount) {
                        uris.add(it.getItemAt(i).uri)
                    }
                    shareFiles(FileTarget.SCREENSHOT, uris.toList())
                } ?: output.data?.data?.let {
                    shareFiles(FileTarget.SCREENSHOT, listOf(it))
                } ?: showMessage(getString(R.string.thimble_message_mockup_error))
            }
            videoPickerContract = registerForActivityResult(VideoPickerContract()) { output ->
                output.data?.clipData?.let {
                    val uris = mutableListOf<Uri>()
                    for (i in 0 until it.itemCount) {
                        uris.add(it.getItemAt(i).uri)
                    }
                    shareFiles(FileTarget.VIDEO, uris.toList())
                } ?: output.data?.data?.let {
                    shareFiles(FileTarget.VIDEO, listOf(it))
                } ?: showMessage(getString(R.string.thimble_message_mockup_error))
            }
        }
    }

    override fun onDestroyView() {
        contract.unregister()
        screenshotPickerContract.unregister()
        videoPickerContract.unregister()
        super.onDestroyView()
    }

    override fun toggleUi(enabled: Boolean) {
        with(binding) {
            recorderSwitch.isEnabled = enabled
            decreaseDelayButton.isEnabled = enabled
            increaseDelayButton.isEnabled = enabled
            delaySlider.isEnabled = enabled
            decreaseScreenshotCompressionButton.isEnabled = enabled
            increaseScreenshotCompressionButton.isEnabled = enabled
            screenshotCompressionSlider.isEnabled = enabled
            audioSwitch.isEnabled = enabled
            lowButton.isEnabled = enabled
            mediumButton.isEnabled = enabled
            highButton.isEnabled = enabled
            if (enabled) {
                lowButton.isChecked = true
                mediumButton.isChecked = false
                highButton.isChecked = false
                videoQualityToggleGroup.check(R.id.lowButton)
            } else {
                lowButton.isChecked = enabled
                mediumButton.isChecked = enabled
                highButton.isChecked = enabled
                videoQualityToggleGroup.check(View.NO_ID)
            }
        }
    }

    override fun configure(configuration: RecorderConfiguration) {
        with(binding) {
            recorderSwitch.isChecked = configuration.enabled
            recorderSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startProjection()
                } else {
                    serviceActivity?.toggleRecorder(isChecked)
                }
            }
            decreaseDelayButton.setOnClickListener {
                delaySlider.value =
                    (delaySlider.value - delaySlider.stepSize)
                        .coerceAtLeast(delaySlider.valueFrom)
            }
            increaseDelayButton.setOnClickListener {
                delaySlider.value =
                    (delaySlider.value + delaySlider.stepSize)
                        .coerceAtMost(delaySlider.valueTo)
            }
            delaySlider.clearOnChangeListeners()
            delaySlider.addOnChangeListener { _, value, _ ->
                serviceActivity?.updateRecorderDelay(value.roundToInt())
                delayValueLabel.text =
                    String.format(
                        getString(R.string.thimble_option_template_s),
                        value.roundToInt()
                    )
            }
            delayValueLabel.text = String.format(
                getString(R.string.thimble_option_template_s),
                configuration.recorderDelay
            )
            decreaseScreenshotCompressionButton.setOnClickListener {
                screenshotCompressionSlider.value =
                    (screenshotCompressionSlider.value - screenshotCompressionSlider.stepSize)
                        .coerceAtLeast(screenshotCompressionSlider.valueFrom)
            }
            increaseScreenshotCompressionButton.setOnClickListener {
                screenshotCompressionSlider.value =
                    (screenshotCompressionSlider.value + screenshotCompressionSlider.stepSize)
                        .coerceAtMost(screenshotCompressionSlider.valueTo)
            }
            screenshotCompressionSlider.clearOnChangeListeners()
            screenshotCompressionSlider.addOnChangeListener { _, value, _ ->
                serviceActivity?.updateScreenshotCompression(value.fromPercentage())
                screenshotCompressionValueLabel.text = value.toPercentage(multiplier = 1)
            }
            screenshotCompressionValueLabel.text = configuration.compression.toPercentage()
            with(screenshotToolbar) {
                setNavigationOnClickListener {
                    screenshotPickerContract.launch(
                        Intent.createChooser(
                            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                type = FileTarget.SCREENSHOT.mimeType
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                addCategory(Intent.CATEGORY_OPENABLE)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("thimble://"))
                                }
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            },
                            String.format(
                                getString(
                                    R.string.thimble_file_picker_template_title,
                                    FileTarget.SCREENSHOT.name.lowercase(Locale.getDefault())
                                )
                            )
                        )
                    )
                }
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.clear -> {
                            showClearConfirmation(FileTarget.SCREENSHOT)
                            true
                        }
                        else -> false
                    }
                }
            }
            audioSwitch.isChecked = configuration.recordInternalAudio
            audioSwitch.setOnCheckedChangeListener { _, isChecked ->
                serviceActivity?.updateRecorderAudio(isChecked)
            }
            videoQualityToggleGroup.clearOnButtonCheckedListeners()
            videoQualityToggleGroup.check(
                when (configuration.videoQuality) {
                    VideoQuality.LOW -> R.id.lowButton
                    VideoQuality.MEDIUM -> R.id.mediumButton
                    VideoQuality.HIGH -> R.id.highButton
                }
            )
            videoQualityToggleGroup.addOnButtonCheckedListener { _, checkedId, _ ->
                serviceActivity?.updateVideoQuality(
                    when (checkedId) {
                        R.id.lowButton -> VideoQuality.LOW
                        R.id.mediumButton -> VideoQuality.MEDIUM
                        R.id.highButton -> VideoQuality.HIGH
                        else -> VideoQuality.HIGH
                    }
                )
            }
            with(videoToolbar) {
                setNavigationOnClickListener {
                    videoPickerContract.launch(
                        Intent.createChooser(
                            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                type = FileTarget.VIDEO.mimeType
                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                addCategory(Intent.CATEGORY_OPENABLE)
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("thimble://"))
                                }
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                            },
                            String.format(
                                getString(
                                    R.string.thimble_file_picker_template_title,
                                    FileTarget.VIDEO.name.lowercase(Locale.getDefault())
                                )
                            )
                        )
                    )
                }
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.clear -> {
                            showClearConfirmation(FileTarget.VIDEO)
                            true
                        }
                        else -> false
                    }
                }
            }
        }
    }

    private fun startProjection() {
        val allowed =
            (context?.applicationContext as? ThimbleApplication)?.mediaProjectionAllowed() ?: false
        if (allowed) {
            serviceActivity?.toggleRecorder(true)
        } else {
            (context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)
                ?.createScreenCaptureIntent()
                ?.let { contract.launch(it) }
        }
    }

    private fun shareFiles(target: FileTarget, uris: List<Uri>) =
        startActivity(
            Intent.createChooser(
                Intent(Intent.ACTION_SEND_MULTIPLE)
                    .apply {
                        type = target.mimeType
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
                    },
                String.format(
                    getString(R.string.thimble_share_picker_template_title),
                    getString(R.string.thimble_name),
                    if (uris.count() == 1) target.prefix else target.folder
                )
            )
        )

    private fun showClearConfirmation(target: FileTarget) {
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle(R.string.thimble_name)
            .setMessage(
                String.format(
                    getString(R.string.thimble_recorder_clear_template_message),
                    target.folder
                )
            )
            .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface?, _ ->
                dialog?.dismiss()
            }
            .setPositiveButton(android.R.string.ok) { dialog: DialogInterface?, _ ->
                FileUtils.clearFolder(target.folder)
                dialog?.dismiss()
            }
            .create()
            .show()
    }
}
