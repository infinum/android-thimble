package com.infinum.thimble.builders

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.infinum.thimble.models.BundleKeys
import com.infinum.thimble.models.ServiceAction
import com.infinum.thimble.ui.ThimbleActivity
import com.infinum.thimble.ui.ThimbleService

internal class ThimbleIntentBuilder(
    private val context: Context
) {

    fun settings(): PendingIntent =
        PendingIntent.getActivity(
            context,
            0,
            Intent(context, ThimbleActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    fun reset(): PendingIntent =
        PendingIntent.getService(
            context,
            0,
            Intent(context, ThimbleService::class.java).apply {
                action = ServiceAction.RESET.code
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    fun stop(): PendingIntent =
        PendingIntent.getService(
            context,
            0,
            Intent(context, ThimbleService::class.java).apply {
                action = ServiceAction.STOP.code
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )

    fun toggleGrid(enabled: Boolean): PendingIntent =
        PendingIntent.getService(
            context,
            0,
            Intent(context, ThimbleService::class.java).apply {
                action = ServiceAction.GRID.code
                putExtra(BundleKeys.TOGGLE.name, enabled)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    fun toggleMockup(enabled: Boolean): PendingIntent =
        PendingIntent.getService(
            context,
            0,
            Intent(context, ThimbleService::class.java).apply {
                action = ServiceAction.MOCKUP.code
                putExtra(BundleKeys.TOGGLE.name, enabled)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    fun toggleMagnifier(enabled: Boolean): PendingIntent =
        PendingIntent.getService(
            context,
            0,
            Intent(context, ThimbleService::class.java).apply {
                action = ServiceAction.MAGNIFIER.code
                putExtra(BundleKeys.TOGGLE.name, enabled)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    fun toggleRecorder(enabled: Boolean): PendingIntent =
        PendingIntent.getService(
            context,
            0,
            Intent(context, ThimbleService::class.java).apply {
                action = ServiceAction.RECORDER.code
                putExtra(BundleKeys.TOGGLE.name, enabled)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )
}
