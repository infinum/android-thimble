package com.infinum.thimble.models

internal enum class ServiceAction(val code: String) {
    START(code = "999"),
    STOP(code = "888"),
    RESET(code = "777"),
    GRID(code = "666"),
    MOCKUP(code = "555"),
    MAGNIFIER(code = "444"),
    RECORDER(code = "333");

    companion object {

        operator fun invoke(code: String) =
            values().firstOrNull { it.code == code }
    }
}
