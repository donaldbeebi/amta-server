package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import java.lang.StringBuilder
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.CheckBoxQuestion
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import java.sql.Connection

private const val PARENT_QUESTION_COUNT = 1
private const val CHILD_QUESTION_COUNT = 1
private const val ANSWER_COUNT = 5
private val SELECTION_STRING: String = run {
    val builder = StringBuilder()
    for (i in 0 until ANSWER_COUNT) {
        if (i != 0) builder.append(", ")
        builder.append("check_").append(i + 1)
    }
    builder.toString()
}


class Transposition(database: Connection) : GroupGenerator(
    "transposition",
    PARENT_QUESTION_COUNT,
    database
) {

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        /*
        val result = database.prepareStatement("""
            SELECT variation, direction_string_key, interval_string_key, check_1, check_2, check_3, check_4, check_5, iteration
            FROM questions_transposition
            ORDER BY RANDOM() LIMIT ?;
        """.trimIndent()).apply{
            setInt(1, parentQuestionCount)
        }.executeQuery()
         */
        val result = database.prepareStatement("""
            WITH loop(variation, direction_string_key, interval_string_key, check_1, check_2, check_3, check_4, check_5, iteration) AS (
                SELECT variation, direction_string_key, interval_string_key, check_1, check_2, check_3, check_4, check_5, 1 AS iteration FROM questions_transposition
                UNION ALL
                SELECT variation, direction_string_key, interval_string_key, check_1, check_2, check_3, check_4, check_5, iteration + 1 FROM loop LIMIT ?
            )
            SELECT variation, direction_string_key, interval_string_key, check_1, check_2, check_3, check_4, check_5 FROM loop ORDER BY iteration, RANDOM()
        """.trimIndent()).apply {
            setInt(1, parentQuestionCount)
        }.executeQuery()

        val questions = List(parentQuestionCount) { index ->
            result.next()
            val variation = result.getInt("variation")
            val transposition: String = context.getString(result.getString("direction_string_key")) + " " +
                    context.getString(result.getString("interval_string_key"))
            val descriptions = listOf(
                // a) first text
                Description(
                    Description.Type.Text,
                    context.getString("transposition_question_desc_1")
                ),
                // b) first image
                Description(
                    Description.Type.Image,
                    "q_transposition_original_$variation"
                ),
                // c) second text
                Description(
                    Description.Type.Text,
                    context.getString("transposition_question_desc_2", transposition)
                ),
                // d) second image
                Description(
                    Description.Type.Image,
                    "q_transposition_transposed_$variation"
                )
            )

            val answers = List(ANSWER_COUNT) { i ->
                if (result.getInt("check_" + (i + 1)) == 0) {
                    CheckBoxQuestion.Answer(null, false)
                } else {
                    CheckBoxQuestion.Answer(null, true)
                }
            }

            ParentQuestion(
                number = index + 1,
                descriptions = descriptions,
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    CheckBoxQuestion(
                        number = childIndex + 1,
                        descriptions = descriptions,
                        answers = answers
                    )
                }
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = questions,
            descriptions = emptyList()
        )
    }

}