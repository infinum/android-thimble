package com.infinum.thimble.commanders.service

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import com.infinum.thimble.commanders.shared.AbstractCommander
import com.infinum.thimble.commanders.shared.Command
import com.infinum.thimble.commanders.shared.Target

internal abstract class OverlayCommander(
    outgoingMessenger: Messenger,
    private val replyToMessenger: Messenger
) : AbstractCommander(outgoingMessenger) {

    override var bound: Boolean = false

    fun register() {
        sendMessage(
            Message.obtain(
                null,
                Target.CLIENT.code,
                Command.REGISTER.code,
                0,
                0
            ).apply { replyTo = replyToMessenger }
        )
    }

    protected fun showGrid() {
        sendMessage(
            Message.obtain(
                null,
                Target.GRID.code,
                Command.SHOW.code,
                0
            )
        )
    }

    protected fun hideGrid() {
        sendMessage(
            Message.obtain(
                null,
                Target.GRID.code,
                Command.HIDE.code,
                0
            )
        )
    }

    protected fun updateGrid(parameter: OverlayParameter, params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                Target.GRID.code,
                Command.UPDATE.code,
                parameter.code,
                params
            )
        )
    }

    protected fun showMockup() {
        sendMessage(
            Message.obtain(
                null,
                Target.MOCKUP.code,
                Command.SHOW.code,
                0
            )
        )
    }

    protected fun hideMockup() {
        sendMessage(
            Message.obtain(
                null,
                Target.MOCKUP.code,
                Command.HIDE.code,
                0
            )
        )
    }

    protected fun updateMockup(parameter: OverlayParameter, params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                Target.MOCKUP.code,
                Command.UPDATE.code,
                parameter.code,
                params
            )
        )
    }

    protected fun showMagnifier() {
        sendMessage(
            Message.obtain(
                null,
                Target.MAGNIFIER.code,
                Command.SHOW.code,
                0
            )
        )
    }

    protected fun hideMagnifier() {
        sendMessage(
            Message.obtain(
                null,
                Target.MAGNIFIER.code,
                Command.HIDE.code,
                0
            )
        )
    }

    protected fun updateMagnifier(parameter: OverlayParameter, params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                Target.MAGNIFIER.code,
                Command.UPDATE.code,
                parameter.code,
                params
            )
        )
    }

    protected fun showRecorder() {
        sendMessage(
            Message.obtain(
                null,
                Target.RECORDER.code,
                Command.SHOW.code,
                0
            )
        )
    }

    protected fun hideRecorder() {
        sendMessage(
            Message.obtain(
                null,
                Target.RECORDER.code,
                Command.HIDE.code,
                0
            )
        )
    }

    protected fun updateRecorder(parameter: OverlayParameter, params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                Target.RECORDER.code,
                Command.UPDATE.code,
                parameter.code,
                params
            )
        )
    }

    fun unregister() {
        sendMessage(
            Message.obtain(
                null,
                Target.CLIENT.code,
                Command.UNREGISTER.code,
                0,
                0
            ).apply { replyTo = replyToMessenger }
        )
    }
}
