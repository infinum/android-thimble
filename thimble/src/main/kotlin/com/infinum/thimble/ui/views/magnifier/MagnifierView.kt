package com.infinum.thimble.ui.views.magnifier

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RestrictTo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.infinum.thimble.R
import com.infinum.thimble.databinding.ThimbleLayoutMagnifierBinding
import com.infinum.thimble.extensions.getHSVCode
import com.infinum.thimble.extensions.getHexCode
import com.infinum.thimble.extensions.getRGBCode
import com.infinum.thimble.extensions.half
import com.infinum.thimble.extensions.orZero
import com.infinum.thimble.models.ColorModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class MagnifierView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val clipboardManager =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    private lateinit var viewBinding: ThimbleLayoutMagnifierBinding

    private var centerPixelColor = 0

    private val previewSize =
        resources.getDimensionPixelSize(R.dimen.thimble_magnifier_content_size)

    private var lensDrawable: Drawable? = null

    private var currentBitmap: Bitmap? = null

    private var coloValueType: ColorModel = ColorModel.HEX

    private val bitmapPaint: Paint = Paint().apply {
        isAntiAlias = false
        isDither = true
        isFilterBitmap = false
    }

    @Suppress("MagicNumber")
    private val gridPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(
            context,
            R.color.thimble_magnifier_content
        )
        alpha = 128
        style = Paint.Style.STROKE
        strokeWidth = 1f * resources.displayMetrics.density
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
    }

    private val outlinePaint: Paint = Paint().apply {
        color = ContextCompat.getColor(
            context,
            R.color.thimble_magnifier_content
        )
        style = Paint.Style.STROKE
        strokeWidth = 2f * resources.displayMetrics.density
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
    }

    private val insets: Point = Point(
        resources.getDimensionPixelSize(R.dimen.thimble_magnifier_content_inset),
        resources.getDimensionPixelSize(R.dimen.thimble_magnifier_content_inset)
    )

    private val previewRect: Rect = Rect(
        insets.x,
        insets.y,
        insets.x + previewSize,
        insets.y + previewSize
    )

    private val previewClipPath: Path = Path().apply {
        addCircle(
            previewRect.exactCenterX(),
            previewRect.exactCenterY(),
            previewSize.half().toFloat(),
            Path.Direction.CCW
        )
    }

    private var pixelOutline: RectF? = null

    private var sourceRect: Rect? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        viewBinding = ThimbleLayoutMagnifierBinding.bind(this)

        with(viewBinding) {
            lensDrawable = root.background

            colorValueView.setOnClickListener {
                clipboardManager?.setPrimaryClip(
                    ClipData.newPlainText("color", colorValueView.text)
                )
                Toast.makeText(
                    it.context,
                    R.string.thimble_copied_to_clipboard,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        lensDrawable?.setBounds(0, 0, w, h)
    }

    override fun dispatchDraw(canvas: Canvas) {
        lensDrawable?.draw(canvas)
        currentBitmap?.let {
            with(canvas) {
                clipPath(previewClipPath)
                currentBitmap?.let { drawBitmap(it, sourceRect, previewRect, bitmapPaint) }
                drawGrid(canvas)
                pixelOutline?.let { drawRect(it, outlinePaint) }
            }
        }
        super.dispatchDraw(canvas)
    }

    fun setColorValueType(type: ColorModel) {
        this.coloValueType = type
        with(viewBinding) {
            colorValueView.text = when (coloValueType) {
                ColorModel.HEX -> centerPixelColor.getHexCode()
                ColorModel.RGB -> centerPixelColor.getRGBCode()
                ColorModel.HSV -> centerPixelColor.getHSVCode()
            }
        }
    }

    fun setPixels(pixelsBitmap: Bitmap) {
        if (pixelsBitmap.width == 1 || pixelsBitmap.height == 1) {
            return
        }

        this.currentBitmap = pixelsBitmap

        sourceRect = Rect(0, 0, pixelsBitmap.width, pixelsBitmap.height)

        centerPixelColor = pixelsBitmap.getPixel(
            pixelsBitmap.width.half().coerceAtMost(pixelsBitmap.width),
            pixelsBitmap.height.half().coerceAtMost(pixelsBitmap.height)
        )

        if (pixelOutline == null) {
            val pixelSize = previewRect.width().toFloat() / pixelsBitmap.width
            currentBitmap?.let {
                val x = (it.width - 1).half() * pixelSize
                val y = (it.height - 1).half() * pixelSize
                pixelOutline = RectF(
                    previewRect.left + x,
                    previewRect.top + y, previewRect.left + x + pixelSize,
                    previewRect.top + y + pixelSize
                )
            }
        }

        with(viewBinding) {
            colorValueView.isVisible = currentBitmap != null
            colorValueView.text = when (coloValueType) {
                ColorModel.HEX -> centerPixelColor.getHexCode()
                ColorModel.RGB -> centerPixelColor.getRGBCode()
                ColorModel.HSV -> centerPixelColor.getHSVCode()
            }

            instructionsView.isVisible = currentBitmap == null
        }

        invalidate()
    }

    private fun drawGrid(canvas: Canvas) {
        val stepSize = previewRect.width() / sourceRect?.width().orZero().coerceAtLeast(1).toFloat()
        var x = previewRect.left + stepSize
        while (x <= previewRect.right) {
            canvas.drawLine(
                x,
                previewRect.top.toFloat(),
                x,
                previewRect.bottom.toFloat(),
                gridPaint
            )
            x += stepSize
        }
        var y = previewRect.top + stepSize
        while (y <= previewRect.bottom) {
            canvas.drawLine(
                previewRect.left.toFloat(),
                y,
                previewRect.right.toFloat(),
                y,
                gridPaint
            )
            y += stepSize
        }
    }
}
