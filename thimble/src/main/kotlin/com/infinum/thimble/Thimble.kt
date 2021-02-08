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
public object Thimble {

    /**
     * Prepared a predefined Intent to launch Thimble and/or be modified before launching manually.
     */
    @JvmStatic
    public fun launchIntent(): Intent = Presentation.launchIntent()

    /**
     * Shows Thimble UI.
     */
    @JvmStatic
    public fun show(): Unit = Presentation.show()

    /**
     * Start Thimble service.
     */
    @JvmStatic
    public fun start(context: Context): Unit =
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
    public fun stop(context: Context): Unit =
        ContextCompat.startForegroundService(
            context,
            Intent(context, ThimbleService::class.java).apply {
                action = ServiceAction.STOP.code
            }
        )
}
