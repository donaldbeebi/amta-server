package com.donald.abrsmappserver.utils.RandomIntegerGenerator

import java.util.*

inline fun randomInt(
    count: Int,
    range: IntRange,
    noinline exclusionPredicate: ((int: Int) -> Boolean)? = null,
    block: (Int) -> Unit
) {
    // initializing
    val excludedInts = TreeSet<Int>()

}

inline fun randomInt(count: Int, range: IntRange, excluded: IntArray, block: (Int) -> Unit) {
    val random = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(range.first)
        .withUpperBound(range.last)
        .excluding(*excluded)
        .build()
    repeat(count) {
        val int = random.nextIntAndExclude()
        block(int)
    }
}

inline fun randomIntIndexed(count: Int, range: IntRange, excluded: IntArray, block: (Int, Int) -> Unit) {
    val random = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(range.first)
        .withUpperBound(range.last)
        .excluding(*excluded)
        .build()
    repeat(count) { index ->
        val int = random.nextIntAndExclude()
        block(index, int)
    }
}


/*
inline fun RandomIntegerGenerator.using(block: RandomIntegerGenerator.() -> Unit) {
    block()
    clearAllExcludedIntegers()
}

 */