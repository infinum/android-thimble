package com.infinum.thimble.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.annotation.RestrictTo
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import com.infinum.thimble.commanders.service.ServiceCommander
import com.infinum.thimble.commanders.ui.UiCommandHandler
import com.infinum.thimble.commanders.ui.UiCommandListener
import com.infinum.thimble.models.BundleKeys
import com.infinum.thimble.models.ColorModel
import com.infinum.thimble.models.ServiceAction
import com.infinum.thimble.models.VideoQuality
import com.infinum.thimble.models.configuration.ThimbleConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class ServiceActivity : FragmentActivity() {

    private var commander: ServiceCommander? = null

    private var bound: Boolean = false

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            commander =
                ServiceCommander(
                    Messenger(service),
                    Messenger(
                        UiCommandHandler(
                            UiCommandListener(
                                onRegister = this@ServiceActivity::onRegister,
                                onUnregister = this@ServiceActivity::onUnregister,
                                onSelfStop = this@ServiceActivity::onSelfStop
                            )
                        )
                    )
                )
            bound = true
            commander?.bound = bound

            register()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            bound = false
            commander?.bound = bound
            commander = null
        }
    }

    abstract fun setupUi(configuration: ThimbleConfiguration)

    override fun onStart() {
        super.onStart()
        bindService()
    }

    override fun onStop() {
        super.onStop()
        unbindService()
    }

    protected fun createService() {
        startService()
        bindService()
    }

    protected fun destroyService() {
        unregister()
    }

    fun toggleGrid(shouldShow: Boolean) {
        commander?.toggleGrid(shouldShow)
    }

    fun updateGridHorizontalColor(@ColorInt color: Int) {
        commander?.updateGridHorizontalColor(
            bundleOf(BundleKeys.HORIZONTAL_LINE_COLOR.name to color)
        )
    }

    fun updateGridVerticalColor(@ColorInt color: Int) {
        commander?.updateGridVerticalColor(
            bundleOf(BundleKeys.VERTICAL_LINE_COLOR.name to color)
        )
    }

    fun updateGridHorizontalGap(@Px gap: Int) {
        commander?.updateGridHorizontalGap(
            bundleOf(BundleKeys.HORIZONTAL_GAP_SIZE.name to gap)
        )
    }

    fun updateGridVerticalGap(@Px gap: Int) {
        commander?.updateGridVerticalGap(
            bundleOf(BundleKeys.VERTICAL_GAP_SIZE.name to gap)
        )
    }

    fun toggleMockup(shouldShow: Boolean) {
        commander?.toggleMockup(shouldShow)
    }

    fun updateMockupOpacity(@FloatRange(from = 0.0, to = 1.0) opacity: Float) {
        commander?.updateMockupOpacity(
            bundleOf(BundleKeys.OPACITY.name to opacity)
        )
    }

    fun updateMockupPortraitUri(uri: String?) {
        commander?.updateMockupPortraitUri(
            bundleOf(BundleKeys.PORTRAIT_URI.name to uri)
        )
    }

    fun updateMockupLandscapeUri(uri: String?) {
        commander?.updateMockupLandscapeUri(
            bundleOf(BundleKeys.LANDSCAPE_URI.name to uri)
        )
    }

    fun toggleMagnifier(shouldShow: Boolean) {
        commander?.toggleMagnifier(shouldShow)
    }

    fun updateMagnifierColorModel(colorModel: ColorModel) {
        commander?.updateMagnifierColorModel(
            bundleOf(
                BundleKeys.COLOR_MODEL.name to colorModel.type
            )
        )
    }

    fun toggleRecorder(shouldShow: Boolean) {
        commander?.toggleRecorder(shouldShow)
    }

    fun updateRecorderDelay(@IntRange(from = 0, to = 60) recorderDelay: Int) {
        commander?.updateRecorderDelay(
            bundleOf(
                BundleKeys.RECORDER_DELAY.name to recorderDelay
            )
        )
    }

    fun updateScreenshotCompression(@FloatRange(from = 0.0, to = 1.0) compression: Float) {
        commander?.updateScreenshotCompression(
            bundleOf(BundleKeys.SCREENSHOT_COMPRESSION.name to compression)
        )
    }

    fun updateRecorderAudio(enabled: Boolean) {
        commander?.updateRecorderAudio(
            bundleOf(
                BundleKeys.RECORDER_AUDIO.name to enabled
            )
        )
    }

    fun updateVideoQuality(videoQuality: VideoQuality) {
        commander?.updateVideoQuality(
            bundleOf(
                BundleKeys.VIDEO_QUALITY.name to videoQuality.bitrate
            )
        )
    }

    private fun register() {
        commander?.register()
    }

    private fun unregister() {
        commander?.unregister()
    }

    private fun startService() {
        ContextCompat.startForegroundService(
            this,
            Intent(this, ThimbleService::class.java).apply {
                action = ServiceAction.START.code
            }
        )
    }

    private fun bindService() {
        if (bound.not()) {
            bindService(
                Intent(this, ThimbleService::class.java),
                serviceConnection,
                0
            )
            bound = true
        } else {
            commander?.register()
        }
    }

    private fun stopService() {
        ContextCompat.startForegroundService(
            this,
            Intent(this, ThimbleService::class.java).apply {
                action = ServiceAction.STOP.code
            }
        )
    }

    private fun unbindService() {
        if (bound) {
            unbindService(serviceConnection)
            bound = false
        }
    }

    private fun onRegister(bundle: Bundle) {
        setupUi(bundle.getParcelable(BundleKeys.CONFIGURATION.name) ?: ThimbleConfiguration())
    }

    private fun onUnregister(bundle: Bundle) {
        setupUi(bundle.getParcelable(BundleKeys.CONFIGURATION.name) ?: ThimbleConfiguration())
        unbindService()
        stopService()
    }

    private fun onSelfStop(bundle: Bundle) {
        setupUi(bundle.getParcelable(BundleKeys.CONFIGURATION.name) ?: ThimbleConfiguration())
        unbindService()
    }
}