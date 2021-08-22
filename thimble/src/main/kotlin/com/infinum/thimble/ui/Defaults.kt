package com.infinum.thimble.ui

import android.content.Context
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel
import com.infinum.thimble.R

internal object Defaults {

    private lateinit var context: Context

    fun initialise(context: Context) {
        this.context = context
    }

    @ColorInt
    fun gridHorizontalColor(): Int {
        if (::context.isInitialized.not()) {
            throw NullPointerException("Context not initialized")
        }
        return ContextCompat.getColor(context, R.color.thimble_color_grid_horizontal)
    }

    @ColorInt
    fun gridVerticalColor(): Int {
        if (::context.isInitialized.not()) {
            throw NullPointerException("Context not initialized")
        }
        return ContextCompat.getColor(context, R.color.thimble_color_grid_vertical)
    }

    @Suppress("MagicNumber")
    fun createShapeAppearanceModel(): ShapeAppearanceModel =
        listOf(
            Pair(
                CornerFamily.ROUNDED,
                0.0f
            ),
            Pair(
                CornerFamily.CUT,
                context.resources.getDimension(R.dimen.thimble_card_corner_radius_primary)
            ),
            Pair(
                CornerFamily.ROUNDED,
                context.resources.getDimension(R.dimen.thimble_card_corner_radius_primary)
            ),
            Pair(
                CornerFamily.ROUNDED,
                context.resources.getDimension(R.dimen.thimble_card_corner_radius_primary)
            )
        )
//            .shuffled()
            .let {
                ShapeAppearanceModel.builder()
                    .setTopLeftCorner(
                        it[0].first,
                        it[0].second
                    )
                    .setTopRightCorner(
                        it[1].first,
                        it[1].second
                    )
                    .setBottomLeftCorner(
                        it[2].first,
                        it[2].second
                    )
                    .setBottomRightCorner(
                        it[3].first,
                        it[3].second
                    )
                    .build()
            }
}
