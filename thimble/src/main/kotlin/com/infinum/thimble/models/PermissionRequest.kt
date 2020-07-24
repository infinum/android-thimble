package com.infinum.thimble.models

internal enum class PermissionRequest(val requestCode: Int) {
    OVERLAY(requestCode = 444),
    MEDIA_PROJECTION(requestCode = 666);

    companion object {

        operator fun invoke(requestCode: Int) =
            values().firstOrNull { it.requestCode == requestCode }
    }
}