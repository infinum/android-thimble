package com.infinum.thimble.ui.overlays.recorder

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.Surface
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.infinum.thimble.R
import com.infinum.thimble.databinding.ThimbleLayoutCounterBinding
import com.infinum.thimble.databinding.ThimbleLayoutRecorderBinding
import com.infinum.thimble.extensions.half
import com.infinum.thimble.extensions.screenCenter
import com.infinum.thimble.extensions.toHalf
import com.infinum.thimble.models.configuration.RecorderConfiguration
import com.infinum.thimble.ui.ThimbleApplication
import com.infinum.thimble.ui.ThimbleService
import com.infinum.thimble.ui.overlays.shared.AbstractOverlay
import com.infinum.thimble.ui.utils.BitmapUtils
import com.infinum.thimble.ui.utils.FileUtils
import com.infinum.thimble.ui.utils.ViewUtils
import com.infinum.thimble.ui.views.recorder.CountDown
import com.infinum.thimble.ui.views.recorder.RecorderView
import kotlin.math.roundToInt

internal class RecorderOverlay(
    private val context: Context
) : AbstractOverlay<RecorderConfiguration>(context) {

    private var configuration: RecorderConfiguration = RecorderConfiguration()

    private var view: RecorderView? = null

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var imageReader: ImageReader? = null

    private var recorderParams: WindowManager.LayoutParams? = null

    private val recorderHeight: Int =
        context.resources.getDimensionPixelSize(R.dimen.thimble_recorder_height)

    private val countdownWidth: Int =
        context.resources.getDimensionPixelSize(R.dimen.thimble_recorder_countdown_width)

    private val countdownHeight: Int =
        context.resources.getDimensionPixelSize(R.dimen.thimble_recorder_countdown_height)

    private val screenSize = Point().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getRealSize(this)
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getRealSize(this)
        }
    }

    private val screenCaptureLock = Any()

    private var recording: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun show() {
        setupMediaProjection()

        recorderParams = WindowManager.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            recorderHeight,
            ViewUtils.getWindowType(true),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = Gravity.TOP xor Gravity.START
            with(context.screenCenter()) {
                this@apply.x = context.resources.displayMetrics.widthPixels
                this@apply.y = this.y.half() + recorderHeight
            }
        }

        ThimbleLayoutRecorderBinding.inflate(LayoutInflater.from(context))
            .apply {
                dragHandle.setOnTouchListener { _, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            root.alpha = 1.0f.toHalf()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            updateRecorderView(
                                event.rawX.roundToInt(),
                                event.rawY.roundToInt()
                            )
                        }
                        MotionEvent.ACTION_UP -> {
                            root.alpha = 1.0f
                        }
                    }
                    true
                }
                screenshotButton.setOnClickListener {
                    view?.isGone = true

                    when (configuration.recorderDelay) {
                        0 -> takeScreenshot()
                        else -> CountDown(
                            delay = configuration.recorderDelay,
                            onStep = { onCountDownTick(it) },
                            onDone = { takeScreenshot() }
                        )
                    }
                }
                videoButton.setOnClickListener {
                    if (recording) {
                        stopMediaRecorder()
                    } else {
                        when (configuration.recorderDelay) {
                            0 -> startMediaRecorder()
                            else -> CountDown(
                                delay = configuration.recorderDelay,
                                onStep = { onCountDownTick(it) },
                                onDone = { startMediaRecorder() }
                            )
                        }
                    }
                }
            }
            .also {
                view = it.root
            }

        if (view?.isAttachedToWindow?.not() == true) {
            recorderParams?.let { windowManager.addView(view, it) }
        }

        showing = true
    }

    override fun hide() {
        view?.let { removeViewIfAttached(it) }

        releaseVirtualDisplay()
        teardownMediaProjection()
        teardownMediaRecorder()

        showing = false
    }

    override fun update(configuration: RecorderConfiguration) {
        this.configuration = configuration
        println(configuration.toString())
    }

    override fun reset(configuration: RecorderConfiguration) {
        this.configuration = configuration
        hide()
        show()
    }

    private fun takeScreenshot() {
        setupImageReader()
        releaseVirtualDisplay()
        createVirtualDisplay(imageReader?.surface)
    }

    private fun startMediaRecorder() {

        releaseVirtualDisplay()
        prepareMediaRecorder()
        createVirtualDisplay(mediaRecorder?.surface)

        mediaRecorder?.start()

        recording = true

        view?.updateRecording(recording)
    }

    private fun stopMediaRecorder() {
        mediaRecorder?.let {
            it.stop()
            it.reset()
        }
        releaseVirtualDisplay()

        recording = false

        view?.updateRecording(recording)
    }

    private fun setupMediaProjection() {
        mediaProjection = (context.applicationContext as? ThimbleApplication)
            ?.mediaProjectionData()
            ?.let {
                (context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager)
                    ?.getMediaProjection(
                        (context.applicationContext as? ThimbleApplication)?.mediaProjectionResultCode()
                            ?: Activity.RESULT_CANCELED,
                        it
                    )
            }
    }

    private fun teardownMediaProjection() {
        mediaProjection = null
    }

    private fun createVirtualDisplay(surface: Surface?) {
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "${ThimbleService::class.java.simpleName}_${RecorderOverlay::class.java.simpleName}",
            screenSize.x,
            screenSize.y,
            context.resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            surface,
            null,
            null
        )
    }

    private fun releaseVirtualDisplay() {
        virtualDisplay?.release()
    }

    private fun updateRecorderView(x: Int, y: Int) {
        recorderParams?.let {
            it.x = x - (view?.width?.half() ?: 0)
            it.y = y - (view?.height?.half() ?: 0)
            windowManager.updateViewLayout(view, it)
        }
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
    private fun setupImageReader() {
        imageReader = ImageReader.newInstance(
            screenSize.x,
            screenSize.y,
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
    }

    @Suppress("MagicNumber")
    private fun prepareMediaRecorder() {
        mediaRecorder = MediaRecorder().also {
            it.setOnErrorListener { _, what, extra ->
                println("BOJAN_ ERROR WHAT: $what EXTRA: $extra")
            }
            it.setOnInfoListener { _, what, extra ->
                println("BOJAN_ INFO WHAT: $what EXTRA: $extra")
            }

            if (configuration.recordInternalAudio) {
                it.setAudioSource(MediaRecorder.AudioSource.MIC)
            }
            it.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            it.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            it.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            if (configuration.recordInternalAudio) {
                it.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            }
            it.setVideoEncodingBitRate(configuration.videoQuality.bitrate.toInt())
            it.setVideoFrameRate(30)
            it.setVideoSize(screenSize.x, screenSize.y)
            it.setOutputFile(FileUtils.createVideoFilename())

            it.prepare()
        }
    }

    private fun teardownImageReader() {
        imageReader?.close()
        imageReader = null
    }

    private fun teardownMediaRecorder() {
        mediaRecorder?.release()
    }

    private fun acquireImage(reader: ImageReader?) {
        synchronized(screenCaptureLock) {
            reader?.acquireLatestImage()?.let { image ->
                FileUtils.saveScreenshot(
                    BitmapUtils.compressScreenshot(
                        image,
                        configuration.compression
                    )
                )
                image.close()
            }
            teardownImageReader()
            view?.isVisible = true
        }
    }

    private fun onCountDownTick(tick: Int) {
        println(tick)
        val counterParams = WindowManager.LayoutParams(
            countdownWidth,
            countdownHeight,
            ViewUtils.getWindowType(true),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            this.gravity = Gravity.TOP xor Gravity.START
        }
        val textView = ThimbleLayoutCounterBinding.inflate(LayoutInflater.from(context)).root
            .apply {
                text = tick.toString()
            }
        if (textView.isAttachedToWindow.not()) {
            windowManager.addView(
                textView,
                counterParams
                    .apply {
                        with(context.screenCenter()) {
                            this@apply.x = this.x - countdownWidth.half()
                            this@apply.y = this.y - countdownHeight.half()
                        }
                    }
            )
        }
        textView.animate()
            .setStartDelay(
                context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
            )
            .scaleX(0.0f)
            .scaleY(0.0f)
            .alpha(0.0f)
            .setDuration(
                context.resources.getInteger(android.R.integer.config_mediumAnimTime).toLong()
            )
            .withEndAction {
                removeViewIfAttached(textView)
            }
            .start()
    }
}
