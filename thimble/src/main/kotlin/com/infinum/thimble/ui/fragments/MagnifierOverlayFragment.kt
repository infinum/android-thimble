package com.infinum.thimble.ui.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.thimble.R
import com.infinum.thimble.databinding.ThimbleFragmentOverlayMagnifierBinding
import com.infinum.thimble.models.ColorModel
import com.infinum.thimble.models.PermissionRequest
import com.infinum.thimble.models.configuration.MagnifierConfiguration
import com.infinum.thimble.ui.Defaults
import com.infinum.thimble.ui.ThimbleApplication
import com.infinum.thimble.ui.fragments.shared.AbstractOverlayFragment
import com.infinum.thimble.ui.shared.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class MagnifierOverlayFragment :
    AbstractOverlayFragment<MagnifierConfiguration>(R.layout.thimble_fragment_overlay_magnifier) {

    override val binding: ThimbleFragmentOverlayMagnifierBinding by viewBinding(
        ThimbleFragmentOverlayMagnifierBinding::bind
    )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        PermissionRequest(requestCode)
            ?.let { permission ->
                when (permission) {
                    PermissionRequest.MEDIA_PROJECTION_MAGNIFIER,
                    PermissionRequest.MEDIA_PROJECTION_RECORDER -> {
                        when (resultCode) {
                            Activity.RESULT_OK -> {
                                (context?.applicationContext as? ThimbleApplication)
                                    ?.setMediaProjectionPermissionData(resultCode, data)

                                if (PermissionRequest(permission.requestCode) ==
                                    PermissionRequest.MEDIA_PROJECTION_MAGNIFIER
                                ) {
                                    serviceActivity?.toggleMagnifier(true)
                                }
                            }
                            Activity.RESULT_CANCELED -> {
                                with(binding) {
                                    if (magnifierSwitch.isChecked) {
                                        magnifierSwitch.isChecked = false
                                    }
                                }
                            }
                            else -> Unit
                        }
                    }
                    else -> throw NotImplementedError()
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            cardView.shapeAppearanceModel = Defaults.createShapeAppearanceModel()
        }
    }

    override fun toggleUi(enabled: Boolean) {
        with(binding) {
            magnifierSwitch.isEnabled = enabled
            hexButton.isEnabled = enabled
            rgbButton.isEnabled = enabled
            hsvButton.isEnabled = enabled
            if (enabled) {
                hexButton.isChecked = true
                rgbButton.isChecked = false
                hsvButton.isChecked = false
                colorModelToggleGroup.check(R.id.hexButton)
            } else {
                hexButton.isChecked = enabled
                rgbButton.isChecked = enabled
                hsvButton.isChecked = enabled
                colorModelToggleGroup.check(View.NO_ID)
            }
        }
    }

    override fun configure(configuration: MagnifierConfiguration) {
        with(binding) {
            magnifierSwitch.isChecked = configuration.enabled
            magnifierSwitch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    startProjection()
                } else {
                    serviceActivity?.toggleMagnifier(isChecked)
                }
            }
            colorModelToggleGroup.clearOnButtonCheckedListeners()
            colorModelToggleGroup.check(
                when (configuration.colorModel) {
                    ColorModel.HEX -> R.id.hexButton
                    ColorModel.RGB -> R.id.rgbButton
                    ColorModel.HSV -> R.id.hsvButton
                }
            )
            colorModelToggleGroup.addOnButtonCheckedListener { _, checkedId, _ ->
                serviceActivity?.updateMagnifierColorModel(
                    when (checkedId) {
                        R.id.hexButton -> ColorModel.HEX
                        R.id.rgbButton -> ColorModel.RGB
                        R.id.hsvButton -> ColorModel.HSV
                        else -> ColorModel.HEX
                    }
                )
            }
        }
    }

    private fun startProjection() {
        val allowed =
            (context?.applicationContext as? ThimbleApplication)?.mediaProjectionAllowed() ?: false
        if (allowed) {
            serviceActivity?.toggleMagnifier(true)
        } else {
            (context?.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)
                ?.createScreenCaptureIntent()
                ?.let {
                    startActivityForResult(
                        it,
                        PermissionRequest.MEDIA_PROJECTION_MAGNIFIER.requestCode
                    )
                }
        }
    }
}