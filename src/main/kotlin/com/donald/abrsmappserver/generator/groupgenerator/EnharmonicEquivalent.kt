package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGenerator
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomPitchGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Measure.Barline
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.question.Description
import java.sql.Connection
import java.util.*

class EnharmonicEquivalent(database: Connection) : GroupGenerator("enharmonic_equivalent", database) {

    private val random = Random()

    private val randomForWrongOptions: RandomIntegerGenerator = RandomIntegerGeneratorBuilder.generator()
       .withLowerBound(0)
       .withUpperBound(5)
       .build()

    private val randomForPitch: RandomPitchGenerator = RandomPitchGenerator().apply {
        setStaffBounds(-6, 14)
    }

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val questions = List(NO_OF_QUESTIONS) { index ->

            // 1. target pitch
            val clefType = Clef.Type.values()[random.nextInt(Clef.Type.values().size)]
            val targetPitch = randomForPitch.nextPitch(clefType)

            val descriptions = run {
                val score = Score().apply {
                    newPart("P1")
                    newMeasure(
                        Measure.Attributes(
                            1,
                            Key(Letter.C, 0, Mode.MAJOR),
                            null,
                            1, arrayOf(Clef(clefType))
                        ),
                        Barline(Barline.BarStyle.LIGHT_LIGHT)
                    )
                    addPitchedNote(targetPitch, 4, Note.Type.WHOLE)
                }
                listOf(Description(Description.Type.Score, score.toDocument().asXML()))
            }

            // 2. correct enharmonic note
            val enharmonicPitches = targetPitch.enharmonicExc()
            val correctEnharmonicPitch = enharmonicPitches[random.nextInt(enharmonicPitches.size)]

            val correctOption = run {
                val score = Score()
                score.newPart("P1")
                score.newMeasure(
                    Measure.Attributes(
                        1,
                        Key(Letter.C, 0, Mode.MAJOR),
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

            // 3. generating wrong options
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
            for (i in 0..5) {
                for (enharmonicNote in enharmonicPitches) {
                    if (plausibleNotes[i]!!.id() == enharmonicNote.id()) randomForWrongOptions.exclude(i)
                }
            }

            // c. randomly choosing from the pool as wrong options
            val options = List(NO_OF_OPTIONS) { i ->
                if (i == 0) {
                    correctOption
                }
                else {
                    val chosenOption = randomForWrongOptions.nextInt()

                    randomForWrongOptions.exclude(chosenOption)
                    val score = Score()
                    score.newPart("P1")
                    score.newMeasure(
                        Measure.Attributes(
                            1,
                            Key(Letter.C, 0, Mode.MAJOR),
                            null,
                            1, arrayOf(Clef(clefType))
                        ),
                        Barline(Barline.BarStyle.LIGHT_LIGHT)
                    )
                    score.addPitchedNote(plausibleNotes[chosenOption], 4, Note.Type.WHOLE)
                    score.toDocument().asXML()
                }
            }
            randomForWrongOptions.clearAllExcludedIntegers()

            val dispositions = options.shuffle()
            val answer = MultipleChoiceQuestion.Answer(null, dispositions[0])

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = descriptions,
                optionType = MultipleChoiceQuestion.OptionType.Score,
                options = options,
                answer = answer
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("enharmonic_equivalent_group_desc")
                )
            )
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 1
        private const val NO_OF_OPTIONS = 3

    }

}