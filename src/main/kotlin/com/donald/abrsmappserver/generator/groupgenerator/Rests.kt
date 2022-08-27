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
private const val NO_OF_ANSWERS = 3
private val SELECTION_STRING: String = run {
    val builder = StringBuilder()
    for (i in 0 until NO_OF_ANSWERS) {
        if (i != 0) builder.append(", ")
        builder.append("truth_").append(i + 1)
    }
    builder.toString()
}

class Rests(database: Connection) : GroupGenerator(
    "rests",
    PARENT_QUESTION_COUNT,
    database
) {

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            WITH loop(variation, truth_1, truth_2, truth_3, iteration) AS (
                SELECT variation, truth_1, truth_2, truth_3, 1 AS iteration FROM questions_rests
                UNION ALL
                SELECT variation, truth_1, truth_2, truth_3, iteration + 1 FROM loop LIMIT ?
            )
            SELECT variation, truth_1, truth_2, truth_3 FROM loop ORDER BY iteration, RANDOM()
        """.trimIndent()).apply {
            setInt(1, parentQuestionCount)
        }.executeQuery()

        val questions = List(parentQuestionCount) { parentIndex ->
            result.next()

            val variation = result.getInt("variation")

            val answers = List(NO_OF_ANSWERS) { i ->
                CheckBoxQuestion.Answer(null, result.getInt("truth_" + (i + 1)) == 1)
            }

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Image,
                        "q_rests_$variation"
                    )
                ),
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    CheckBoxQuestion(
                        number = childIndex + 1,
                        answers = answers
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
                    context.getString("rests_question_desc")
                )
            )
        )
    }

}