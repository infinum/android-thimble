package com.infinum.thimble.ui.overlays.shared

import android.content.Context
import android.view.View
import android.view.WindowManager
import com.infinum.thimble.models.configuration.shared.Configuration

internal abstract class AbstractOverlay<T : Configuration>(
    context: Context
) : Overlay<T> {

    protected var showing: Boolean = false

    protected val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    protected fun removeViewIfAttached(view: View) {
        if (view.isAttachedToWindow) {
            windowManager.removeView(view)
        }
    }

    override fun isShowing() = showing
}
