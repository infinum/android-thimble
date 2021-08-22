package com.infinum.thimble.ui.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.M)
internal class OverlayPermissionContract(
    private val context: Context
) : ActivityResultContract<Unit, Boolean>() {

    companion object {
        fun canDrawOverlays(context: Context): Boolean =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(context.applicationContext)
    }

    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        if (resultCode == Activity.RESULT_OK) {
            canDrawOverlays(context.applicationContext)
        } else {
            false
        }
}
