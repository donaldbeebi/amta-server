package com.donald.abrsmappserver.utils

import com.donald.abrsmappserver.utils.music.*
import java.util.*

class RandomPitchForIntervalGenerator {

    private val random: Random = Random()
    private var lowerPitch: FreePitch? = null
    private var upperPitch: FreePitch? = null
    private var staffLowerBound = 0
    private var staffUpperBound = 0

    fun setStaffBounds(lowerBound: Int, upperBound: Int) {
        require(lowerBound < upperBound)
        staffLowerBound = lowerBound
        staffUpperBound = upperBound
    }

    fun randomForInterval(interval: Interval, clef: Clef.Type) {
        // 1. figure out the staff position range of the lower note and pick a random note within the range
        val lowerPitchAbsStep = run {
            val highestStaffPos = staffUpperBound - interval.numberN() + 1
            val range = highestStaffPos - staffLowerBound + 1
            check(range > 0)
            random.nextInt(range) + staffLowerBound + clef.baseAbsStep()
        }

        val lowerPitchLetter = Music.letterFromAbsStep(lowerPitchAbsStep)

        // 2. figure out the possible rel id
        val finalRelId = run {
            val lowestRelId = Music.LOWEST_REL_ID.coerceAtLeast(Music.LOWEST_REL_ID - interval.fifths())
            val highestRelId = Music.HIGHEST_REL_ID.coerceAtMost(Music.HIGHEST_REL_ID - interval.fifths())
            val range = highestRelId - lowestRelId + 1
            check(range > 0)

            val numberOfPossibleRelIds = numberOfPossibleRelIds(lowestRelId, highestRelId, lowerPitchLetter)
            val lowestRelIdForLetter = lowestRelIdForLetter(lowestRelId, lowerPitchLetter)
            random.nextInt(numberOfPossibleRelIds) * Music.FIFTH_TABLE_COLUMN_COUNT + lowestRelIdForLetter
        }

        lowerPitch = FreePitch(
            lowerPitchLetter,
            Music.alterFromId(finalRelId),
            Music.octaveFromAbsStep(lowerPitchAbsStep)
        )

        val upperPitch = FreePitch(lowerPitch)
        val result = upperPitch.translateUp(interval)
        check(result)
        this.upperPitch = upperPitch
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

    fun lowerPitch(): FreePitch {
        return lowerPitch ?: throw IllegalStateException()
    }

    fun upperPitch(): FreePitch {
        return upperPitch ?: throw IllegalStateException()
    }

}