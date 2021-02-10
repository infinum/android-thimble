package com.infinum.thimble.ui.overlays.magnifier

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.WindowManager
import com.infinum.thimble.R
import com.infinum.thimble.databinding.ThimbleLayoutMagnifierBinding
import com.infinum.thimble.extensions.half
import com.infinum.thimble.extensions.screenCenter
import com.infinum.thimble.extensions.toHalf
import com.infinum.thimble.models.configuration.MagnifierConfiguration
import com.infinum.thimble.ui.ThimbleApplication
import com.infinum.thimble.ui.ThimbleService
import com.infinum.thimble.ui.overlays.shared.AbstractOverlay
import com.infinum.thimble.ui.utils.BitmapUtils
import com.infinum.thimble.ui.utils.ViewUtils
import com.infinum.thimble.ui.views.magnifier.MagnifierView
import kotlin.math.roundToInt

internal class MagnifierOverlay(
    private val context: Context
) : AbstractOverlay<MagnifierConfiguration>(context) {

    private var configuration: MagnifierConfiguration = MagnifierConfiguration()

    private var view: MagnifierView? = null

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null

    private var magnifierParams: WindowManager.LayoutParams? = null

    private var previewArea: Rect = Rect()

    private val screenCaptureLock = Any()

    private val magnifierWidth: Int =
        context.resources.getDimensionPixelSize(R.dimen.thimble_magnifier_size)
    private val magnifierHeight: Int =
        context.resources.getDimensionPixelSize(R.dimen.thimble_magnifier_size)

    private var previewSampleWidth =
        context.resources.getInteger(R.integer.thimble_magnifier_sample_size)
    private var previewSampleHeight =
        context.resources.getInteger(R.integer.thimble_magnifier_sample_size)

    private var isMoving: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun show() {
        setupMediaProjection()

        magnifierParams = WindowManager.LayoutParams(
            magnifierWidth,
            magnifierHeight,
            ViewUtils.getWindowType(true),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = Gravity.TOP xor Gravity.START
            with(context.screenCenter()) {
                this@apply.x = this.x - magnifierWidth.half()
                this@apply.y = this.y - magnifierHeight.half()
            }
        }

        view = ThimbleLayoutMagnifierBinding.inflate(LayoutInflater.from(context)).root
            .apply {
                setOnTouchListener { view, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            view.alpha = 1.0f.toHalf()
                            isMoving = false
                        }
                        MotionEvent.ACTION_MOVE -> {
                            isMoving = true
                            updateMagnifierView(
                                event.rawX.roundToInt(),
                                event.rawY.roundToInt()
                            )
                        }
                        MotionEvent.ACTION_UP -> {
                            isMoving = false
                            view.alpha = 1.0f
                        }
                    }
                    true
                }
            }
        if (view?.isAttachedToWindow?.not() == true) {
            magnifierParams?.let { windowManager.addView(view, it) }
        }

        showing = true
    }

    override fun hide() {
        view?.let { removeViewIfAttached(it) }
        closeImageReader()
        teardownMediaProjection()

        showing = false
    }

    override fun update(configuration: MagnifierConfiguration) {
        this.configuration = configuration

        view?.setColorValueType(configuration.colorModel)
    }

    override fun reset(configuration: MagnifierConfiguration) {
        this.configuration = configuration
        hide()
        show()
    }

    private fun setupMediaProjection() {
        val size = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getRealSize(size)
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealSize(size)
        }

        /*
        Error: Must be one of:
        ImageFormat.UNKNOWN,
        ImageFormat.RGB_565,
        ImageFormat.YV12,
        ImageFormat.Y8,
        ImageFormat.NV16,
        ImageFormat.NV21,
        ImageFormat.YUY2,
        ImageFormat.JPEG,
        ImageFormat.DEPTH_JPEG,
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888,
        ImageFormat.FLEX_RGB_888,
        ImageFormat.FLEX_RGBA_8888,
        ImageFormat.RAW_SENSOR,
        ImageFormat.RAW_PRIVATE,
        ImageFormat.RAW10,
        ImageFormat.RAW12,
        ImageFormat.DEPTH16,
        ImageFormat.DEPTH_POINT_CLOUD,
        ImageFormat.PRIVATE,
        ImageFormat.HEIC
         */
        imageReader = ImageReader.newInstance(
            size.x,
            size.y,
            ImageFormat.RGB_565,
//            1,
            2
        )
            .also {
                it.setOnImageAvailableListener(
                    this::acquireImage,
                    Handler(Looper.getMainLooper())
                )
            }

        mediaProjection =
            (context.applicationContext as? ThimbleApplication)?.mediaProjectionData()?.let {
                (context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)
                    ?.getMediaProjection(
                        (context.applicationContext as? ThimbleApplication)?.mediaProjectionResultCode()
                            ?: Activity.RESULT_CANCELED,
                        it
                    )
            }
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "${ThimbleService::class.java.simpleName}_${MagnifierOverlay::class.java.simpleName}",
            size.x,
            size.y,
            context.resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null,
            null
        )
    }

    private fun teardownMediaProjection() {
        virtualDisplay?.release()
        mediaProjection?.stop()
    }

    private fun acquireImage(reader: ImageReader?) {
        synchronized(screenCaptureLock) {
            reader?.acquireLatestImage()
                ?.let { image ->
                    if (isMoving) {
                        view?.setPixels(
                            BitmapUtils.screenBitmapRegion(
                                image,
                                previewArea
                            )
                        )
                    }
                    image.close()
                }
        }
    }

    private fun closeImageReader() {
        imageReader?.close()
        imageReader = null
    }

    private fun updateMagnifierView(x: Int, y: Int) {
        updatePreviewArea(x, y)

        magnifierParams?.let {
            it.x = x - magnifierWidth.half()
            it.y = y - magnifierHeight.half()
            windowManager.updateViewLayout(view, it)
        }
    }

    private fun updatePreviewArea(x: Int, y: Int) =
        with(previewArea) {
            left = x - previewSampleWidth.half()
            top = y - previewSampleHeight.half()
            right = x + previewSampleWidth.half() + 1
            bottom = y + previewSampleHeight.half() + 1
        }
}
