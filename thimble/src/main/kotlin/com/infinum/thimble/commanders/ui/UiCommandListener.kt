package com.infinum.thimble.commanders.ui

import android.os.Bundle
import android.os.Message
import com.infinum.thimble.commanders.shared.Command

internal class UiCommandListener(
    private val onRegister: (Bundle) -> Unit,
    private val onUnregister: (Bundle) -> Unit
) {

    fun onClientCommand(message: Message) =
        Command(message.arg1)
            ?.let { command ->
                when (command) {
                    Command.REGISTER -> onRegister(message.obj as Bundle)
                    Command.UNREGISTER -> onUnregister(message.obj as Bundle)
                    else -> throw NotImplementedError()
                }
            }
}
