package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.*
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.*
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.utils.music.new.AlterRange
import com.donald.abrsmappserver.utils.music.new.span
import com.donald.abrsmappserver.utils.range.listTo
import java.sql.Connection
import java.util.*

private const val STAFF_LOWER_BOUND = -3 // should be -6
private const val STAFF_UPPER_BOUND = 11 // should be 13
private val StaffRange = -3..11
private val PitchConstraintRange = Music.relIdFromLetter(Letter.F, -1)..Music.relIdFromLetter(Letter.G, 0)
private const val NOTE_COUNT = 13
private const val PARENT_QUESTION_COUNT = 2
private const val CHILD_QUESTION_COUNT = 1

class ChromaticScale(database: Connection) : GroupGenerator(
    "chromatic_scale",
    PARENT_QUESTION_COUNT,
    database
) {

    private val random: Random = Random()

    private val clefConstraint = Constraint.Builder()
        .withRange(0 until Clef.Type.values().size)
        .build()

    /*
    private val randomForClef = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(Clef.Type.values().size - 1)
        .build()

     */

    /*private val randomForFirstPitch = RandomPitchGenerator().apply {
        setStaffBounds(STAFF_LOWER_BOUND, STAFF_UPPER_BOUND)
    }

    private val pitchConstraintConstraint = Constraint.Builder()
        .withRange(Music.relIdFromLetter(Letter.F, -1)..Music.relIdFromLetter(Letter.G, 0))
        .build()*/

    /*
    private val randomForConstrain = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(Music.relIdFromLetter(Letter.F, -1))
        .withUpperBound(Music.relIdFromLetter(Letter.G, 0))
        .build()

     */

    private val errorConstraint = Constraint.Builder()
        .withRange(0 until ScaleError.values().size)
        .build()


    /*
    private val randomForError = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(ScaleError.values().size - 1)
        .build()

     */

    // relative to the clef's base ordinal
    private val relPitchOrdinalList = run {
        val lowest = Music.pitchOrdinalFromAbsStep(STAFF_LOWER_BOUND, -AlterRange.span / 2)
        val highest = Music.pitchOrdinalFromAbsStep(STAFF_UPPER_BOUND, AlterRange.span / 2)
        lowest listTo highest
    }

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val pitchRandom = ConstrainedPitchRandom(
            StaffRange,
            Clef.Type.types,
            PitchConstraintRange,
            autoReset = true
        )

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            val (clefType, pitchIterator) = pitchRandom.generateAndExclude()
            val isWrong = random.nextBoolean()
            val statement: String = context.getString("chromatic_scale_truth_statement", pitchIterator.string())

            val staffPosition = pitchIterator.absStep() - clefType.baseAbsStep()
            val ascending: Boolean = if (staffPosition > STAFF_UPPER_BOUND - (Letter.NO_OF_LETTERS + 1)) {
                false
            } else if (staffPosition < STAFF_LOWER_BOUND + (Letter.NO_OF_LETTERS + 1)) {
                true
            } else {
                random.nextBoolean()
            }

            val score = generateScore(isWrong, ascending, clefType, pitchIterator)

            ParentQuestion(
                number = parentIndex + 1,
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
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex -> generateChildQuestion(childIndex, isWrong) }
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = parentQuestions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("general_truth_group_desc")
                )
            )
        )

    }

    private fun generateChildQuestion(childIndex: Int, isWrong: Boolean): TruthQuestion {
        return TruthQuestion(
            number = childIndex + 1,
            descriptions = emptyList(),
            answer = TruthQuestion.Answer(null, !isWrong)
        )
    }

    private fun generateScore(isWrong: Boolean, ascending: Boolean, clefType: Clef.Type, pitchIterator: FreePitch): Score {
        val errorRandom = DynamicIntRandom(errorConstraint, autoReset = false)
        val score = Score()
        score.newPart("P1")
        score.newMeasure(
            Measure.Attributes(
                1,
                Key(Letter.C, 0, Mode.Major),
                null,
                1, arrayOf(Clef(clefType))
            )
        )

        val chromaticPitches = Array(NOTE_COUNT) {
            FreePitch(pitchIterator).also {
                if (ascending) {
                    pitchIterator.translateUp(Interval.AUG_U)
                } else {
                    pitchIterator.translateDown(Interval.AUG_U)
                }
            }
        }

        if (isWrong) {
            // considering if the missing accidental error is possible
            if (!missingNaturalIsPossible(chromaticPitches, ascending)) {
                errorRandom.exclude(ScaleError.MissNatural.ordinal)
            }
            // considering if the end note error is possible
            if (!wrongEndNoteIsPossible(chromaticPitches, ascending)) {
                errorRandom.exclude(ScaleError.EndNote.ordinal)
            }
            val scaleError = ScaleError.values()[errorRandom.generate()]
            when (scaleError) {
                ScaleError.TripleLetter -> alterScaleForTripleLetter(chromaticPitches, ascending)
                ScaleError.EndNote -> alterScaleForEndNote(chromaticPitches, ascending)
                else -> alterScaleForMissingNat(chromaticPitches, ascending)
            }
        }

        for (i in 0 until NOTE_COUNT) {
            score.addPitchedNote(chromaticPitches[i], 4, Note.Type.WHOLE)
        }

        return score
    }


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

    private enum class ScaleError { TripleLetter, EndNote, MissNatural }

}