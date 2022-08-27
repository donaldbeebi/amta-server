package com.donald.abrsmappserver.utils

import java.io.Closeable

fun Int.floorMod(divisor: Int) = java.lang.Math.floorMod(this, divisor)

inline fun <R> using(closeable: Closeable, block: () -> R): R {
    val result = block()
    closeable.close()
    return result
}

inline fun <R> using(closeable1: Closeable, closeable2: Closeable, block: () -> R): R {
    val result = block()
    closeable1.close()
    closeable2.close()
    return result
}

inline fun <R> using(closeable1: Closeable, closeable2: Closeable, closeable3: Closeable, block: () -> R): R {
    val result = block()
    closeable1.close()
    closeable2.close()
    closeable3.close()
    return result
}