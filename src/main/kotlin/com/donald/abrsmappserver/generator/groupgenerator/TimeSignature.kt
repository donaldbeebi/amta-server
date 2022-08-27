package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.DynamicIntRandom
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.buildConstraint
import java.sql.Connection

private const val PARENT_QUESTION_COUNT = 3
private const val CHILD_QUESTION_COUNT = 1
private const val OPTION_COUNT = 3
private val TIME_SIGNATURE_POSSIBLE_ANSWERS = arrayOf(
    "2_2", "2_4", "2_8", "3_2", "3_4", "3_8", "4_2", "4_4", "4_8", "5_4", "5_8",
    "6_4", "6_8", "6_16", "7_4", "7_8", "9_4", "9_8", "9_16", "12_4", "12_8", "12_16"
)
/*
private val TIME_SIGNATURE_POSSIBLE_CORRECT_ANSWERS = arrayOf(
    "4_2",
    "2_4", "3_4", "4_4", "5_4", "6_4", "7_4", "9_4",
    "3_8", "5_8", "6_8", "7_8", "9_8", "12_8"
)
 */

class TimeSignature(database: Connection) : GroupGenerator(
    "time_signature",
    PARENT_QUESTION_COUNT,
    database
) {

    private val wrongOptionConstraint = buildConstraint {
        withRange(TIME_SIGNATURE_POSSIBLE_ANSWERS.indices)
    }

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            WITH RECURSIVE loop(variation, time_signature, iteration) AS (
            	SELECT variation, time_signature, 1 AS iteration FROM questions_time_signature
            	UNION ALL
            	SELECT variation, time_signature, iteration + 1 FROM loop LIMIT ?
            )
            SELECT * FROM loop ORDER BY iteration, RANDOM()
        """.trimIndent()).apply {
            setInt(1, parentQuestionCount)
        }.executeQuery()

        val questions = List(parentQuestionCount) { parentIndex ->
            result.next()
            val correctTimeSignature = result.getString("time_signature")
            val variation = result.getInt("variation")

            val wrongOptionRandom = DynamicIntRandom(wrongOptionConstraint, autoReset = false)
            wrongOptionRandom.exclude(timeSignatureIndex(correctTimeSignature))
            val options = List(OPTION_COUNT) { i ->
                if (i == 0) {
                    "a_time_signature_$correctTimeSignature"
                } else {
                    "a_time_signature_" + TIME_SIGNATURE_POSSIBLE_ANSWERS[wrongOptionRandom.generateAndExclude()]
                }
            }

            val (shuffledOptions, dispositions) = options.shuffled()

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Image,
                        "q_time_signature_$variation"
                    ),
                    /*Description(
                        Description.Type.Image,
                        "image that does not exist"
                    )*/
                ),
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    MultipleChoiceQuestion(
                        number = childIndex + 1,
                        optionType = MultipleChoiceQuestion.OptionType.Image,
                        options = shuffledOptions,
                        answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
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
                    context.getString("time_signature_question_desc")
                )
            )
        )
    }

}

private fun timeSignatureIndex(timeSignature: String): Int {
    for (i in TIME_SIGNATURE_POSSIBLE_ANSWERS.indices) {
        if (timeSignature == TIME_SIGNATURE_POSSIBLE_ANSWERS[i]) {
            return i
        }
    }
    return -1
}