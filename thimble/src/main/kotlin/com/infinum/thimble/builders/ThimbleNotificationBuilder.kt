package com.infinum.thimble.builders

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
        buildNotification(
            gridEnabled = false,
            mockupEnabled = false,
            magnifierEnabled = false,
            recorderEnabled = false
        )
            .build()
            .also {
                service.startForeground(NOTIFICATION_ID, it)
            }
    }

    fun update(
        gridEnabled: Boolean,
        mockupEnabled: Boolean,
        magnifierEnabled: Boolean,
        recorderEnabled: Boolean
    ) {
        buildNotification(
            gridEnabled = gridEnabled,
            mockupEnabled = mockupEnabled,
            magnifierEnabled = magnifierEnabled,
            recorderEnabled = recorderEnabled
        )
            .build()
            .also {
                NotificationManagerCompat.from(service).notify(NOTIFICATION_ID, it)
            }
    }

    private fun buildNotification(
        gridEnabled: Boolean,
        mockupEnabled: Boolean,
        magnifierEnabled: Boolean,
        recorderEnabled: Boolean
    ): NotificationCompat.Builder =
        NotificationCompat.Builder(service, channelId())
            .setSmallIcon(R.drawable.thimble_ic_logo)
            .setOngoing(false)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(service, R.color.thimble_color_primary))
            .setColorized(false)
            .setCustomContentView(
                RemoteViews(service.packageName, R.layout.thimble_layout_notification_small)
                    .apply {
                        setOnClickPendingIntent(
                            R.id.gridSwitch,
                            intentBuilder.toggleGrid(gridEnabled.not())
                        )
                        setOnClickPendingIntent(
                            R.id.mockupSwitch,
                            intentBuilder.toggleMockup(mockupEnabled.not())
                        )
                        setOnClickPendingIntent(
                            R.id.magnifierSwitch,
                            intentBuilder.toggleMagnifier(magnifierEnabled.not())
                        )
                        setOnClickPendingIntent(
                            R.id.recorderSwitch,
                            intentBuilder.toggleRecorder(recorderEnabled.not())
                        )
                    }
            )
            .setCustomBigContentView(
                RemoteViews(service.packageName, R.layout.thimble_layout_notification_large)
                    .apply {
                        setOnClickPendingIntent(
                            R.id.gridSwitch,
                            intentBuilder.toggleGrid(gridEnabled.not())
                        )
                        setOnClickPendingIntent(
                            R.id.mockupSwitch,
                            intentBuilder.toggleMockup(mockupEnabled.not())
                        )
                        setOnClickPendingIntent(
                            R.id.magnifierSwitch,
                            intentBuilder.toggleMagnifier(magnifierEnabled.not())
                        )
                        setOnClickPendingIntent(
                            R.id.recorderSwitch,
                            intentBuilder.toggleRecorder(recorderEnabled.not())
                        )
                    }
            )
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setDeleteIntent(intentBuilder.stop())
            .addAction(
                NotificationCompat.Action(
                    R.drawable.thimble_ic_settings,
                    service.getString(R.string.thimble_settings),
                    intentBuilder.settings()
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.thimble_ic_reset,
                    service.getString(R.string.thimble_reset),
                    intentBuilder.reset()
                )
            )
            .addAction(
                NotificationCompat.Action(
                    R.drawable.thimble_ic_quit,
                    service.getString(R.string.thimble_stop),
                    intentBuilder.stop()
                )
            )

    private fun channelId(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(
                service.getString(R.string.thimble_name),
                service.getString(R.string.thimble_name)
            )
        } else {
            service.getString(R.string.thimble_name)
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