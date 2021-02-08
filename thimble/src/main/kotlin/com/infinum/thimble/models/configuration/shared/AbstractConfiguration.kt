package com.infinum.thimble.models.configuration.shared

import android.os.Bundle
import android.os.Parcelable
import androidx.core.os.bundleOf
import com.infinum.thimble.models.BundleKeys

internal abstract class AbstractConfiguration : Configuration, Parcelable {

    abstract val enabled: Boolean

    override fun toBundle(): Bundle =
        bundleOf(BundleKeys.CONFIGURATION.name to this@AbstractConfiguration)
}
