package com.infinum.thimble.models.configuration

import androidx.annotation.FloatRange
import com.infinum.thimble.models.configuration.shared.AbstractConfiguration
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class MockupConfiguration(
    override var enabled: Boolean = false,
    @FloatRange(from = 0.0, to = 1.0) val opacity: Float = 0.2f,
    val portraitUri: String? = null,
    val landscapeUri: String? = null
) : AbstractConfiguration()