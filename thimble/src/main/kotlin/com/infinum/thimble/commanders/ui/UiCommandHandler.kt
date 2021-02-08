package com.infinum.thimble.commanders.ui

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.infinum.thimble.commanders.shared.Target

internal class UiCommandHandler(
    private val commandListener: UiCommandListener
) : Handler(Looper.getMainLooper()) {

    override fun handleMessage(message: Message) {
        Target(message.what)
            ?.let { commandType ->
                when (commandType) {
                    Target.CLIENT -> commandListener.onClientCommand(message)
                    else -> super.handleMessage(message)
                }
            } ?: super.handleMessage(message)
    }
}
