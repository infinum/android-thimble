package com.infinum.thimble.models.configuration

import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import com.infinum.thimble.models.VideoQuality
import com.infinum.thimble.models.configuration.shared.AbstractConfiguration
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class RecorderConfiguration(
    override var enabled: Boolean = false,
    @IntRange(from = 0, to = 60) val recorderDelay: Int = 0,
    @FloatRange(from = 0.0, to = 1.0) val compression: Float = 0.9f,
    val recordInternalAudio: Boolean = false,
    val videoQuality: VideoQuality = VideoQuality.HIGH
) : AbstractConfiguration()