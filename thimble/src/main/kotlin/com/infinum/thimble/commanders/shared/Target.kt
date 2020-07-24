package com.infinum.thimble.commanders.shared

internal enum class Target(val code: Int) {
    CLIENT(code = 1),
    GRID(code = 2),
    MOCKUP(code = 3),
    MAGNIFIER(code = 4),
    RECORDER(code = 5);

    companion object {

        operator fun invoke(code: Int) = values().firstOrNull { it.code == code }
    }
}