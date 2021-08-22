package com.infinum.thimble.ui.views.grid

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.RestrictTo
import com.infinum.thimble.extensions.toPx
import com.infinum.thimble.ui.Defaults

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class GridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    @Px
    private var gridSizeHorizontal = 8.0f.toPx()

    @Px
    private var gridSizeVertical = 8.0f.toPx()

    private var pointsHorizontal: FloatArray = FloatArray(0)
    private var pointsVertical: FloatArray = FloatArray(0)

    private val paintHorizontal = Paint().apply {
        color = Defaults.gridHorizontalColor()
        strokeWidth = 0.0f
        style = Paint.Style.STROKE
    }
    private val paintVertical = Paint().apply {
        color = Defaults.gridVerticalColor()
        strokeWidth = 0.0f
        style = Paint.Style.STROKE
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        updateGrid(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        if (pointsHorizontal.isNotEmpty()) {
            canvas.drawLines(pointsHorizontal, paintHorizontal)
        }
        if (pointsVertical.isNotEmpty()) {
            canvas.drawLines(pointsVertical, paintVertical)
        }
    }

    fun updateHorizontalColor(@ColorInt newColor: Int) {
        paintHorizontal.color = newColor
        invalidate()
    }

    fun updateVerticalColor(@ColorInt newColor: Int) {
        paintVertical.color = newColor
        invalidate()
    }

    fun updateHorizontalSize(@Px newGridSize: Int) {
        gridSizeHorizontal = newGridSize
        updateGrid(width, height)
        invalidate()
    }

    fun updateVerticalSize(@Px newGridSize: Int) {
        gridSizeVertical = newGridSize
        updateGrid(width, height)
        invalidate()
    }

    @Suppress("MagicNumber")
    private fun updateGrid(width: Int, height: Int) {
        val numHorizontalLines = height / gridSizeHorizontal
        val numVerticalLines = width / gridSizeVertical
        val numHorizontalPoints = if (numHorizontalLines > 0) {
            (numHorizontalLines + 1) * 4
        } else {
            0
        }
        val numVerticalPoints = if (numVerticalLines > 0) {
            (numVerticalLines + 1) * 4
        } else {
            0
        }
        if (numHorizontalPoints + numVerticalPoints > 0) {
            pointsHorizontal = FloatArray(numHorizontalPoints)
            pointsVertical = FloatArray(numVerticalPoints)

            // set up horizontal lines
            var gap = gridSizeHorizontal.toFloat()
            for (i in 0..numHorizontalLines) {
                val base = i * 4
                pointsHorizontal[base] = 0f
                pointsHorizontal[base + 1] = gap // + positionShift
                pointsHorizontal[base + 2] = width.toFloat()
                pointsHorizontal[base + 3] = gap // + positionShift
                gap += gridSizeHorizontal
            }

            // set up vertical lines
            gap = gridSizeVertical.toFloat()
            for (i in 0..numVerticalLines) {
                val base = i * 4
                pointsVertical[base] = gap // + positionShift
                pointsVertical[base + 1] = 0f
                pointsVertical[base + 2] = gap // + positionShift
                pointsVertical[base + 3] = height.toFloat()
                gap += gridSizeVertical
            }
        } else {
            pointsHorizontal = FloatArray(0)
            pointsVertical = FloatArray(0)
        }
    }
}
