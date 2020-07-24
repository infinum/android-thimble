package com.infinum.thimble.models

internal enum class ServiceAction(val code: String) {
    START(code = "999"),
    STOP(code = "888"),
    RESET(code = "777");

    companion object {

        operator fun invoke(code: String) =
            values().firstOrNull { it.code == code }
    }
}
