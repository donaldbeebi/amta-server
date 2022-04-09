package com.donald.abrsmappserver.utils.music

fun Int.toAlphabetUpper(): Char {
    require(this in 0..25)
    return ('A'.code + this).toChar()
}

fun Int.toAlphabetLower(): Char {
    require(this in 0..25)
    return ('a'.code + this).toChar()
}