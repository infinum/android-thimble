package com.infinum.thimble.extensions

import android.content.Context
import android.graphics.Point

internal fun Context.screenCenter(): Point = Point(
    resources.displayMetrics.widthPixels.half(),
    resources.displayMetrics.heightPixels.half()
)
