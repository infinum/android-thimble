package com.infinum.thimble.commanders.service

import android.os.Handler
import android.os.Message
import com.infinum.thimble.commanders.shared.Target

internal class ServiceCommandHandler(
    private val commandListener: ServiceCommandListener
) : Handler() {

    override fun handleMessage(message: Message) {
        Target(message.what)
            ?.let { commandType ->
                when (commandType) {
                    Target.CLIENT -> commandListener.onClientCommand(message)
                    Target.GRID -> commandListener.onGridCommand(message)
                    Target.MOCKUP -> commandListener.onMockupCommand(message)
                    Target.MAGNIFIER -> commandListener.onMagnifierCommand(message)
                    Target.RECORDER -> commandListener.onRecorderCommand(message)
                }
            } ?: super.handleMessage(message)
    }
}