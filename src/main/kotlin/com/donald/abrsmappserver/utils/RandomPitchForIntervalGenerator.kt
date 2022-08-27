package com.donald.abrsmappserver.utils

import com.donald.abrsmappserver.utils.music.*
import java.util.*

// TODO: PROVIDE THE FUNCTIONALITY TO GENERATE AND EXCLUDE

class RandomPitchForIntervalGenerator {

    private val random: Random = Random()
    //private var lowerPitch: FreePitch? = null
    //private var upperPitch: FreePitch? = null
    private var staffLowerBound = 0
    private var staffUpperBound = 0
    private var relIdLowerBound = Music.LOWEST_REL_ID
    private var relIdUpperBound = Music.HIGHEST_REL_ID

    fun setRelIdBounds(lowerBound: Int, upperBound: Int) {
        relIdLowerBound = lowerBound
        relIdUpperBound = upperBound
    }

    fun setStaffBounds(lowerBound: Int, upperBound: Int) {
        require(lowerBound < upperBound)
        staffLowerBound = lowerBound
        staffUpperBound = upperBound
    }

    fun randomForInterval(interval: Interval, clef: Clef.Type): Result {
        // 1. figure out the staff position range of the lower note and pick a random note within the range
        val lowerPitchAbsStep = run {
            val highestStaffPos = staffUpperBound - interval.numberN() + 1
            val range = highestStaffPos - staffLowerBound + 1
            check(range > 0)
            random.nextInt(range) + staffLowerBound + clef.baseAbsStep()
        }

        val lowerPitchLetter = Music.letterFromAbsStep(lowerPitchAbsStep)

        // 2. figure out the possible rel id
        val finalFirstRelId = run {
            val lowestRelId = relIdLowerBound.coerceAtLeast(relIdLowerBound - interval.fifths())
            val highestRelId = relIdUpperBound.coerceAtMost(relIdUpperBound - interval.fifths())
            val range = highestRelId - lowestRelId + 1
            assert(range > 0)

            val numberOfPossibleRelIds = numberOfPossibleRelIds(lowestRelId, highestRelId, lowerPitchLetter)
            val lowestRelIdForLetter = lowestRelIdForLetter(lowestRelId, lowerPitchLetter)
            random.nextInt(numberOfPossibleRelIds) * Music.FIFTH_TABLE_COLUMN_COUNT + lowestRelIdForLetter
        }

        val lowerPitch = FreePitch(
            lowerPitchLetter,
            Music.alterFromId(finalFirstRelId),
            Music.octaveFromAbsStep(lowerPitchAbsStep)
        )

        val upperPitch = FreePitch(lowerPitch)
        val result = upperPitch.translateUp(interval)
        check(result)

        return Result(lowerPitch, upperPitch)
    }

    private fun numberOfPossibleRelIds(lowestRelId: Int, highestRelId: Int, letter: Letter): Int {//= (range + letter.ordinal) / Music.ID_TABLE_COLUMN_COUNT
        // TODO: OPTIMIZE PERHAPS
        var count = 0
        for (id in lowestRelId..highestRelId) {
            if (Music.letterFromId(id) == letter) count++
        }
        return count
    }

    private fun lowestRelIdForLetter(lowestRelId: Int, letter: Letter) = (Math.floorMod(letter.ordinal - lowestRelId, Music.FIFTH_TABLE_COLUMN_COUNT) + lowestRelId)

    /*
    fun lowerPitch(): FreePitch {
        return lowerPitch ?: throw IllegalStateException()
    }

    fun upperPitch(): FreePitch {
        return upperPitch ?: throw IllegalStateException()
    }
     */

    data class Result(val lowerPitch: FreePitch, val upperPitch: FreePitch)

}