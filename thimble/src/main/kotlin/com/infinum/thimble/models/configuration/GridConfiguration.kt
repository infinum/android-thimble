package com.infinum.thimble.models.configuration

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Px
import com.infinum.thimble.extensions.toPx
import com.infinum.thimble.models.configuration.shared.AbstractConfiguration
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class GridConfiguration(
    override val enabled: Boolean = false,
    @ColorInt val horizontalLineColor: Int = Color.RED,
    @ColorInt val verticalLineColor: Int = Color.BLUE,
    @Px val horizontalGridSize: Int = 8.0f.toPx(),
    @Px val verticalGridSize: Int = 8.0f.toPx()
) : AbstractConfiguration()