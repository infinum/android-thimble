package com.infinum.thimble.extensions

import android.content.res.Resources
import kotlin.math.roundToInt

internal fun Float.toPx(): Int =
    (this * Resources.getSystem().displayMetrics.density).roundToInt()

internal fun Float.toHalf(): Float =
    this / 2.0f

internal fun Float.fromPercentage(): Float =
    this / 100.0f

internal fun Float.toPercentage(multiplier: Int = 100): String =
    "${(this * multiplier).roundToInt()}%"