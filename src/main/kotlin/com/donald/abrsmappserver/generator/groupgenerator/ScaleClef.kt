package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.question.Description
import java.sql.Connection
import java.util.*

class ScaleClef(database: Connection) : GroupGenerator("scale_clef", database) {

    private val random: Random = Random()

    private val randomForKey: RandomIntegerGenerator = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(-7)
        .withUpperBound(7)
        .build()

    private val randomForMode: RandomIntegerGenerator = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(2)
        .build()

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val questions = List(NO_OF_QUESTIONS) { index ->
            // TODO: VERIFY THE ASSUMPTION THAT A VALID MINOR SCALE IS ALWAYS INVALID WITH ANOTHER CLEF
            val clefIndex = random.nextInt(Clef.Type.values().size)
            val clefType = Clef.Type.values()[clefIndex]
            val clef = Clef(clefType)
            clef.setPrintObject(false)
            val mode = Mode.values()[randomForMode.nextInt()]
            val key = Key(randomForKey.nextInt(), mode)
            val ascending = random.nextBoolean()
            val firstNoteAbsStep = getRandomFirstNoteAbsStep(key, clefType, ascending, random)
            val pitch = DiatonicPitch(
                key,
                Music.letterFromAbsStep(firstNoteAbsStep),
                Music.octaveFromAbsStep(firstNoteAbsStep)
            )
            val score = Score()
            score.newPart("P1")
            score.newMeasure(
                Measure.Attributes(
                    1,
                    Key(0, Mode.MAJOR),
                    null,
                    1, arrayOf(clef)
                )
            )
            for (i in 0 until NO_OF_NOTES) {
                score.addPitchedNote(DiatonicPitch(pitch), 4, Note.Type.WHOLE)
                if (ascending) pitch.translateUp(DiatonicInterval.SECOND) else pitch.translateDown(DiatonicInterval.SECOND)
            }

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Score,
                        score.toDocument().asXML()
                    )
                ),
                optionType = MultipleChoiceQuestion.OptionType.Image,
                options = OPTION_LIST.toList(),
                answer = MultipleChoiceQuestion.Answer(null, NO_OF_OPTIONS - 1 - clefIndex)
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("scale_clef_question_desc")
                )
            ),
        )
    }

    companion object {

        private const val STAFF_LOWER_BOUND = -5
        private const val STAFF_UPPER_BOUND = 13
        private const val NO_OF_NOTES = 8
        private const val NO_OF_QUESTIONS = 3
        private val NO_OF_OPTIONS = Clef.Type.values().size
        private val OPTION_LIST = listOf(
            "g_treble_clef",
            "g_alto_clef",
            "g_tenor_clef",
            "g_bass_clef"
        )

        private fun getRandomFirstNoteAbsStep(
            key: Key, clefType: Clef.Type,
            ascending: Boolean, random: Random
        ): Int {
            val lowestAbsoluteStep: Int
            val firstTonicLetter = key.tonicPitch().letter()
            lowestAbsoluteStep = if (ascending) {
                clefType.baseAbsStep() + STAFF_LOWER_BOUND
            } else {
                clefType.baseAbsStep() + STAFF_LOWER_BOUND - 1 + NO_OF_NOTES
            }
            val lowestLetter = Letter.fromAbsoluteStep(lowestAbsoluteStep)
            val tonicStepHeight =  // distance between the lowest absolute step and the lowest possible tonic absolute step
                Math.floorMod(firstTonicLetter.step() - lowestLetter.step(), Letter.NO_OF_LETTERS)
            // for example, a C major scale might start at C4 or C5 with a treble clef
            val numberOfPossiblePositions = (STAFF_UPPER_BOUND - STAFF_LOWER_BOUND + 1 - (NO_OF_NOTES - 1) - tonicStepHeight +
                    Letter.NO_OF_LETTERS - 1) /
                    Letter.NO_OF_LETTERS
            val lowestPossibleTonicStep = lowestAbsoluteStep + tonicStepHeight
            return lowestPossibleTonicStep +
                    random.nextInt(numberOfPossiblePositions) * Consts.PITCHES_PER_SCALE
        }

    }

}