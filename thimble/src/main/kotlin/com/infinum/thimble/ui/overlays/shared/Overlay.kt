package com.infinum.thimble.ui.overlays.shared

import com.infinum.thimble.models.configuration.shared.Configuration

internal interface Overlay<T : Configuration> {

    fun show()

    fun hide()

    fun isShowing(): Boolean

    fun update(configuration: T)

    fun reset(configuration: T)
}
