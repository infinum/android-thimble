package com.infinum.thimble.ui.overlays.grid

import android.content.Context
import android.graphics.PixelFormat
import android.view.ViewGroup
import android.view.WindowManager
import com.infinum.thimble.models.configuration.GridConfiguration
import com.infinum.thimble.ui.overlays.shared.AbstractOverlay
import com.infinum.thimble.ui.utils.ViewUtils
import com.infinum.thimble.ui.views.grid.GridView

internal class GridOverlay(
    private val context: Context
) : AbstractOverlay<GridConfiguration>(context) {

    private var configuration: GridConfiguration = GridConfiguration()

    private var view: GridView? = null

    override fun show() {
        view = GridView(context)
            .also {
                windowManager.addView(
                    it,
                    WindowManager.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewUtils.getWindowType(),
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                        PixelFormat.TRANSLUCENT
                    )
                )
                it.updateHorizontalColor(configuration.horizontalLineColor)
                it.updateVerticalColor(configuration.verticalLineColor)
                it.updateHorizontalSize(configuration.horizontalGridSize)
                it.updateVerticalSize(configuration.verticalGridSize)
            }

        showing = true
    }

    override fun hide() {
        view?.let { removeViewIfAttached(it) }
        view = null

        showing = false
    }

    override fun update(configuration: GridConfiguration) {
        this.configuration = configuration

        view?.let {
            it.updateHorizontalColor(configuration.horizontalLineColor)
            it.updateVerticalColor(configuration.verticalLineColor)
            it.updateHorizontalSize(configuration.horizontalGridSize)
            it.updateVerticalSize(configuration.verticalGridSize)
        }
    }

    override fun reset(configuration: GridConfiguration) {
        this.configuration = configuration
        hide()
        show()
    }
}