package com.infinum.thimble

import android.content.Intent

/**
 * Access only class for Thimble UI and features
 */
public object Thimble {

    /**
     * Prepared a predefined Intent to launch Thimble and/or be modified before launching manually.
     */
    @JvmStatic
    public fun launchIntent(): Intent = Intent()

    /**
     * Shows Thimble UI.
     */
    @JvmStatic
    public fun show(): Unit = Unit
}
