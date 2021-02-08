package com.infinum.thimble.ui.fragments

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.RestrictTo
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.infinum.thimble.R
import com.infinum.thimble.databinding.ThimbleFragmentOverlayGridBinding
import com.infinum.thimble.databinding.ThimbleViewColorPickerBinding
import com.infinum.thimble.extensions.getHexCode
import com.infinum.thimble.extensions.toHalf
import com.infinum.thimble.extensions.toPx
import com.infinum.thimble.models.LineOrientation
import com.infinum.thimble.models.configuration.GridConfiguration
import com.infinum.thimble.ui.Defaults
import com.infinum.thimble.ui.fragments.shared.AbstractOverlayFragment
import com.infinum.thimble.ui.shared.viewBinding
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import java.util.Locale
import kotlin.math.floor
import kotlin.math.roundToInt

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class GridOverlayFragment :
    AbstractOverlayFragment<GridConfiguration>(R.layout.thimble_fragment_overlay_grid) {

    override val binding: ThimbleFragmentOverlayGridBinding by viewBinding(
        ThimbleFragmentOverlayGridBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            cardView.shapeAppearanceModel = Defaults.createShapeAppearanceModel()

            horizontalGridSizeSlider.isEnabled = false
            verticalGridSizeSlider.isEnabled = false
        }
    }

    override fun toggleUi(enabled: Boolean) {
        with(binding) {
            gridOverlaySwitch.isEnabled = enabled

            horizontalLineColorButton.isEnabled = enabled
            verticalLineColorButton.isEnabled = enabled
            horizontalLineColorButton.alpha = if (enabled) 1.0f else 1.0f.toHalf()
            verticalLineColorButton.alpha = if (enabled) 1.0f else 1.0f.toHalf()

            decreaseHorizontalGridSizeButton.isEnabled = enabled
            increaseHorizontalGridSizeButton.isEnabled = enabled
            horizontalGridSizeSlider.isEnabled = enabled

            decreaseVerticalGridSizeButton.isEnabled = enabled
            increaseVerticalGridSizeButton.isEnabled = enabled
            verticalGridSizeSlider.isEnabled = enabled
        }
    }

    override fun configure(configuration: GridConfiguration) {
        with(binding) {
            gridOverlaySwitch.isChecked = configuration.enabled
            gridOverlaySwitch.setOnCheckedChangeListener { _, isChecked ->
                serviceActivity?.toggleGrid(isChecked)
            }

            horizontalLineColorButton.setOnClickListener {
                openGridColorPicker(LineOrientation.HORIZONTAL, configuration)
            }
            verticalLineColorButton.setOnClickListener {
                openGridColorPicker(LineOrientation.VERTICAL, configuration)
            }

            horizontalGridSizeSlider.valueFrom =
                resources.getInteger(R.integer.thimble_grid_gap_from).toFloat()
            horizontalGridSizeSlider.value =
                configuration.horizontalGridSize.toFloat() / resources.displayMetrics.density
            horizontalGridSizeSlider.valueTo =
                floor((resources.displayMetrics.heightPixels / resources.displayMetrics.density).toHalf()).roundToInt()
                    .toFloat()
            decreaseHorizontalGridSizeButton.setOnClickListener {
                horizontalGridSizeSlider.value =
                    (horizontalGridSizeSlider.value - horizontalGridSizeSlider.stepSize).coerceAtLeast(
                        horizontalGridSizeSlider.valueFrom
                    )
            }
            increaseHorizontalGridSizeButton.setOnClickListener {
                horizontalGridSizeSlider.value =
                    (horizontalGridSizeSlider.value + horizontalGridSizeSlider.stepSize).coerceAtMost(
                        horizontalGridSizeSlider.valueTo
                    )
            }
            horizontalGridSizeSlider.clearOnChangeListeners()
            horizontalGridSizeSlider.addOnChangeListener { _, value, _ ->
                serviceActivity?.updateGridHorizontalGap(value.toPx())
                horizontalGridSizeValueLabel.text = String.format(
                    getString(R.string.thimble_option_template_dp),
                    value.roundToInt()
                )
            }

            verticalGridSizeSlider.valueFrom =
                resources.getInteger(R.integer.thimble_grid_gap_from).toFloat()
            verticalGridSizeSlider.value =
                configuration.verticalGridSize.toFloat() / resources.displayMetrics.density
            verticalGridSizeSlider.valueTo =
                floor((resources.displayMetrics.widthPixels / resources.displayMetrics.density).toHalf()).roundToInt()
                    .toFloat()
            decreaseVerticalGridSizeButton.setOnClickListener {
                verticalGridSizeSlider.value =
                    (verticalGridSizeSlider.value - verticalGridSizeSlider.stepSize).coerceAtLeast(
                        verticalGridSizeSlider.valueFrom
                    )
            }
            increaseVerticalGridSizeButton.setOnClickListener {
                verticalGridSizeSlider.value =
                    (verticalGridSizeSlider.value + verticalGridSizeSlider.stepSize).coerceAtMost(
                        verticalGridSizeSlider.valueTo
                    )
            }
            verticalGridSizeSlider.clearOnChangeListeners()
            verticalGridSizeSlider.addOnChangeListener { _, value, _ ->
                serviceActivity?.updateGridVerticalGap(value.toPx())
                verticalGridSizeValueLabel.text = String.format(
                    getString(R.string.thimble_option_template_dp),
                    value.roundToInt()
                )
            }
            horizontalLineColorButton.setBackgroundColor(configuration.horizontalLineColor)
            verticalLineColorButton.setBackgroundColor(configuration.verticalLineColor)
            horizontalLineColorValueLabel.text =
                configuration.horizontalLineColor.getHexCode()
            vertialLineColorValueLabel.text = configuration.verticalLineColor.getHexCode()
            horizontalGridSizeValueLabel.text = String.format(
                getString(R.string.thimble_option_template_dp),
                horizontalGridSizeSlider.value.roundToInt()
            )
            verticalGridSizeValueLabel.text = String.format(
                getString(R.string.thimble_option_template_dp),
                verticalGridSizeSlider.value.roundToInt()
            )
        }
    }

    private fun openGridColorPicker(
        orientation: LineOrientation,
        configuration: GridConfiguration
    ) {
        ThimbleViewColorPickerBinding.inflate(layoutInflater)
            .apply {
                colorPickerView.setColorListener(object : ColorEnvelopeListener {
                    override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                        envelope?.let {
                            when (orientation) {
                                LineOrientation.HORIZONTAL -> setHorizontalGridLineColor(it.color)
                                LineOrientation.VERTICAL -> setVerticalGridLineColor(it.color)
                            }
                        }
                    }
                })
                when (orientation) {
                    LineOrientation.HORIZONTAL -> colorPickerView.fireColorListener(
                        configuration.horizontalLineColor,
                        false
                    )
                    LineOrientation.VERTICAL -> colorPickerView.fireColorListener(
                        configuration.verticalLineColor,
                        false
                    )
                }
            }
            .let {
                MaterialAlertDialogBuilder(it.root.context)
                    .setTitle(
                        String.format(
                            getString(R.string.thimble_color_picker_template_title),
                            orientation.name.toLowerCase(Locale.getDefault())
                        )
                    )
                    .setView(it.root)
                    .setCancelable(false)
                    .setNegativeButton(getString(R.string.thimble_close)) { dialog: DialogInterface, _: Int ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
    }

    private fun setHorizontalGridLineColor(@ColorInt color: Int) {
        with(binding) {
            horizontalLineColorButton.backgroundTintList = ColorStateList.valueOf(color)
            horizontalLineColorValueLabel.text = color.getHexCode()
        }

        serviceActivity?.updateGridHorizontalColor(color)
    }

    private fun setVerticalGridLineColor(@ColorInt color: Int) {
        with(binding) {
            verticalLineColorButton.backgroundTintList = ColorStateList.valueOf(color)
            vertialLineColorValueLabel.text = color.getHexCode()
        }

        serviceActivity?.updateGridVerticalColor(color)
    }
}
