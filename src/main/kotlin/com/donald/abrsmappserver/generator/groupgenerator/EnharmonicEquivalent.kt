package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Measure.Barline
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.*
import java.sql.Connection
import java.util.*

private const val PARENT_QUESTION_COUNT = 1
private const val CHILD_QUESTION_COUNT = 1
private const val OPTION_COUNT = 3
private val STAFF_RANGE = -6..14

class EnharmonicEquivalent(database: Connection) : GroupGenerator(
    "enharmonic_equivalent",
    PARENT_QUESTION_COUNT,
    database
) {

    private val random = Random()

    private val wrongOptionConstraint = Constraint.Builder()
        .withRange(0..5)
        .build()

    /*
    private val randomForWrongOptions: RandomIntegerGenerator = RandomIntegerGeneratorBuilder.generator()
       .withLowerBound(0)
       .withUpperBound(5)
       .build()
     */

    private val randomForPitch: RandomPitchGenerator = RandomPitchGenerator().apply {
        setStaffBounds(-6, 14)
    }

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val pitchRandom = PitchRandom(STAFF_RANGE, Clef.Type.types, autoReset = true)

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            val (clefType, targetPitch) = pitchRandom.generateAndExclude()

            val descriptions = run {
                val score = Score().apply {
                    newPart("P1")
                    newMeasure(
                        Measure.Attributes(
                            1,
                            Key(Letter.C, 0, Mode.Major),
                            null,
                            1, arrayOf(Clef(clefType))
                        ),
                        Barline(Barline.BarStyle.LIGHT_LIGHT)
                    )
                    addPitchedNote(targetPitch, 4, Note.Type.WHOLE)
                }
                listOf(Description(Description.Type.Score, score.toDocument().asXML()))
            }

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = descriptions,
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex -> generateChildQuestion(childIndex, clefType, targetPitch) }
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = parentQuestions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("enharmonic_equivalent_group_desc")
                )
            )
        )
    }

    private fun generateChildQuestion(childIndex: Int, clefType: Clef.Type, targetPitch: FreePitch): MultipleChoiceQuestion {
        // 1. correct enharmonic note
        val enharmonicPitches = targetPitch.enharmonicExc()
        val correctEnharmonicPitch = enharmonicPitches[random.nextInt(enharmonicPitches.size)]

        val correctOption = run {
            val score = Score()
            score.newPart("P1")
            score.newMeasure(
                Measure.Attributes(
                    1,
                    Key(Letter.C, 0, Mode.Major),
                    null,
                    1, arrayOf(Clef(clefType))
                ),
                Barline(
                    Barline.BarStyle.LIGHT_LIGHT
                )
            )
            score.addPitchedNote(correctEnharmonicPitch, 4, Note.Type.WHOLE)
            score.toDocument().asXML()
        }

        // 2. generating wrong options
        // a. creating a pool of plausible notes
        val plausibleNotes = arrayOfNulls<FreePitch>(6)
        val targetNoteLetter = targetPitch.letter()
        // i. the white note below
        var whiteNoteOctave = targetPitch.octave()
        if (targetNoteLetter == Letter.C) whiteNoteOctave--
        val whiteNoteBelow = FreePitch(
            Letter.valuesBySteps()[Math.floorMod(targetNoteLetter.step() - 1, Letter.NO_OF_LETTERS)],
            0, whiteNoteOctave
        )
        // ii. the white note above
        whiteNoteOctave = targetPitch.octave()
        if (targetNoteLetter == Letter.B) whiteNoteOctave++
        val whiteNoteAbove = FreePitch(
            Letter.valuesBySteps()[Math.floorMod(targetNoteLetter.step() + 1, Letter.NO_OF_LETTERS)],
            0, whiteNoteOctave
        )

        // iii. filling up the pool
        // TODO: ENSURE THERE ARE SHARPS / FLATS IF THE ENHARMONIC NOTE HAS NO NOTES
        // TODO: AVOID PRODUCING ALL THESE OBJECT ALLOCATIONS
        var currentNote: FreePitch
        plausibleNotes[0] = whiteNoteBelow
        currentNote = FreePitch(whiteNoteBelow)
        currentNote.setAlter(1)
        plausibleNotes[1] = currentNote
        currentNote = FreePitch(whiteNoteBelow)
        currentNote.setAlter(2)
        plausibleNotes[2] = currentNote
        plausibleNotes[3] = whiteNoteAbove
        currentNote = FreePitch(whiteNoteAbove)
        currentNote.setAlter(-1)
        plausibleNotes[4] = currentNote
        currentNote = FreePitch(whiteNoteAbove)
        currentNote.setAlter(-2)
        plausibleNotes[5] = currentNote

        // b. excluding the correct answers
        val wrongOptionRandom = DynamicIntRandom(wrongOptionConstraint, autoReset = true)

        for (i in 0..5) {
            for (enharmonicNote in enharmonicPitches) {
                if (plausibleNotes[i]!!.id() == enharmonicNote.id()) wrongOptionRandom.exclude(i)
            }
        }

        // c. randomly choosing from the pool as wrong options
        val options = List(OPTION_COUNT) { i ->
            if (i == 0) {
                correctOption
            }
            else {
                val chosenOption = wrongOptionRandom.generate()

                wrongOptionRandom.exclude(chosenOption)
                val score = Score()
                score.newPart("P1")
                score.newMeasure(
                    Measure.Attributes(
                        1,
                        Key(Letter.C, 0, Mode.Major),
                        null,
                        1, arrayOf(Clef(clefType))
                    ),
                    Barline(Barline.BarStyle.LIGHT_LIGHT)
                )
                score.addPitchedNote(plausibleNotes[chosenOption], 4, Note.Type.WHOLE)
                score.toDocument().asXML()
            }
        }

        val (shuffledOptions, dispositions) = options.shuffled()
        val answer = MultipleChoiceQuestion.Answer(null, dispositions[0])

        return MultipleChoiceQuestion(
            number = childIndex + 1,
            descriptions = emptyList(),
            optionType = MultipleChoiceQuestion.OptionType.Score,
            options = shuffledOptions,
            answer = answer
        )
    }

}