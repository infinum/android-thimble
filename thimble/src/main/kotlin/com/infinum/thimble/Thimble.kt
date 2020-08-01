package com.infinum.thimble

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.infinum.thimble.models.ServiceAction
import com.infinum.thimble.ui.Presentation
import com.infinum.thimble.ui.ThimbleService

/**
 * Access only class for Thimble UI and features
 */
object Thimble {

    /**
     * Prepared a predefined Intent to launch Thimble and/or be modified before launching manually.
     */
    @JvmStatic
    fun launchIntent() = Presentation.launchIntent()

    /**
     * Shows Thimble UI.
     */
    @JvmStatic
    fun show() = Presentation.show()

    /**
     * Start Thimble service.
     */
    @JvmStatic
    fun start(context: Context) =
        ContextCompat.startForegroundService(
            context,
            Intent(context, ThimbleService::class.java).apply {
                action = ServiceAction.START.code
            }
        )

    /**
     * Stop Thimble service.
     */
    @JvmStatic
    fun stop(context: Context) {
        ContextCompat.startForegroundService(
            context,
            Intent(context, ThimbleService::class.java).apply {
                action = ServiceAction.STOP.code
            }
        )
    }
}