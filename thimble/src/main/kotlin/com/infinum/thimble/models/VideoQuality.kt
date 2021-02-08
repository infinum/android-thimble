package com.infinum.thimble.models

@Suppress("MagicNumber")
internal enum class VideoQuality(val bitrate: Long) {
    LOW(512 * 1000),
    MEDIUM(12032 * 1000),
    HIGH(24576 * 1000);

    companion object {

        operator fun invoke(bitrate: Long) =
            values().firstOrNull { it.bitrate == bitrate }
    }
}
