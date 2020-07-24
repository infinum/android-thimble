package com.infinum.thimble.ui

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ThimbleApplication : Application() {

    private var mediaProjectionResultCode = Activity.RESULT_CANCELED

    private var mediaProjectionData: Intent? = null

    fun setMediaProjectionPermissionData(resultCode: Int, data: Intent?) {
        mediaProjectionResultCode = resultCode
        mediaProjectionData = data
    }

    fun mediaProjectionResultCode(): Int = mediaProjectionResultCode

    fun mediaProjectionData(): Intent? = mediaProjectionData
}