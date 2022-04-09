package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomPitchGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.TruthQuestion
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.Question
import java.sql.Connection
import java.util.*

class ChromaticScale(database: Connection) : GroupGenerator("chromatic_scale", database) {

    private val random: Random = Random()

    private val randomForClef = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(Clef.Type.values().size - 1)
        .build()

    private val randomForFirstPitch = RandomPitchGenerator().apply {
        setStaffBounds(STAFF_LOWER_BOUND, STAFF_UPPER_BOUND)
    }

    private val randomForConstrain = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(Music.relIdFromLetter(Letter.F, -1))
        .withUpperBound(Music.relIdFromLetter(Letter.G, 0))
        .build()

    private val randomForError = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(ScaleError.values().size - 1)
        .build()

    private fun missingNaturalIsPossible(chromaticPitches: Array<FreePitch>, ascending: Boolean): Boolean {
        var possible = false
        for (i in 0 until chromaticPitches.size - 1) {
            val pitch = chromaticPitches[i]
            val nextPitch = chromaticPitches[i + 1]
            if (ascending && pitch.alter() == -1 && nextPitch.letter() == pitch.letter() ||
                !ascending && pitch.alter() == 1 && nextPitch.letter() == pitch.letter()
            ) {
                possible = true
                break
            }
        }
        return possible
    }

