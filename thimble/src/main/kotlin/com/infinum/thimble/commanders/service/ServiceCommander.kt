package com.infinum.thimble.commanders.service

import android.os.Bundle
import android.os.Messenger

internal class ServiceCommander(
    outgoingMessenger: Messenger,
    replyToMessenger: Messenger
) : OverlayCommander(outgoingMessenger, replyToMessenger) {

    fun toggleGrid(shouldShow: Boolean) {
        if (shouldShow) {
            showGrid()
        } else {
            hideGrid()
        }
    }

    fun toggleMockup(shouldShow: Boolean) {
        if (shouldShow) {
            showMockup()
        } else {
            hideMockup()
        }
    }

    fun toggleMagnifier(shouldShow: Boolean) {
        if (shouldShow) {
            showMagnifier()
        } else {
            hideMagnifier()
        }
    }

    fun toggleRecorder(shouldShow: Boolean) {
        if (shouldShow) {
            showRecorder()
        } else {
            hideRecorder()
        }
    }

    fun updateGridHorizontalColor(params: Bundle) =
        updateGrid(OverlayParameter.COLOR_HORIZONTAL, params)

    fun updateGridVerticalColor(params: Bundle) =
        updateGrid(OverlayParameter.COLOR_VERTICAL, params)

    fun updateGridHorizontalGap(params: Bundle) =
        updateGrid(OverlayParameter.GAP_HORIZONTAL, params)

    fun updateGridVerticalGap(params: Bundle) =
        updateGrid(OverlayParameter.GAP_VERTICAL, params)

    fun updateMockupOpacity(params: Bundle) =
        updateMockup(OverlayParameter.OPACITY, params)

    fun updateMockupPortraitUri(params: Bundle) =
        updateMockup(OverlayParameter.URI_PORTRAIT, params)

    fun updateMockupLandscapeUri(params: Bundle) =
        updateMockup(OverlayParameter.URI_LANDSCAPE, params)

    fun updateMagnifierColorModel(params: Bundle) =
        updateMagnifier(OverlayParameter.COLOR_MODEL, params)

    fun updateRecorderDelay(params: Bundle) =
        updateRecorder(OverlayParameter.RECORDER_DELAY, params)

    fun updateScreenshotCompression(params: Bundle) =
        updateRecorder(OverlayParameter.SCREENSHOT_COMPRESSION, params)

    fun updateRecorderAudio(params: Bundle) =
        updateRecorder(OverlayParameter.RECORDER_AUDIO, params)

    fun updateVideoQuality(params: Bundle) =
        updateRecorder(OverlayParameter.VIDEO_QUALITY, params)
}