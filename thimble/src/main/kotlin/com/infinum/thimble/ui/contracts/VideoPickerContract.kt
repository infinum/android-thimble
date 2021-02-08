package com.infinum.thimble.ui.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

internal class VideoPickerContract : ActivityResultContract<Intent, VideoPickerContract.Output>() {

    override fun createIntent(context: Context, input: Intent): Intent =
        input

    override fun parseResult(resultCode: Int, intent: Intent?): Output =
        if (resultCode == Activity.RESULT_OK) {
            Output(data = intent)
        } else {
            Output()
        }

    data class Output(
        val data: Intent? = null
    )
}
