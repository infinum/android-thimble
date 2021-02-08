package com.infinum.thimble.commanders.ui

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import com.infinum.thimble.commanders.shared.AbstractCommander
import com.infinum.thimble.commanders.shared.Command
import com.infinum.thimble.commanders.shared.Target

internal class UiCommander(
    clientMessenger: Messenger
) : AbstractCommander(clientMessenger) {

    override var bound: Boolean = false

    fun notifyRegister(params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                Target.CLIENT.code,
                Command.REGISTER.code,
                0,
                params
            )
        )
    }

    fun notifyUnregister(params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                Target.CLIENT.code,
                Command.UNREGISTER.code,
                0,
                params
            )
        )
    }

    fun notifySelfStop(params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                Target.CLIENT.code,
                Command.UPDATE.code,
                0,
                params
            )
        )
    }
}
