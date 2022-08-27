package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.TextInputQuestion
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import java.sql.Connection

// TODO: PROCEDURAL?

private const val PARENT_QUESTION_COUNT = 1
private const val CHILD_QUESTION_COUNT = 2 // not changeable
private const val PART_A_START = 1
private const val PART_B_START = 31

class Rhythm(database: Connection) : GroupGenerator(
    "rhythm",
    PARENT_QUESTION_COUNT,
    database
) {

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val partAResult = database.prepareStatement("""
            WITH loop(variation, answer, iteration) AS (
                SELECT variation, answer, 1 AS iteration FROM questions_rhythm_1
                UNION ALL
                SELECT variation, answer, iteration + 1 FROM loop LIMIT ?
            )
            SELECT variation, answer FROM loop ORDER BY iteration, RANDOM()
        """.trimIndent()).apply {
            setInt(1, parentQuestionCount)
        }.executeQuery()
        val partBResult = database.prepareStatement("""
            WITH loop(variation, answer, iteration) AS (
                SELECT variation, answer, 1 AS iteration FROM questions_rhythm_2
                UNION ALL
                SELECT variation, answer, iteration + 1 FROM loop LIMIT ?
            )
            SELECT variation, answer FROM loop ORDER BY iteration, RANDOM()
        """.trimIndent()).apply {
            setInt(1, parentQuestionCount)
        }.executeQuery()

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            partAResult.next()
            partBResult.next()

            val partAVariation = partAResult.getInt("variation")
            val partBVariation = partBResult.getInt("variation")
            ParentQuestion(
                number = parentIndex + 1,
                childQuestions = listOf(
                    generateChildQuestion(1, partAVariation, context.getString("rhythm_question_1_$partAVariation"), partAResult.getString("answer")),
                    generateChildQuestion(2, partBVariation, context.getString("rhythm_question_2_$partBVariation"), partBResult.getString("answer"))
                )
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = parentQuestions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("rhythm_question_desc")
                )
            )
        )
    }

    private fun generateChildQuestion(number: Int, variation: Int, descriptionContent: String, answer: String): TextInputQuestion {
        return TextInputQuestion(
            number = number,
            descriptions = listOf(
                Description(
                    Description.Type.TextSpannable,
                    descriptionContent
                )
            ),
            answers = listOf(TextInputQuestion.Answer(null, listOf(answer))),
            inputType = TextInputQuestion.InputType.Number
        )
    }

}