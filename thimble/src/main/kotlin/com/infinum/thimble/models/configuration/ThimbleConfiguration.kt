package com.infinum.thimble.models.configuration

import com.infinum.thimble.models.configuration.shared.AbstractConfiguration
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ThimbleConfiguration(
    override val enabled: Boolean = false,
    val grid: GridConfiguration = GridConfiguration(),
    val mockup: MockupConfiguration = MockupConfiguration(),
    val magnifier: MagnifierConfiguration = MagnifierConfiguration(),
    val recorder: RecorderConfiguration = RecorderConfiguration()
) : AbstractConfiguration()
