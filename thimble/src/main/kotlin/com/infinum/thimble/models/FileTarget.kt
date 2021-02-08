package com.infinum.thimble.models

internal enum class FileTarget(
    val folder: String,
    val prefix: String,
    val extension: String,
    val mimeType: String
) {
    SCREENSHOT(
        folder = "screenshots",
        prefix = "screenshot",
        extension = "jpg",
        mimeType = "image/jpeg"
    ),
    VIDEO(
        folder = "videos",
        prefix = "video",
        extension = "mp4",
        mimeType = "video/mp4"
    )
}
