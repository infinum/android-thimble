package com.infinum.thimble.models.configuration

import com.infinum.thimble.models.ColorModel
import com.infinum.thimble.models.configuration.shared.AbstractConfiguration
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class MagnifierConfiguration(
    override var enabled: Boolean = false,
    val colorModel: ColorModel = ColorModel.HEX
) : AbstractConfiguration()