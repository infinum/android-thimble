package com.infinum.thimble.ui.views.recorder

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.RestrictTo
import androidx.core.view.isGone
import com.infinum.thimble.R
import com.infinum.thimble.databinding.ThimbleLayoutRecorderBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class RecorderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var viewBinding: ThimbleLayoutRecorderBinding

    override fun onFinishInflate() {
        super.onFinishInflate()

        viewBinding = ThimbleLayoutRecorderBinding.bind(this)
    }

    fun updateRecording(recording: Boolean) {
        with(viewBinding) {
            videoButton.setImageResource(
                if (recording) {
                    R.drawable.thimble_ic_stop
                } else {
                    R.drawable.thimble_ic_video
                }
            )
            screenshotButton.isGone = recording
            dragHandle.isGone = recording
        }
    }
}
