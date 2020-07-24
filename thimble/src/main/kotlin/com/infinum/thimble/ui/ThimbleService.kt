package com.infinum.thimble.ui

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.IBinder
import android.os.Messenger
import androidx.annotation.RestrictTo
import androidx.core.os.bundleOf
import com.infinum.thimble.builders.ThimbleNotificationBuilder
import com.infinum.thimble.extensions.toPx
import com.infinum.thimble.commanders.service.ServiceCommandHandler
import com.infinum.thimble.commanders.service.ServiceCommandListener
import com.infinum.thimble.commanders.ui.UiCommander
import com.infinum.thimble.models.ColorModel
import com.infinum.thimble.models.ServiceAction
import com.infinum.thimble.models.BundleKeys
import com.infinum.thimble.models.configuration.ThimbleConfiguration
import com.infinum.thimble.ui.overlays.grid.GridOverlay
import com.infinum.thimble.ui.overlays.magnifier.MagnifierOverlay
import com.infinum.thimble.ui.overlays.mockup.MockupOverlay

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ThimbleService : Service() {

    private var commander: UiCommander? = null

    private var configuration = ThimbleConfiguration()

    private lateinit var gridOverlay: GridOverlay
    private lateinit var mockupOverlay: MockupOverlay
    private lateinit var magnifierOverlay: MagnifierOverlay

    private var isRunning: Boolean = false

    override fun onCreate() {
        super.onCreate()

        gridOverlay = GridOverlay(this)
        mockupOverlay =
            MockupOverlay(this)
        magnifierOverlay =
            MagnifierOverlay(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            ServiceAction(it)?.let { action ->
                when (action) {
                    ServiceAction.START -> startService()
                    ServiceAction.STOP -> stopService()
                    ServiceAction.RESET -> resetOverlays()
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
                    },
                    onHideGridOverlay = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(enabled = false)
                        )
                        gridOverlay.hide()
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
                    },
                    onHideMockupOverlay = {
                        configuration = configuration.copy(
                            mockup = configuration.mockup.copy(enabled = false)
                        )
                        mockupOverlay.hide()
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
                    },
                    onHideMagnifierOverlay = {
                        configuration = configuration.copy(
                            magnifier = configuration.magnifier.copy(enabled = false)
                        )
                        magnifierOverlay.hide()
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
    }

    private fun startService() {
        configuration = configuration.copy(enabled = true)

        if (isRunning) {
            return
        }

        ThimbleNotificationBuilder(this).also { it.show() }

        isRunning = true
    }

    @Suppress("TooGenericExceptionCaught")
    private fun stopService() {
        gridOverlay.hide()
        mockupOverlay.hide()
        magnifierOverlay.hide()

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
                horizontalLineColor = Color.RED,
                verticalLineColor = Color.BLUE,
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
            magnifier = configuration.magnifier.copy(enabled = false)
        )

        commander?.notifyUnregister(
            bundleOf(BundleKeys.CONFIGURATION.name to configuration)
        )
        commander?.bound = false
        commander = null
    }
}