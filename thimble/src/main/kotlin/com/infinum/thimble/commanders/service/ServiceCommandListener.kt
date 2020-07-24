package com.infinum.thimble.commanders.service

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import com.infinum.thimble.commanders.shared.Command

internal class ServiceCommandListener(
    private val onRegister: (Messenger) -> Unit,
    private val onShowGridOverlay: () -> Unit,
    private val onHideGridOverlay: () -> Unit,
    private val onUpdateGridOverlayHorizontalColor: (Bundle) -> Unit,
    private val onUpdateGridOverlayVerticalColor: (Bundle) -> Unit,
    private val onUpdateGridOverlayHorizontalGap: (Bundle) -> Unit,
    private val onUpdateGridOverlayVerticalGap: (Bundle) -> Unit,
    private val onShowMockupOverlay: () -> Unit,
    private val onHideMockupOverlay: () -> Unit,
    private val onUpdateMockupOverlayOpacity: (Bundle) -> Unit,
    private val onUpdateMockupOverlayPortraitUri: (Bundle) -> Unit,
    private val onUpdateMockupOverlayLandscapeUri: (Bundle) -> Unit,
    private val onShowMagnifierOverlay: () -> Unit,
    private val onHideMagnifierOverlay: () -> Unit,
    private val onUpdateMagnifierOverlayColorModel: (Bundle) -> Unit,
    private val onUnregister: () -> Unit
) {

    fun onClientCommand(message: Message) =
        Command(message.arg1)
            ?.let { command ->
                when (command) {
                    Command.REGISTER -> onRegister(message.replyTo)
                    Command.UNREGISTER -> onUnregister()
                    else -> throw NotImplementedError()
                }
            }

    fun onGridCommand(message: Message) =
        Command(message.arg1)
            ?.let { command ->
                when (command) {
                    Command.SHOW -> onShowGridOverlay()
                    Command.HIDE -> onHideGridOverlay()
                    Command.UPDATE -> {
                        OverlayParameter(
                            message.arg2
                        )?.let { parameter ->
                            when (parameter) {
                                OverlayParameter.COLOR_HORIZONTAL ->
                                    onUpdateGridOverlayHorizontalColor(message.obj as Bundle)
                                OverlayParameter.COLOR_VERTICAL ->
                                    onUpdateGridOverlayVerticalColor(message.obj as Bundle)
                                OverlayParameter.GAP_HORIZONTAL ->
                                    onUpdateGridOverlayHorizontalGap(message.obj as Bundle)
                                OverlayParameter.GAP_VERTICAL ->
                                    onUpdateGridOverlayVerticalGap(message.obj as Bundle)
                                else -> throw NotImplementedError()
                            }
                        }
                    }
                    else -> throw NotImplementedError()
                }
            }

    fun onMockupCommand(message: Message) =
        Command(message.arg1)
            ?.let { command ->
                when (command) {
                    Command.SHOW -> onShowMockupOverlay()
                    Command.HIDE -> onHideMockupOverlay()
                    Command.UPDATE -> {
                        OverlayParameter(
                            message.arg2
                        )?.let { parameter ->
                            when (parameter) {
                                OverlayParameter.OPACITY ->
                                    onUpdateMockupOverlayOpacity(message.obj as Bundle)
                                OverlayParameter.URI_PORTRAIT ->
                                    onUpdateMockupOverlayPortraitUri(message.obj as Bundle)
                                OverlayParameter.URI_LANDSCAPE ->
                                    onUpdateMockupOverlayLandscapeUri(message.obj as Bundle)
                                else -> throw NotImplementedError()
                            }
                        }
                    }
                    else -> throw NotImplementedError()
                }
            }

    fun onMagnifierCommand(message: Message) =
        Command(message.arg1)
            ?.let { command ->
                when (command) {
                    Command.SHOW -> onShowMagnifierOverlay()
                    Command.HIDE -> onHideMagnifierOverlay()
                    Command.UPDATE -> {
                        OverlayParameter(
                            message.arg2
                        )?.let { parameter ->
                            when (parameter) {
                                OverlayParameter.COLOR_MODEL ->
                                    onUpdateMagnifierOverlayColorModel(message.obj as Bundle)
                                else -> throw NotImplementedError()
                            }
                        }
                    }
                    else -> throw NotImplementedError()
                }
            }
}
