package com.infinum.thimble.ui

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import android.os.Messenger
import androidx.annotation.RestrictTo
import androidx.core.os.bundleOf
import com.infinum.thimble.builders.ThimbleNotificationBuilder
import com.infinum.thimble.commanders.service.ServiceCommandHandler
import com.infinum.thimble.commanders.service.ServiceCommandListener
import com.infinum.thimble.commanders.ui.UiCommander
import com.infinum.thimble.extensions.toPx
import com.infinum.thimble.models.BundleKeys
import com.infinum.thimble.models.ColorModel
import com.infinum.thimble.models.ServiceAction
import com.infinum.thimble.models.VideoQuality
import com.infinum.thimble.models.configuration.ThimbleConfiguration
import com.infinum.thimble.ui.overlays.grid.GridOverlay
import com.infinum.thimble.ui.overlays.magnifier.MagnifierOverlay
import com.infinum.thimble.ui.overlays.mockup.MockupOverlay
import com.infinum.thimble.ui.overlays.recorder.RecorderOverlay

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ThimbleService : Service() {

    private var commander: UiCommander? = null

    private var configuration = ThimbleConfiguration()

    private lateinit var notifications: ThimbleNotificationBuilder

    private lateinit var gridOverlay: GridOverlay
    private lateinit var mockupOverlay: MockupOverlay
    private lateinit var magnifierOverlay: MagnifierOverlay
    private lateinit var recorderOverlay: RecorderOverlay

    private var isRunning: Boolean = false

    override fun onCreate() {
        super.onCreate()

        notifications = ThimbleNotificationBuilder(this)

        gridOverlay = GridOverlay(this)
        mockupOverlay = MockupOverlay(this)
        magnifierOverlay = MagnifierOverlay(this)
        recorderOverlay = RecorderOverlay(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            ServiceAction(it)?.let { action ->
                when (action) {
                    ServiceAction.START -> startService()
                    ServiceAction.STOP -> stopService()
                    ServiceAction.RESET -> resetOverlays()
                    ServiceAction.GRID -> toggleGrid(
                        intent.getBooleanExtra(BundleKeys.TOGGLE.name, false)
                    )
                    ServiceAction.MOCKUP -> toggleMockup(
                        intent.getBooleanExtra(BundleKeys.TOGGLE.name, false)
                    )
                    ServiceAction.MAGNIFIER -> toggleMagnifier(
                        intent.getBooleanExtra(BundleKeys.TOGGLE.name, false)
                    )
                    ServiceAction.RECORDER -> toggleRecorder(
                        intent.getBooleanExtra(BundleKeys.TOGGLE.name, false)
                    )
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? =
        Messenger(
            ServiceCommandHandler(
                ServiceCommandListener(
                    onRegister = this::register,
                    onShowGridOverlay = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(enabled = true)
                        )
                        gridOverlay.show()
                        updateNotification()
                    },
                    onHideGridOverlay = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(enabled = false)
                        )
                        gridOverlay.hide()
                        updateNotification()
                    },
                    onUpdateGridOverlayHorizontalColor = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(
                                horizontalLineColor = it.getInt(
                                    BundleKeys.HORIZONTAL_LINE_COLOR.name,
                                    configuration.grid.horizontalLineColor
                                )
                            )
                        )
                        gridOverlay.update(configuration.grid)
                    },
                    onUpdateGridOverlayVerticalColor = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(
                                verticalLineColor = it.getInt(
                                    BundleKeys.VERTICAL_LINE_COLOR.name,
                                    configuration.grid.verticalLineColor
                                )
                            )
                        )
                        gridOverlay.update(configuration.grid)
                    },
                    onUpdateGridOverlayHorizontalGap = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(
                                horizontalGridSize = it.getInt(
                                    BundleKeys.HORIZONTAL_GAP_SIZE.name,
                                    configuration.grid.horizontalGridSize
                                )
                            )
                        )
                        gridOverlay.update(configuration.grid)
                    },
                    onUpdateGridOverlayVerticalGap = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(
                                verticalGridSize = it.getInt(
                                    BundleKeys.VERTICAL_GAP_SIZE.name,
                                    configuration.grid.horizontalGridSize
                                )
                            )
                        )
                        gridOverlay.update(configuration.grid)
                    },
                    onShowMockupOverlay = {
                        configuration = configuration.copy(
                            mockup = configuration.mockup.copy(enabled = true)
                        )
                        mockupOverlay.show()
                        updateNotification()
                    },
                    onHideMockupOverlay = {
                        configuration = configuration.copy(
                            mockup = configuration.mockup.copy(enabled = false)
                        )
                        mockupOverlay.hide()
                        updateNotification()
                    },
                    onUpdateMockupOverlayOpacity = {
                        configuration = configuration.copy(
                            mockup = configuration.mockup.copy(
                                opacity = it.getFloat(
                                    BundleKeys.OPACITY.name,
                                    configuration.mockup.opacity
                                )
                            )
                        )
                        mockupOverlay.update(configuration.mockup)
                    },
                    onUpdateMockupOverlayPortraitUri = {
                        configuration = configuration.copy(
                            mockup = configuration.mockup.copy(
                                portraitUri = it.getString(
                                    BundleKeys.PORTRAIT_URI.name,
                                    null
                                )
                            )
                        )
                        mockupOverlay.update(configuration.mockup)
                    },
                    onUpdateMockupOverlayLandscapeUri = {
                        configuration = configuration.copy(
                            mockup = configuration.mockup.copy(
                                landscapeUri = it.getString(
                                    BundleKeys.LANDSCAPE_URI.name,
                                    null
                                )
                            )
                        )
                        mockupOverlay.update(configuration.mockup)
                    },
                    onShowMagnifierOverlay = {
                        configuration = configuration.copy(
                            magnifier = configuration.magnifier.copy(enabled = true)
                        )
                        magnifierOverlay.show()
                        updateNotification()
                    },
                    onHideMagnifierOverlay = {
                        configuration = configuration.copy(
                            magnifier = configuration.magnifier.copy(enabled = false)
                        )
                        magnifierOverlay.hide()
                        updateNotification()
                    },
                    onUpdateMagnifierOverlayColorModel = {
                        configuration = configuration.copy(
                            magnifier = configuration.magnifier.copy(
                                colorModel = ColorModel(
                                    it.getString(
                                        BundleKeys.COLOR_MODEL.name,
                                        configuration.magnifier.colorModel.type
                                    )
                                ) ?: ColorModel.HEX
                            )
                        )
                        magnifierOverlay.update(configuration.magnifier)
                    },
                    onShowRecorderOverlay = {
                        configuration = configuration.copy(
                            recorder = configuration.recorder.copy(enabled = true)
                        )
                        recorderOverlay.show()
                        updateNotification()
                    },
                    onHideRecorderOverlay = {
                        configuration = configuration.copy(
                            recorder = configuration.recorder.copy(enabled = false)
                        )
                        recorderOverlay.hide()
                        updateNotification()
                    },
                    onUpdateRecorderDelay = {
                        configuration = configuration.copy(
                            recorder = configuration.recorder.copy(
                                recorderDelay =
                                it.getInt(
                                    BundleKeys.RECORDER_DELAY.name,
                                    configuration.recorder.recorderDelay
                                )
                            )
                        )
                        recorderOverlay.update(configuration.recorder)
                    },
                    onUpdateRecorderScreenshotCompression = {
                        configuration = configuration.copy(
                            recorder = configuration.recorder.copy(
                                compression = it.getFloat(
                                    BundleKeys.SCREENSHOT_COMPRESSION.name,
                                    configuration.recorder.compression
                                )
                            )
                        )
                        recorderOverlay.update(configuration.recorder)
                    },
                    onUpdateRecorderAudio = {
                        configuration = configuration.copy(
                            recorder = configuration.recorder.copy(
                                recordInternalAudio = it.getBoolean(
                                    BundleKeys.RECORDER_AUDIO.name,
                                    configuration.recorder.recordInternalAudio
                                )
                            )
                        )
                        recorderOverlay.update(configuration.recorder)
                    },
                    onUpdateRecorderVideoQuality = {
                        configuration = configuration.copy(
                            recorder = configuration.recorder.copy(
                                videoQuality = VideoQuality(
                                    it.getLong(
                                        BundleKeys.VIDEO_QUALITY.name,
                                        configuration.recorder.videoQuality.bitrate
                                    )
                                ) ?: VideoQuality.HIGH
                            )
                        )
                        recorderOverlay.update(configuration.recorder)
                    },
                    onUnregister = this::unregister
                )
            )
        ).binder

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (gridOverlay.isShowing()) {
            gridOverlay.hide()
            gridOverlay.show()
        }
        if (magnifierOverlay.isShowing()) {
            magnifierOverlay.hide()
            magnifierOverlay.show()
        }
        if (recorderOverlay.isShowing()) {
            recorderOverlay.hide()
            recorderOverlay.show()
        }
    }

    private fun startService() {
        configuration = configuration.copy(enabled = true)

        if (isRunning) {
            return
        }

        notifications.show()

        isRunning = true
    }

    @Suppress("TooGenericExceptionCaught")
    private fun stopService() {
        gridOverlay.hide()
        mockupOverlay.hide()
        magnifierOverlay.hide()
        recorderOverlay.hide()

        configuration = configuration.copy(
            enabled = false,
            grid = configuration.grid.copy(enabled = false),
            mockup = configuration.mockup.copy(enabled = false),
            magnifier = configuration.magnifier.copy(enabled = false),
            recorder = configuration.recorder.copy(enabled = false)
        )

        commander?.notifySelfStop(
            bundleOf(BundleKeys.CONFIGURATION.name to configuration)
        )

        try {
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isRunning = false
    }

    private fun resetOverlays() {
        configuration = configuration.copy(
            grid = configuration.grid.copy(
                horizontalLineColor = Defaults.gridHorizontalColor(),
                verticalLineColor = Defaults.gridVerticalColor(),
                horizontalGridSize = 8.0f.toPx(),
                verticalGridSize = 8.0f.toPx()
            ),
            mockup = configuration.mockup.copy(
                opacity = 0.2f,
                portraitUri = null,
                landscapeUri = null
            ),
            magnifier = configuration.magnifier.copy(
                colorModel = ColorModel.HEX
            ),
            recorder = configuration.recorder.copy(
                recorderDelay = 0,
                compression = 0.9f,
                recordInternalAudio = false,
                videoQuality = VideoQuality.HIGH
            )
        )
        if (gridOverlay.isShowing()) {
            gridOverlay.reset(configuration.grid)
        }
        if (mockupOverlay.isShowing()) {
            mockupOverlay.reset(configuration.mockup)
        }
        if (magnifierOverlay.isShowing()) {
            magnifierOverlay.reset(configuration.magnifier)
        }
        if (recorderOverlay.isShowing()) {
            recorderOverlay.reset(configuration.recorder)
        }
    }

    private fun toggleGrid(enabled: Boolean) {
        configuration = configuration.copy(
            grid = configuration.grid.copy(enabled = enabled)
        )

        commander?.notifyRegister(
            bundleOf(BundleKeys.CONFIGURATION.name to configuration)
        )

        updateNotification()
    }

    private fun toggleMockup(enabled: Boolean) {
        configuration = configuration.copy(
            mockup = configuration.mockup.copy(enabled = enabled)
        )

        commander?.notifyRegister(
            bundleOf(BundleKeys.CONFIGURATION.name to configuration)
        )

        updateNotification()
    }

    private fun toggleMagnifier(enabled: Boolean) {
        configuration = configuration.copy(
            magnifier = configuration.magnifier.copy(enabled = enabled)
        )

        commander?.notifyRegister(
            bundleOf(BundleKeys.CONFIGURATION.name to configuration)
        )

        updateNotification()
    }

    private fun toggleRecorder(enabled: Boolean) {
        configuration = configuration.copy(
            recorder = configuration.recorder.copy(enabled = enabled)
        )

        commander?.notifyRegister(
            bundleOf(BundleKeys.CONFIGURATION.name to configuration)
        )

        updateNotification()
    }

    private fun register(client: Messenger) {
        this.commander = UiCommander(client)
        commander?.bound = true

        commander?.notifyRegister(
            bundleOf(BundleKeys.CONFIGURATION.name to configuration)
        )
    }

    private fun unregister() {
        configuration = configuration.copy(
            enabled = false,
            grid = configuration.grid.copy(enabled = false),
            mockup = configuration.mockup.copy(enabled = false),
            magnifier = configuration.magnifier.copy(enabled = false),
            recorder = configuration.recorder.copy(enabled = false)
        )

        commander?.notifyUnregister(
            bundleOf(BundleKeys.CONFIGURATION.name to configuration)
        )
        commander?.bound = false
        commander = null
    }

    private fun updateNotification() {
        notifications.update(
            configuration.grid.enabled,
            configuration.mockup.enabled,
            configuration.magnifier.enabled,
            configuration.recorder.enabled
        )
    }
}