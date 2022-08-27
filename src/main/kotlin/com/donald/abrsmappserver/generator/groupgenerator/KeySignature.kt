package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.newIndex
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Clef
import com.donald.abrsmappserver.utils.music.Key
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom.Random1
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom.Random3
import com.donald.abrsmappserver.utils.music.Mode
import java.sql.Connection

private const val PARENT_QUESTION_COUNT = 2
private const val CHILD_QUESTION_COUNT = 1
private const val OPTION_COUNT = 4
private val SHARP_LIST = listOf(true, false)
private val MAJOR_MINOR = listOf(true, false)
private val ACC_COUNTS = listOf(3, 4, 5, 6)
private val WRONG_ACC_COUNTS = listOf(1, 2)

class KeySignature(database: Connection) : GroupGenerator(
    "key_signature",
    PARENT_QUESTION_COUNT,
    database
) {

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val sharpRandom = Random1(SHARP_LIST, autoReset = true)
        val majorRandom = Random1(MAJOR_MINOR, autoReset = true)
        val variationRandom = Random3(
            Clef.Type.types,
            ACC_COUNTS,
            WRONG_ACC_COUNTS,
            autoReset = true
        )

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            val (correctClefType, accCount, wrongAccCount) = variationRandom.generateAndExclude()
            //val correctClefIndex = clefRandom.generateAndExclude()
            //val correctClef = Clef.Type.values()[correctClefIndex]
            val isSharp = sharpRandom.generateAndExclude()
            val isMajor = majorRandom.generateAndExclude()

            val options: List<String>
            val answer: Int
            run {
                val shuffleResult = generateOptions(
                    correctClefType,
                    accCount,
                    wrongAccCount,
                    if (isSharp) 's' else 'b'
                ).shuffled()
                options = shuffleResult.shuffled
                answer = shuffleResult.newIndex(correctClefType.ordinal)
            }

            val key: Key = run {
                val mode = if (isMajor) Mode.Major else Mode.NatMinor
                val fifths = if (isSharp) accCount else -accCount
                Key(fifths, mode)
            }

            val childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                MultipleChoiceQuestion(
                    number = childIndex + 1,
                    optionType = MultipleChoiceQuestion.OptionType.Image,
                    options = options,
                    answer = MultipleChoiceQuestion.Answer(null, answer)
                )
            }

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Text,
                        context.getString("key_signature_question_desc", key.string(context.bundle))
                    )
                ),
                childQuestions = childQuestions
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = parentQuestions
        )
    }

    private fun generateOptions(correctClefType: Clef.Type, accCount: Int, wrongAccCount: Int, accidentalSymbol: Char): List<String> {
        return List(OPTION_COUNT) { i ->
            val currentClefType = Clef.Type.types[i]
            if (currentClefType == correctClefType) {
                "g_key_signature_" + correctClefType.string() + "_" + accidentalSymbol + accCount
            } else {
                "g_key_signature_" + currentClefType.string() + "_" + accidentalSymbol + accCount + "w" + wrongAccCount
            }
        }
    }

}