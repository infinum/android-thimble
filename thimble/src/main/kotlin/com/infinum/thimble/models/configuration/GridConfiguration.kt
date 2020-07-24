package com.infinum.thimble.models.configuration

import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.infinum.thimble.extensions.toPx
import com.infinum.thimble.models.configuration.shared.AbstractConfiguration
import com.infinum.thimble.ui.Defaults
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class GridConfiguration(
    override val enabled: Boolean = false,
    @ColorInt val horizontalLineColor: Int = Defaults.gridHorizontalColor(),
    @ColorInt val verticalLineColor: Int = Defaults.gridVerticalColor(),
    @Px val horizontalGridSize: Int = 8.0f.toPx(),
    @Px val verticalGridSize: Int = 8.0f.toPx()
) : AbstractConfiguration()