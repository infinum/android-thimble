package com.infinum.thimble.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.infinum.thimble.R
import com.infinum.thimble.databinding.ThimbleFragmentOverlayMockupBinding
import com.infinum.thimble.extensions.fromPercentage
import com.infinum.thimble.extensions.toPercentage
import com.infinum.thimble.models.MockupOrientation
import com.infinum.thimble.models.configuration.MockupConfiguration
import com.infinum.thimble.ui.fragments.shared.AbstractOverlayFragment
import com.infinum.thimble.ui.shared.viewBinding
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class MockupOverlayFragment :
    AbstractOverlayFragment<MockupConfiguration>(R.layout.thimble_fragment_overlay_mockup) {

    companion object {
        private const val MIME_TYPE_IMAGE = "image/*"
    }

    override val binding: ThimbleFragmentOverlayMockupBinding by viewBinding(
        ThimbleFragmentOverlayMockupBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            mockupOpacitySlider.isEnabled = false
            portraitMockup.isEnabled = false
            landscapeMockup.isEnabled = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        MockupOrientation(requestCode)
            ?.let { orientation ->
                when (orientation) {
                    MockupOrientation.PORTRAIT -> {
                        when (resultCode) {
                            Activity.RESULT_OK -> data?.data?.let {
                                setMockupPortrait(it)
                            } ?: showMessage(getString(R.string.thimble_message_mockup_error))
                            Activity.RESULT_CANCELED -> Unit
                            else -> Unit
                        }
                    }
                    MockupOrientation.LANDSCAPE -> {
                        when (resultCode) {
                            Activity.RESULT_OK -> data?.data?.let {
                                setMockupLandscape(it)
                            } ?: showMessage(getString(R.string.thimble_message_mockup_error))
                            Activity.RESULT_CANCELED -> Unit
                            else -> Unit
                        }
                    }
                }
            }
    }

    override fun toggleUi(enabled: Boolean) {
        with(binding) {
            mockupOverlaySwitch.isEnabled = enabled

            decreaseMockupOpacityButton.isEnabled = enabled
            increaseMockupOpacityButton.isEnabled = enabled
            mockupOpacitySlider.isEnabled = enabled

            portraitMockup.isEnabled = enabled
            landscapeMockup.isEnabled = enabled

            clearPortraitMockupButton.isEnabled = enabled
            clearLandscapeMockupButton.isEnabled = enabled
        }
    }

    override fun configure(configuration: MockupConfiguration) {
        with(binding) {
            mockupOverlaySwitch.setOnCheckedChangeListener(null)
            mockupOverlaySwitch.isChecked = configuration.enabled
            mockupOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                serviceActivity?.toggleMockup(isChecked)
            }
            decreaseMockupOpacityButton.setOnClickListener {
                mockupOpacitySlider.value =
                    (mockupOpacitySlider.value - mockupOpacitySlider.stepSize)
                        .coerceAtLeast(mockupOpacitySlider.valueFrom)
            }
            increaseMockupOpacityButton.setOnClickListener {
                mockupOpacitySlider.value =
                    (mockupOpacitySlider.value + mockupOpacitySlider.stepSize)
                        .coerceAtMost(mockupOpacitySlider.valueTo)
            }
            mockupOpacitySlider.clearOnChangeListeners()
            mockupOpacitySlider.addOnChangeListener { _, value, _ ->
                serviceActivity?.updateMockupOpacity(value.fromPercentage())
                mockupOpacityValueLabel.text = value.toPercentage()
            }

            portraitMockupLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio =
                    "${resources.displayMetrics.widthPixels}:${resources.displayMetrics.heightPixels}"
            }
            landscapeMockupLayout.updateLayoutParams<ConstraintLayout.LayoutParams> {
                dimensionRatio =
                    "${resources.displayMetrics.heightPixels}:${resources.displayMetrics.widthPixels}"
            }
            portraitMockup.setOnClickListener {
                openImagePicker(MockupOrientation.PORTRAIT)
            }
            portraitMockup.setOnLongClickListener {
                clearPortraitMockup()
                true
            }
            landscapeMockup.setOnClickListener {
                openImagePicker(MockupOrientation.LANDSCAPE)
            }
            landscapeMockup.setOnLongClickListener {
                clearLandscapeMockup()
                true
            }

            clearPortraitMockupButton.setOnClickListener {
                clearPortraitMockup()
            }
            clearLandscapeMockupButton.setOnClickListener {
                clearLandscapeMockup()
            }

            mockupOpacityValueLabel.text = configuration.opacity.toPercentage()
        }
    }

    private fun openImagePicker(orientation: MockupOrientation) =
        startActivityForResult(
            Intent.createChooser(
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = MIME_TYPE_IMAGE
                },
                String.format(
                    getString(
                        R.string.thimble_image_picker_template_title,
                        orientation.name.toLowerCase(Locale.getDefault())
                    )
                )
            ),
            orientation.requestCode
        )

    private fun setMockupPortrait(uri: Uri) {
        with(binding) {
            portraitMockup.setImageURI(uri)
            clearPortraitMockupButton.isVisible = true
        }

        serviceActivity?.updateMockupPortraitUri(uri.toString())
    }

    private fun setMockupLandscape(uri: Uri) {
        with(binding) {
            landscapeMockup.setImageURI(uri)
            clearLandscapeMockupButton.isVisible = true
        }

        serviceActivity?.updateMockupLandscapeUri(uri.toString())
    }

    private fun clearPortraitMockup() {
        with(binding) {
            if (portraitMockup.drawable != null) {
                portraitMockup.setImageDrawable(null)
                clearPortraitMockupButton.isGone = true

                showMessage(getString(R.string.thimble_message_mockup_cleared))
            }
        }

        serviceActivity?.updateMockupPortraitUri(null)
    }

    private fun clearLandscapeMockup() {
        with(binding) {
            if (landscapeMockup.drawable != null) {
                landscapeMockup.setImageDrawable(null)
                clearLandscapeMockupButton.isGone = true

                showMessage(getString(R.string.thimble_message_mockup_cleared))
            }
        }

        serviceActivity?.updateMockupLandscapeUri(null)
    }
}