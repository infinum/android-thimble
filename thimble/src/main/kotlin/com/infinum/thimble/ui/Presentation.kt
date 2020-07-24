package com.infinum.thimble.ui

import android.content.Context
import android.content.Intent

internal object Presentation {

    private lateinit var launchIntent: Intent

    private lateinit var context: Context

    fun initialise(context: Context) {
        this.context = context
        this.launchIntent = Intent(this.context, ThimbleActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    fun launchIntent() = launchIntent

    fun show() = context.startActivity(launchIntent)
}
