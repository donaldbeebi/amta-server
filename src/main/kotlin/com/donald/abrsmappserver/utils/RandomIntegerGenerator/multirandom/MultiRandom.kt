package com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom

import com.donald.abrsmappserver.utils.RandomIntegerGenerator.DynamicIntRandom
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.buildConstraint

class Random1<T>(
    private val list: List<T>,
    autoReset: Boolean
) {

    val combinationCount: Int
        get() = list.size

    private val random = run {
        val constraint = buildConstraint {
            withRange(0 until combinationCount)
        }
        DynamicIntRandom(constraint, autoReset)
    }

    val hasNext: Boolean
        get() = random.hasNext

    fun generate(): T {
        return calculateResult(random.generate())
    }

    fun generateAndExclude(): T {
        return calculateResult(random.generateAndExclude())
    }

    private fun calculateResult(ordinal: Int): T {
        return list[ordinal]
    }

}

class Random2<T1, T2>(
    private val list1: List<T1>,
    private val list2: List<T2>,
    autoReset: Boolean
) {

    val combinationCount: Int
        get() = list1.size mulExact list2.size

    private val random = run {
        val constraint = buildConstraint {
            withRange(0 until combinationCount)
        }
        DynamicIntRandom(constraint, autoReset)
    }

    val hasNext: Boolean
        get() = random.hasNext

    fun generate(): Result<T1, T2> {
        return calculateResult(random.generate())
    }

    fun generateAndExclude(): Result<T1, T2> {
        return calculateResult(random.generateAndExclude())
    }

    private fun calculateResult(ordinal: Int): Result<T1, T2> {
        val value1 = list1[ordinal % list1.size]
        val value2 = list2[(ordinal / list1.size) /* % list2.size */]
        return Result(value1, value2)
    }

    data class Result<T1, T2>(val value1: T1, val value2: T2)

}

class Random3<T1, T2, T3>(
    private val list1: List<T1>,
    private val list2: List<T2>,
    private val list3: List<T3>,
    autoReset: Boolean
) {

    val combinationCount: Int
        get() = list1.size mulExact list2.size mulExact list3.size

    private val random = run {
        val constraint = buildConstraint {
            withRange(0 until combinationCount)
        }
        DynamicIntRandom(constraint, autoReset)
    }

    val hasNext: Boolean
        get() = random.hasNext

    fun generate(): Result<T1, T2, T3> {
        return calculateResult(random.generate())
    }

    fun generateAndExclude(): Result<T1, T2, T3> {
        return calculateResult(random.generateAndExclude())
    }

    private fun calculateResult(ordinal: Int): Result<T1, T2, T3> {
        val value1 = list1[ordinal % list1.size]
        val value2 = list2[(ordinal / list1.size) % list2.size]
        val value3 = list3[((ordinal / list1.size) / list2.size) /* % list3.size */]
        return Result(value1, value2, value3)
    }

    data class Result<T1, T2, T3>(val value1: T1, val value2: T2, val value3: T3)

}

class Random4<T1, T2, T3, T4>(
    private val list1: List<T1>,
    private val list2: List<T2>,
    private val list3: List<T3>,
    private val list4: List<T4>,
    autoReset: Boolean
) {

    val combinationCount: Int
        get() = list1.size mulExact list2.size mulExact list3.size mulExact list4.size

    private val random = run {
        val constraint = buildConstraint {
            withRange(0 until combinationCount)
        }
        DynamicIntRandom(constraint, autoReset)
    }

    val hasNext: Boolean
        get() = random.hasNext

    fun generate(): Result<T1, T2, T3, T4> {
        return calculateResult(random.generate())
    }

    fun generateAndExclude(): Result<T1, T2, T3, T4> {
        return calculateResult(random.generateAndExclude())
    }

    private fun calculateResult(ordinal: Int): Result<T1, T2, T3, T4> {
        val value1 = list1[ordinal % list1.size]
        val value2 = list2[(ordinal / list1.size) % list2.size]
        val value3 = list3[((ordinal / list1.size) / list2.size) % list3.size]
        val value4 = list4[(((ordinal / list1.size) / list2.size) / list3.size) /* % list4.size */]
        return Result(value1, value2, value3, value4)
    }

    data class Result<T1, T2, T3, T4>(val value1: T1, val value2: T2, val value3: T3, val value4: T4)

}

class Random5<T1, T2, T3, T4, T5>(
    private val list1: List<T1>,
    private val list2: List<T2>,
    private val list3: List<T3>,
    private val list4: List<T4>,
    private val list5: List<T5>,
    autoReset: Boolean
) {

    val combinationCount: Int
        get() = list1.size mulExact list2.size mulExact list3.size mulExact list4.size mulExact list5.size

    private val random = run {
        val constraint = buildConstraint {
            withRange(0 until combinationCount)
        }
        DynamicIntRandom(constraint, autoReset)
    }

    val hasNext: Boolean
        get() = random.hasNext

    fun generate(): Result<T1, T2, T3, T4, T5> {
        return calculateResult(random.generate())
    }

    fun generateAndExclude(): Result<T1, T2, T3, T4, T5> {
        return calculateResult(random.generateAndExclude())
    }

    private fun calculateResult(ordinal: Int): Result<T1, T2, T3, T4, T5> {
        val value1 = list1[ordinal % list1.size]
        val value2 = list2[(ordinal / list1.size) % list2.size]
        val value3 = list3[((ordinal / list1.size) / list2.size) % list3.size]
        val value4 = list4[(((ordinal / list1.size) / list2.size) / list3.size) % list4.size]
        val value5 = list5[((((ordinal / list1.size) / list2.size) / list3. size) / list4.size) /* %list5.size */]
        return Result(value1, value2, value3, value4, value5)
    }

    data class Result<T1, T2, T3, T4, T5>(val value1: T1, val value2: T2, val value3: T3, val value4: T4, val value5: T5)

}

private infix fun Int.mulExact(int: Int): Int {
    return Math.multiplyExact(this, int)
}