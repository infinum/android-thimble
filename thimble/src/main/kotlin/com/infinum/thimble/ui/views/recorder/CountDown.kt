package com.infinum.thimble.ui.views.recorder

import android.os.CountDownTimer
import kotlin.math.roundToInt

internal class CountDown(
    delay: Int,
    private val onStep: (Int) -> Unit,
    private val onDone: () -> Unit
) : CountDownTimer(delay * INTERVAL, INTERVAL) {

    companion object {
        private const val INTERVAL = 1000L
    }

    init {
        start()
    }

    override fun onTick(millisUntilFinished: Long) =
        onStep((millisUntilFinished / INTERVAL.toFloat()).roundToInt())

    override fun onFinish() =
        onDone()
}