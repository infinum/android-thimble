package com.infinum.thimble

import android.content.Context
import androidx.startup.Initializer
import com.infinum.thimble.ui.Defaults
import com.infinum.thimble.ui.Presentation
import com.infinum.thimble.ui.utils.FileUtils

internal class ThimbleInitializer : Initializer<Class<ThimbleInitializer>> {

    override fun create(context: Context): Class<ThimbleInitializer> {

        Defaults.initialise(context)
        Presentation.initialise(context)
        FileUtils.initialise(context)

        return ThimbleInitializer::class.java
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf()
}