    private fun wrongEndNoteIsPossible(chromaticPitches: Array<FreePitch>, ascending: Boolean): Boolean {
        var possible = true
        var endPitch = chromaticPitches[chromaticPitches.size - 1]
        endPitch = FreePitch(endPitch)
        endPitch.removeConstraint()
        val enharmonicPitches = endPitch.enharmonicExc()
        val chosenEnharmonic = if (ascending) enharmonicPitches[enharmonicPitches.size - 1] else enharmonicPitches[0]
        if (chosenEnharmonic.alter() == -2 || chosenEnharmonic.alter() == 2) {
            possible = false
        }
        return possible
    }

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {

        val questions = List<Question>(NO_OF_QUESTIONS) { index ->
            val isWrong = random.nextBoolean()
            val constrainRelId = randomForConstrain.nextInt()
            val clefType = Clef.Type.values()[randomForClef.nextInt()]
            randomForClef.exclude(clefType.ordinal)
            val pitchIterator = randomForFirstPitch.nextPitch(clefType)
            pitchIterator.constrain(constrainRelId)

            // TODO: WHEN CONSTRAINING THE NOTE MIGHT GET 1 STEP HIGHER OR LOWER --> LOWER THAN ALLOWED RANGE
            // TODO: POSSIBLY FIXED?
            val statement: String = context.getString("chromatic_scale_truth_statement", pitchIterator.string())
            val staffPosition = pitchIterator.absStep() - clefType.baseAbsStep()
            val ascending: Boolean = if (staffPosition > STAFF_UPPER_BOUND - (Letter.NO_OF_LETTERS + 1)) {
                false
            } else if (staffPosition < STAFF_LOWER_BOUND + (Letter.NO_OF_LETTERS + 1)) {
                true
            } else {
                random.nextBoolean()
            }

            val score = Score()
            score.newPart()
            score.newMeasure(
                Measure.Attributes(
                    1,
                    Key(Letter.C, 0, Mode.MAJOR),
                    null,
                    1, arrayOf(Clef(clefType))
                )
            )

            val chromaticPitches = Array(NO_OF_NOTES) {
                FreePitch(pitchIterator).also {
                    if (ascending) {
                        pitchIterator.translateUp(Interval.AUG_U)
                    } else {
                        pitchIterator.translateDown(Interval.AUG_U)
                    }
                }
            }

            // considering if the missing accidental error is possible
            if (!missingNaturalIsPossible(chromaticPitches, ascending)) {
                randomForError.exclude(ScaleError.MISSING_NAT.ordinal)
            }
            // considering if the end note error is possible
            if (!wrongEndNoteIsPossible(chromaticPitches, ascending)) {
                randomForError.exclude(ScaleError.END_NOTE.ordinal)
            }

            if (isWrong) {
                val scaleError = ScaleError.values()[randomForError.nextInt()]
                randomForError.clearAllExcludedIntegers()
                when (scaleError) {
                    ScaleError.TRIPLE_LETTER -> alterScaleForTripleLetter(chromaticPitches, ascending)
                    ScaleError.END_NOTE -> alterScaleForEndNote(chromaticPitches, ascending)
                    else -> alterScaleForMissingNat(chromaticPitches, ascending)
                }
            }

            for (i in 0 until NO_OF_NOTES) {
                score.addPitchedNote(chromaticPitches[i], 4, Note.Type.WHOLE)
            }

            TruthQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Score,
                        score.toDocument().asXML()
                    ),
                    Description(
                        Description.Type.TextEmphasize,
                        statement
                    )
                ),
                answer = TruthQuestion.Answer(null, !isWrong)
            )
        }

        randomForClef.clearAllExcludedIntegers()
        randomForConstrain.clearAllExcludedIntegers()

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("general_truth_group_desc")
                )
            )
        )
    }

    private fun alterScaleForTripleLetter(chromaticPitches: Array<FreePitch>, ascending: Boolean) {
        val eligiblePitches = ArrayList<Int>()
        val noteBelow = ArrayList<Boolean>()
        for (i in 1 until chromaticPitches.size - 1) {
            val previousPitch = chromaticPitches[i - 1]
            val pitch = chromaticPitches[i]
            val nextPitch = chromaticPitches[i + 1]
            val equalsPrevious = previousPitch.letter() == pitch.letter()
            val equalsNext = nextPitch.letter() == pitch.letter()
            if ((equalsPrevious || equalsNext) && pitch.alter() == 0) {
                eligiblePitches.add(i)
                noteBelow.add(equalsNext)
            }
        }
        val chosenEligiblePitch = random.nextInt(eligiblePitches.size)
        val refPitchIndex = eligiblePitches[chosenEligiblePitch]
        if (noteBelow[chosenEligiblePitch]) {
            chromaticPitches[refPitchIndex - 1].removeConstraint()
            if (ascending) {
                chromaticPitches[refPitchIndex - 1].translateUp(Interval.DIM_2)
            } else {
                chromaticPitches[refPitchIndex - 1].translateDown(Interval.DIM_2)
            }
        } else {
            chromaticPitches[refPitchIndex + 1].removeConstraint()
            if (ascending) {
                chromaticPitches[refPitchIndex + 1].translateDown(Interval.DIM_2)
            } else {
                chromaticPitches[refPitchIndex + 1].translateUp(Interval.DIM_2)
            }
        }
    }

    private fun alterScaleForEndNote(chromaticPitches: Array<FreePitch>, ascending: Boolean) {
        val endPitch = chromaticPitches[chromaticPitches.size - 1]
        endPitch.removeConstraint()
        val enharmonicPitches = endPitch.enharmonicExc()
        chromaticPitches[chromaticPitches.size - 1] = if (ascending) enharmonicPitches[enharmonicPitches.size - 1] else enharmonicPitches[0]
    }

    private fun alterScaleForMissingNat(chromaticPitches: Array<FreePitch>, ascending: Boolean) {
        val eligiblePitches = ArrayList<Int>()
        if (ascending) {
            for (i in 0 until chromaticPitches.size - 1) {
                val pitch = chromaticPitches[i]
                val nextPitch = chromaticPitches[i + 1]
                if (pitch.alter() == -1 &&
                    nextPitch.letter() == pitch.letter()
                ) {
                    eligiblePitches.add(i)
                }
            }
            val wrongPitchIndex = eligiblePitches[random.nextInt(eligiblePitches.size)]
            val wrongPitch = chromaticPitches[wrongPitchIndex + 1]
            wrongPitch.setAlter(-1)
        } else {
            for (i in 0 until chromaticPitches.size - 1) {
                val pitch = chromaticPitches[i]
                val nextPitch = chromaticPitches[i + 1]
                if (pitch.alter() == 1 &&
                    nextPitch.letter() == pitch.letter()
                ) {
                    eligiblePitches.add(i)
                }
            }
            val wrongPitchIndex = eligiblePitches[random.nextInt(eligiblePitches.size)]
            val wrongPitch = chromaticPitches[wrongPitchIndex + 1]
            wrongPitch.setAlter(1)
        }
    }

    companion object {

        // TODO: CONSTRAINING FREE PITCH CAUSES IT TO GO BEYOND BOUND, MAX BY 2, PLEASE FIX maybe not.
        private const val STAFF_LOWER_BOUND = -3 // should be -6
        private const val STAFF_UPPER_BOUND = 11 // should be 13
        private const val NO_OF_NOTES = 13
        private const val NO_OF_QUESTIONS = 2

    }

    private enum class ScaleError { TRIPLE_LETTER, END_NOTE, MISSING_NAT }

}