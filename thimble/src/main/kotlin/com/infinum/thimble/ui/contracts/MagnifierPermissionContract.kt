package com.infinum.thimble.ui.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

internal class MagnifierPermissionContract : ActivityResultContract<Intent, MagnifierPermissionContract.Output>() {

    override fun createIntent(context: Context, input: Intent): Intent =
        input

    override fun parseResult(resultCode: Int, intent: Intent?): Output =
        if (resultCode == Activity.RESULT_OK) {
            Output(
                success = true,
                resultCode = resultCode,
                data = intent
            )
        } else {
            Output(
                success = false,
            )
        }

    data class Output(
        val success: Boolean,
        val resultCode: Int = Activity.RESULT_CANCELED,
        val data: Intent? = null
    )
}
