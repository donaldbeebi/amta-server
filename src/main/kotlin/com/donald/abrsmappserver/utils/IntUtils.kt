package com.donald.abrsmappserver.utils.music

fun Int.toAlphabetUpperFromZero(): Char {
    require(this in 0..25)
    return ('A'.code + this).toChar()
}

fun Int.toAlphabetUpperFromOne(): Char {
    require(this in 1..26)
    return ('A'.code + this - 1).toChar()
}


fun Int.toAlphabetLowerFromOne(): Char {
    require(this in 1..26)
    return ('a'.code + this - 1).toChar()
}

infix fun Double.fmod(other: Double) = ((this % other) + other) % other

infix fun Int.fmod(other: Int) = ((this % other) + other) % other

infix fun Double.fmod(other: Int) = ((this % other) + other) % other

infix fun Int.fmod(other: Double) = ((this % other) + other) % other