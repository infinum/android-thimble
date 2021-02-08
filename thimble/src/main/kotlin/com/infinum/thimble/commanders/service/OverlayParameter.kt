package com.infinum.thimble.commanders.service

internal enum class OverlayParameter(val code: Int) {
    COLOR_HORIZONTAL(code = 1),
    COLOR_VERTICAL(code = 2),
    GAP_HORIZONTAL(code = 3),
    GAP_VERTICAL(code = 4),
    OPACITY(code = 5),
    URI_PORTRAIT(code = 6),
    URI_LANDSCAPE(code = 7),
    COLOR_MODEL(code = 8),
    RECORDER_DELAY(code = 9),
    SCREENSHOT_COMPRESSION(code = 10),
    RECORDER_AUDIO(code = 11),
    VIDEO_QUALITY(code = 12);

    companion object {

        operator fun invoke(code: Int) = values().firstOrNull { it.code == code }
    }
}
