package com.donald.abrsmappserver.utils.RandomIntegerGenerator

import java.util.*
import kotlin.math.ln
import kotlin.math.pow

//private val MAX_TOTAL = ln(Int.MAX_VALUE.toDouble()).toLong()

data class Combination(val seed: Int, val total: Int) {
    private var current = 0
    fun nextBoolean(): Boolean {
        if (current >= total) throw IllegalStateException()
        val boolean = seed.getBit(current) != 0
        current++
        return boolean
    }
}

fun Int.getBit(position: Int): Int {
    return (this shr position) and 1
}

/*
fun randomCombination(total: Int, trueCount: Int): Combination {
    require(total in 0..MAX_TOTAL && trueCount in 0..total)
    return Combination(Random().nextLong())
}

 */