package com.donald.abrsmappserver.utils.music.new

import com.donald.abrsmappserver.utils.music.Music
import java.lang.Math.floorMod
import kotlin.math.abs
import com.donald.abrsmappserver.utils.music.Letter as OldLetter

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
val AlterRange = -2..2
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

val IntRange.span: Int
    get() = last - first

fun PitchFifthsValue.asPitchFifthsValueToLetter(): Letter {
    if (isInvalidPitchFifthsValue) throwInvalidPitchFifthsValue()
    return Letter.fromLetterFifths(floorMod(this, Letter.cardinality))
}

fun PitchFifthsValue.asPitchFifthsValueToAlter(): Alter {
    if (isInvalidPitchFifthsValue) throwInvalidPitchFifthsValue()
    return (floorMod(this, PITCH_FIFTHS_VALUE_RANGE.span) / Letter.cardinality) - (AlterRange.span / 2)
}

fun PitchFifthsValue.asPitchFifthsValueToOrdinal(octave: Int = 0): Ordinal {
    if (isInvalidPitchFifthsValue) throwInvalidPitchFifthsValue()
    return this.asPitchFifthsValueToLetter().step * AlterRange.span +
            this.asPitchFifthsValueToAlter() + AlterRange.span / 2 +
            octave * PITCH_FIFTHS_VALUE_RANGE.span
}

fun pitchFifthsValue(letter: Letter, alterValue: Int): Int {
    if (alterValue.isInvalidAlterValue) throwInvalidAlterValue()
    return letter.fifths + (alterValue + AlterRange.span / 2) * PITCH_TABLE_COLUMN_COUNT
}

// TODO: FOR OLD CODE ONLY
fun pitchFifthsValue(letter: OldLetter, keyStartPitchFifthsValue: Int): Int {
    if (keyStartPitchFifthsValue.isInvalidPitchFifthsValue) throwInvalidPitchFifthsValue()
    return floorMod(letter.ordinal - keyStartPitchFifthsValue, OldLetter.NO_OF_LETTERS) + keyStartPitchFifthsValue
}

fun intervalNumber(fifths: IntervalFifths, octaves: Int): IntervalNumber {
    return floorMod(fifths * 4, 7) + (octaves * 7) + 1
}

fun intervalQuality(fifths: Int): Interval.Quality {
    val simpleNumber = intervalNumber(fifths, 0)
    val qualityIndex = (fifths + INTERVAL_FIFTHS_RANGE.span / 2) / 7
    return when (simpleNumber) {
        2, 3, 6, 7 -> Interval.Quality.imperfQualities[qualityIndex]
        1, 4, 5 -> Interval.Quality.perfQualities[qualityIndex]
        else -> throw IllegalStateException("Illegal number $simpleNumber")
    }
}

fun relPitchOrdinalRange(staffRange: IntRange): IntRange {
    require(staffRange.first < staffRange.last)
    val lowest = Music.pitchOrdinalFromAbsStep(staffRange.first, -AlterRange.span / 2)
    val highest = Music.pitchOrdinalFromAbsStep(staffRange.last, AlterRange.span / 2)
    return lowest..highest
}

/*
 * EXTENSIONS
 */

val Int.isValidPitchFifthsValue: Boolean
    get() = this in PITCH_FIFTHS_VALUE_RANGE

val Int.isInvalidPitchFifthsValue: Boolean
    get() = this !in PITCH_FIFTHS_VALUE_RANGE

val Int.isValidAlterValue: Boolean
    get() = this in AlterRange

val Int.isInvalidAlterValue: Boolean
    get() = this !in AlterRange

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