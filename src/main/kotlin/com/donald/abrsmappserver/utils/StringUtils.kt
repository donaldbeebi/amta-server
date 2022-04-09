package com.donald.abrsmappserver.utils

inline fun String.forEachArg(block: (content: String) -> Unit) {
    var argStart = false
    var startIndex = 0
    this.forEachIndexed { index, char ->
        when {
            !argStart && char == '{' -> {
                argStart = true
                startIndex = index
            }
            argStart && char == '}' -> {
                val endIndex = index + 1
                block(this.substring(startIndex + 1, endIndex - 1))
                argStart = false
            }
        }
    }
}