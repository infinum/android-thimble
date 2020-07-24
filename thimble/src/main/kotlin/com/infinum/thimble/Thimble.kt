package com.infinum.thimble

import com.infinum.thimble.ui.Presentation

/**
 * Access only class for Thimble UI and features
 */
object Thimble {

    /**
     * Prepared a predefined Intent to launch Thimble and/or be modified before launching manually.
     */
    @JvmStatic
    fun launchIntent() = Presentation.launchIntent()

    /**
     * Shows Thimble UI.
     */
    @JvmStatic
    fun show() = Presentation.show()
}