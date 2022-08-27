package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.DynamicIntRandom
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.buildConstraint
import java.sql.Connection
import java.util.*

private const val STAFF_LOWER_BOUND = -5
private const val STAFF_UPPER_BOUND = 13
private const val NOTE_COUNT = 8
private const val PARENT_QUESTION_COUNT = 3
private const val CHILD_QUESTION_COUNT = 1
private val OPTION_COUNT = Clef.Type.values().size
private val OPTION_LIST = listOf(
    "g_treble_clef",
    "g_alto_clef",
    "g_tenor_clef",
    "g_bass_clef"
)

class ScaleClef(database: Connection) : GroupGenerator(
    "scale_clef",
    PARENT_QUESTION_COUNT,
    database
) {

    private val random: Random = Random()

    /*
    private val randomForKey: RandomIntegerGenerator = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(-6)
        .withUpperBound(6)
        .build()

    private val randomForMode: RandomIntegerGenerator = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(2)
        .build()

     */

    private val keyConstraint = buildConstraint {
        withRange(-6..6)
    }

    private val modeConstraint = buildConstraint {
        withRange(0..2)
    }

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val keyRandom = DynamicIntRandom(keyConstraint, autoReset = true)
        val questions = List(parentQuestionCount) { parentIndex ->
            // TODO: VERIFY THE ASSUMPTION THAT A VALID MINOR SCALE IS ALWAYS INVALID WITH ANOTHER CLEF
            val clefType = Clef.Type.types.random()
            val clefIndex = clefType.ordinal
            val clef = Clef(clefType)
            clef.setPrintObject(false)
            val mode = Mode.modes.random()
            val key = Key(keyRandom.generateAndExclude(), mode)
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
                    Key(0, Mode.Major),
                    null,
                    1, arrayOf(clef)
                )
            )
            for (i in 0 until NOTE_COUNT) {
                score.addPitchedNote(DiatonicPitch(pitch), 4, Note.Type.WHOLE)
                if (ascending) pitch.translateUp(DiatonicInterval.SECOND) else pitch.translateDown(DiatonicInterval.SECOND)
            }

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Score,
                        score.toDocument().asXML()
                    )
                ),
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    MultipleChoiceQuestion(
                        number = childIndex + 1,
                        optionType = MultipleChoiceQuestion.OptionType.Image,
                        options = OPTION_LIST.toList(),
                        answer = MultipleChoiceQuestion.Answer(null, OPTION_COUNT - 1 - clefIndex)
                    )
                }
            )
        }


        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("scale_clef_question_desc")
                )
            ),
        )
    }

    private fun getRandomFirstNoteAbsStep(
        key: Key, clefType: Clef.Type,
        ascending: Boolean, random: Random
    ): Int {
        val lowestAbsoluteStep: Int
        val firstTonicLetter = key.tonicPitch().letter()
        lowestAbsoluteStep = if (ascending) {
            clefType.baseAbsStep() + STAFF_LOWER_BOUND
        } else {
            clefType.baseAbsStep() + STAFF_LOWER_BOUND - 1 + NOTE_COUNT
        }
        val lowestLetter = Letter.fromAbsoluteStep(lowestAbsoluteStep)
        val tonicStepHeight =  // distance between the lowest absolute step and the lowest possible tonic absolute step
            Math.floorMod(firstTonicLetter.step() - lowestLetter.step(), Letter.NO_OF_LETTERS)
        // for example, a C major scale might start at C4 or C5 with a treble clef
        val numberOfPossiblePositions = (STAFF_UPPER_BOUND - STAFF_LOWER_BOUND + 1 - (NOTE_COUNT - 1) - tonicStepHeight +
                Letter.NO_OF_LETTERS - 1) /
                Letter.NO_OF_LETTERS
        val lowestPossibleTonicStep = lowestAbsoluteStep + tonicStepHeight
        return lowestPossibleTonicStep +
                random.nextInt(numberOfPossiblePositions) * Consts.PITCHES_PER_SCALE
    }

}