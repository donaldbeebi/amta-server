package com.donald.abrsmappserver.utils.RandomIntegerGenerator

import com.donald.abrsmappserver.utils.music.*

class PitchRandom(
    staffRange: IntRange,
    autoReset: Boolean
) {

    private val ordinalSpan = calculateOrdinalSpan(staffRange)

    val random = run {
        val constraint = buildConstraint {
            withRange(0 until ordinalSpan * Clef.Type.values().size)
        }
        DynamicIntRandom(constraint, autoReset)
    }

    //private val random = Random()

    fun generate(): Result {
        val value = random.generate()
        val clefType = Clef.Type.values()[value / ordinalSpan]
        val ordinal = value % ordinalSpan
        return Result(clefType, FreePitch(Music.idFromOrdinal(ordinal)))
    }

    fun generateAndExclude(): Result {
        val value = random.generateAndExclude()
        val clefType = Clef.Type.values()[value / ordinalSpan]
        val ordinal = value % ordinalSpan
        return Result(clefType, FreePitch(Music.idFromOrdinal(ordinal)))
    }

    /*
    fun nextPitch(clefType: Clef.Type): FreePitch {
        val lowerBound = calculatePitchOrdinalLowerBound(clefType)
        val upperBound = calculatePitchOrdinalUpperBound(clefType)
        val ordinal = random.nextInt(upperBound - lowerBound + 1) + lowerBound
        return FreePitch(Music.idFromOrdinal(ordinal))
    }

    fun nextPitch(clefType: Clef.Type, key: Key?): DiatonicPitch {
        val lowerBound = clefType.baseAbsStep() + STAFF_LOWER_BOUND
        val upperBound = clefType.baseAbsStep() + STAFF_UPPER_BOUND
        val absStep = random.nextInt(upperBound - lowerBound + 1) + lowerBound
        return DiatonicPitch(
            key,
            Music.letterFromAbsStep(absStep),
            Music.octaveFromAbsStep(absStep)
        )
    }

     */

    data class Result(val clefType: Clef.Type, val pitch: FreePitch)
}

private fun calculateOrdinalSpan(staffRange: IntRange): Int {
    val lowestPitchOrdinal = Music.pitchOrdinalFromAbsStep(staffRange.first, -Music.ALTER_RANGE / 2)
    val highestPitchOrdinal = Music.pitchOrdinalFromAbsStep(staffRange.last, Music.ALTER_RANGE / 2)

    return highestPitchOrdinal - lowestPitchOrdinal + 1
}