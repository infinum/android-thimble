package com.infinum.thimble.ui.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

internal class MockupPortraitPickerContract : ActivityResultContract<Intent, MockupPortraitPickerContract.Output>() {

    override fun createIntent(context: Context, input: Intent): Intent =
        input

    override fun parseResult(resultCode: Int, intent: Intent?): Output =
        if (resultCode == Activity.RESULT_OK) {
            Output(uri = intent?.data)
        } else {
            Output()
        }

    data class Output(
        val uri: Uri? = null
    )
}
