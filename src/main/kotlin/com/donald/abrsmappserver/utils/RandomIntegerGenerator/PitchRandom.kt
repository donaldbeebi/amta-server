package com.donald.abrsmappserver.utils.RandomIntegerGenerator

import com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom.Random3
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.utils.music.new.AlterRange
import com.donald.abrsmappserver.utils.music.new.span
import com.donald.abrsmappserver.utils.range.listUntil
import com.donald.abrsmappserver.utils.range.toRangeList

class ConstrainedPitchRandom(
    staffRange: IntRange,
    clefTypes: List<Clef.Type>,
    pitchConstraintRange: IntRange,
    autoReset: Boolean
) {
    init {
        require(staffRange.last > staffRange.first && staffRange.step == 1)
        require(Music.isValidRelId(pitchConstraintRange.first))
        require(Music.isValidRelId(pitchConstraintRange.last))
    }

    private val random = Random3(
        list1 = 0 listUntil (calculateOrdinalSpan(staffRange)/* * clefTypes.size*/),
        list2 = clefTypes,
        list3 = pitchConstraintRange.toRangeList(),
        autoReset = autoReset
    )

    val hasNext: Boolean
        get() = random.hasNext

    fun generate(): PitchRandom.Result {
        return generateResult(random.generate())
    }

    fun generateAndExclude(): PitchRandom.Result {
        return generateResult(random.generateAndExclude())
    }
}

private fun generateResult(randomResult: Random3.Result<Int, Clef.Type, Int>): PitchRandom.Result {
    val (relOrdinal, clefType, pitchConstraint) = randomResult
    val ordinal = relOrdinal + Music.pitchOrdinalFromAbsStep(clefType.baseAbsStep(), -AlterRange.span / 2)
    return PitchRandom.Result(
        clefType,
        FreePitch(Music.idFromOrdinal(ordinal)).apply {
            constrain(pitchConstraint)
        }
    )
}

class PitchRandom(
    staffRange: IntRange,
    private val clefTypes: List<Clef.Type>,
    autoReset: Boolean
) {

    init {
        require(staffRange.first < staffRange.last && staffRange.step == 1)
    }

    private val ordinalSpan = calculateOrdinalSpan(staffRange)

    private val random = run {
        val constraint = buildConstraint {
            withRange(0 until ordinalSpan * clefTypes.size)
        }
        DynamicIntRandom(constraint, autoReset)
    }

    //private val random = Random()

    fun generate(): Result {
        val value = random.generate()
        val clefType = clefTypes[value / ordinalSpan]
        val ordinal = value % ordinalSpan + Music.pitchOrdinalFromAbsStep(clefType.baseAbsStep(), -AlterRange.span / 2)
        return Result(clefType, FreePitch(Music.idFromOrdinal(ordinal)))
    }

    fun generateAndExclude(): Result {
        val value = random.generateAndExclude()
        val clefType = clefTypes[value / ordinalSpan]
        val ordinal = value % ordinalSpan + Music.pitchOrdinalFromAbsStep(clefType.baseAbsStep(), -AlterRange.span / 2)
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