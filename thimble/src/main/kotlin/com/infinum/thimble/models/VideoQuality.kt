package com.infinum.thimble.models

@Suppress("MagicNumber")
internal enum class VideoQuality(val bitrate: Long) {
    LOW(512 * 1000L),
    MEDIUM(12032 * 1000L),
    HIGH(24576 * 1000L);

    companion object {

        operator fun invoke(bitrate: Long) =
            values().firstOrNull { it.bitrate == bitrate }
    }
}
