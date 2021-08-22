package com.infinum.thimble.ui.views.mockup

import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.util.AttributeSet
import androidx.annotation.FloatRange
import androidx.annotation.RestrictTo
import androidx.appcompat.widget.AppCompatImageView

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class MockupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    companion object {
        private const val DEFAULT_OPACITY = 0.2f
    }

    @FloatRange(from = 0.0, to = 1.0)
    private var opacity: Float = DEFAULT_OPACITY

    private var portraitUri: Uri? = null
    private var landscapeUri: Uri? = null

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        setOpacity()
        setImage()
    }

    fun updateOpacity(@FloatRange(from = 0.0, to = 1.0) opacity: Float) {
        this.opacity = opacity
        setOpacity()
    }

    fun updatePortraitUri(uri: Uri?) {
        this.portraitUri = uri
        setImage()
    }

    fun updateLandscapeUri(uri: Uri?) {
        this.landscapeUri = uri
        setImage()
    }

    private fun setOpacity() {
        alpha = opacity
    }

    private fun setImage() {
        setImageURI(
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                portraitUri
            } else {
                landscapeUri
            }
        )
    }
}
