package com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom

import com.donald.abrsmappserver.utils.RandomIntegerGenerator.DynamicIntRandom
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.buildConstraint

class Random4<T1, T2, T3, T4>(
    list1: List<T1>,
    list2: List<T2>,
    list3: List<T3>,
    list4: List<T4>,
    autoReset: Boolean
) {
    private val random = run {
        val combinations = list1.size mulExact list2.size mulExact list3.size mulExact list4.size
        val constraint = buildConstraint {
            withRange(0..combinations)
        }
        DynamicIntRandom(constraint, autoReset)
    }

    private infix fun Int.mulExact(int: Int): Int {
        return Math.multiplyExact(this, int)
    }
}