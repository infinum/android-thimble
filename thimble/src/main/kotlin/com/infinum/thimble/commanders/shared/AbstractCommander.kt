package com.infinum.thimble.commanders.shared

import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import timber.log.Timber

internal abstract class AbstractCommander(
    private val outgoingMessenger: Messenger
) {

    abstract var bound: Boolean

    protected fun sendMessage(message: Message) {
        if (bound) {
            try {
                outgoingMessenger.send(message)
            } catch (e: RemoteException) {
                Timber.e(e)
            }
        }
    }
}
