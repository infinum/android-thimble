package com.infinum.thimble.builders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.infinum.thimble.R

internal class ThimbleNotificationBuilder(
    private val service: Service
) {

    companion object {
        private const val NOTIFICATION_ID = 555
    }

    private val intentBuilder = ThimbleIntentBuilder(service)

    fun show() {
        NotificationCompat.Builder(
            service,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel(
                    service.getString(R.string.thimble_name),
                    service.getString(R.string.thimble_name)
                )
            } else {
                service.getString(R.string.thimble_name)
            }
        )
            .setSmallIcon(R.drawable.thimble_ic_logo)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentTitle(service.getString(R.string.thimble_name))
            .setColor(ContextCompat.getColor(service, R.color.thimble_color_primary))
            .setColorized(true)
            .setContentIntent(intentBuilder.settings())
            .setDeleteIntent(intentBuilder.stop())
            .addAction(
                NotificationCompat.Action(
                    0,
                    service.getString(R.string.thimble_settings),
                    intentBuilder.settings()
                )
            )
            .addAction(
                NotificationCompat.Action(
                    0,
                    service.getString(R.string.thimble_reset),
                    intentBuilder.reset()
                )
            )
            .addAction(
                NotificationCompat.Action(
                    0,
                    service.getString(R.string.thimble_stop),
                    intentBuilder.stop()
                )
            )
            .build()
            .also {
                service.startForeground(NOTIFICATION_ID, it)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        (service.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.let {
            it.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    lightColor = ContextCompat.getColor(service, R.color.thimble_color_primary)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
            )
        }
        return channelId
    }
}