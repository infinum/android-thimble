package com.infinum.thimble.models

internal enum class FileTarget(
    val folder: String,
    val prefix: String,
    val extension: String,
    val mimeType: String,
    val requestCode: Int
) {
    SCREENSHOT(
        folder = "screenshots",
        prefix = "screenshot",
        extension = "jpg",
        mimeType = "image/jpeg",
        requestCode = 777
    ),
    VIDEO(
        folder = "videos",
        prefix = "video",
        extension = "mp4",
        mimeType = "video/mp4",
        requestCode = 888
    );

    companion object {

        operator fun invoke(requestCode: Int) =
            values().firstOrNull { it.requestCode == requestCode }
    }
}