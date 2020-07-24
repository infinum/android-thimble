package com.infinum.thimble.ui.overlays.mockup

import android.content.Context
import android.graphics.PixelFormat
import android.net.Uri
import android.view.ViewGroup
import android.view.WindowManager
import com.infinum.thimble.models.configuration.MockupConfiguration
import com.infinum.thimble.ui.overlays.shared.AbstractOverlay
import com.infinum.thimble.ui.utils.ViewUtils
import com.infinum.thimble.ui.views.mockup.MockupView

internal class MockupOverlay(
    private val context: Context
) : AbstractOverlay<MockupConfiguration>(context) {

    private var configuration: MockupConfiguration = MockupConfiguration()

    private var view: MockupView? = null

    override fun show() {
        view = MockupView(context).also {
            it.updateOpacity(configuration.opacity)
            it.updatePortraitUri(
                if (configuration.portraitUri.isNullOrBlank().not()) {
                    Uri.parse(configuration.portraitUri)
                } else {
                    null
                }
            )
            it.updateLandscapeUri(
                if (configuration.landscapeUri.isNullOrBlank().not()) {
                    Uri.parse(configuration.landscapeUri)
                } else {
                    null
                }
            )

            windowManager.addView(
                it,
                WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewUtils.getWindowType(),
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSPARENT
                )
            )
        }

        showing = true
    }

    override fun hide() {
        view?.let { removeViewIfAttached(it) }
        view = null

        showing = false
    }

    override fun update(configuration: MockupConfiguration) {
        this.configuration = configuration

        view?.let {
            it.updateOpacity(configuration.opacity)
            it.updatePortraitUri(
                if (configuration.portraitUri.isNullOrBlank().not()) {
                    Uri.parse(configuration.portraitUri)
                } else {
                    null
                }
            )
            it.updateLandscapeUri(
                if (configuration.landscapeUri.isNullOrBlank().not()) {
                    Uri.parse(configuration.landscapeUri)
                } else {
                    null
                }
            )
        }
    }

    override fun reset(configuration: MockupConfiguration) {
        this.configuration = configuration
        hide()
        show()
    }
}