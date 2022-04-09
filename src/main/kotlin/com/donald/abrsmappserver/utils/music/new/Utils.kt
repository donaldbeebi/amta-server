package com.donald.abrsmappserver.utils.music.new

import java.lang.Math.floorMod
import kotlin.math.abs

typealias Alter = Int
typealias Octave = Int
typealias Ordinal = Int
typealias IntervalNumber = Int
typealias IntervalFifths = Int
typealias PitchFifthsValue = Int

/*
 * DATA
 */

const val PITCH_TABLE_COLUMN_COUNT = 7
const val PITCH_TABLE_ROW_COUNT = 5

val PITCH_FIFTHS_VALUE_RANGE = 0..35
val ALTER_RANGE = -2..2
val INTERVAL_FIFTHS_RANGE = -12..12

val EXCLUDED_INTERVALS = hashSetOf(
    com.donald.abrsmappserver.utils.music.Interval.DIM_U,
    com.donald.abrsmappserver.utils.music.Interval.PER_U,
    com.donald.abrsmappserver.utils.music.Interval.AUG_U,
    com.donald.abrsmappserver.utils.music.Interval.DIM_2,
    com.donald.abrsmappserver.utils.music.Interval.DIM_6,
    com.donald.abrsmappserver.utils.music.Interval.DIM_9,
    com.donald.abrsmappserver.utils.music.Interval.DIM_13
)

val com.donald.abrsmappserver.utils.music.Interval.isTested: Boolean
    get() = this !in EXCLUDED_INTERVALS

val IntRange.size: Int
    get() = last - first

fun PitchFifthsValue.asPitchFifthsValueToLetter(): Letter {
    if (isInvalidPitchFifthsValue) throwInvalidPitchFifthsValue()
    return Letter.fromLetterFifths(floorMod(this, Letter.cardinality))
}

fun PitchFifthsValue.asPitchFifthsValueToAlter(): Alter {
    if (isInvalidPitchFifthsValue) throwInvalidPitchFifthsValue()
    return (floorMod(this, PITCH_FIFTHS_VALUE_RANGE.size) / Letter.cardinality) - (ALTER_RANGE.size / 2)
}

fun PitchFifthsValue.asPitchFifthsValueToOrdinal(octave: Int = 0): Ordinal {
    if (isInvalidPitchFifthsValue) throwInvalidPitchFifthsValue()
    return this.asPitchFifthsValueToLetter().step * ALTER_RANGE.size +
            this.asPitchFifthsValueToAlter() + ALTER_RANGE.size / 2 +
            octave * PITCH_FIFTHS_VALUE_RANGE.size
}

fun pitchFifthsValue(letter: Letter, alterValue: Int): Int {
    if (alterValue.isInvalidAlterValue) throwInvalidAlterValue()
    return letter.fifths + (alterValue + ALTER_RANGE.size / 2) * PITCH_TABLE_COLUMN_COUNT
}

fun intervalNumber(fifths: IntervalFifths, octaves: Int): IntervalNumber {
    return floorMod(fifths * 4, 7) + (octaves * 7) + 1
}

fun intervalQuality(fifths: Int): Interval.Quality {
    val simpleNumber = intervalNumber(fifths, 0)
    val qualityIndex = (fifths + INTERVAL_FIFTHS_RANGE.size / 2) / 7
    return when (simpleNumber) {
        2, 3, 6, 7 -> Interval.Quality.imperfQualities[qualityIndex]
        1, 4, 5 -> Interval.Quality.perfQualities[qualityIndex]
        else -> throw IllegalStateException("Illegal number $simpleNumber")
    }
}

/*
 * EXTENSIONS
 */

val Int.isValidPitchFifthsValue: Boolean
    get() = this in PITCH_FIFTHS_VALUE_RANGE

val Int.isInvalidPitchFifthsValue: Boolean
    get() = this !in PITCH_FIFTHS_VALUE_RANGE

val Int.isValidAlterValue: Boolean
    get() = this in ALTER_RANGE

val Int.isInvalidAlterValue: Boolean
    get() = this !in ALTER_RANGE

fun Int.asAlterToSymbol(): String {
    if (this.isInvalidAlterValue) throwInvalidAlterValue()
    return when {
        this <  0 -> String(CharArray(abs(this)) { 'b' })
        this == 0 -> ""
        this >  0 -> String(CharArray(this) { '#' })
        else      -> throw IllegalStateException()
    }
}

/*
 * PRIVATE FUNCTIONS
 */

fun throwInvalidPitchFifthsValue(): Nothing {
    throw IllegalArgumentException("Invalid Id")
}

fun throwInvalidAlterValue(): Nothing {
    throw IllegalArgumentException("Invalid Id")
}