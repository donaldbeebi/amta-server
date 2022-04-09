package com.donald.abrsmappserver.utils.music.new

import kotlin.math.abs

@Deprecated("Use int instead")
@JvmInline
value class AlterOld(val value: Int) {

    init {
        if (value.isInvalidAlterValue) throwInvalidAlterValue()
    }

    operator fun plus(other: AlterOld) = AlterOld(this.value + other.value)
    operator fun minus(other: AlterOld) = AlterOld(this.value - other.value)
    operator fun times(other: AlterOld) = AlterOld(this.value * other.value)
    operator fun div(other: AlterOld) = AlterOld(this.value / other.value)
    operator fun inc() = AlterOld(this.value + 1)
    operator fun dec() = AlterOld(this.value - 1)
    operator fun compareTo(other: AlterOld): Int = this.value.compareTo(other.value)

    override fun toString(): String {
        return when {
            value <  0 -> String(CharArray(abs(value)) { 'b' })
            value == 0 -> ""
            value >  0 -> String(CharArray(value) { '#' })
            else -> throw IllegalStateException()
        }
    }

}