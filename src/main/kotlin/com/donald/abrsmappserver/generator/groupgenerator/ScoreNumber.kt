package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.Section7GroupGenerator
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.TextInputQuestion
import com.donald.abrsmappserver.utils.getIntOrNull
import java.sql.Connection

private const val PARENT_QUESTION_COUNT = 2
private const val CHILD_QUESTION_COUNT = 1

class ScoreNumber(database: Connection) : Section7GroupGenerator(
    "score_number",
    testParentQuestionCount = PARENT_QUESTION_COUNT,
    maxParentQuestionCount = PARENT_QUESTION_COUNT,
    database
) {

    override fun generateGroup(sectionVariation: Int, sectionGroupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            SELECT answer_1, answer_2 FROM questions_score_number
            WHERE section_variation = ?
            ORDER BY RANDOM() LIMIT ?;
        """.trimIndent()).apply {
            setInt(1, sectionVariation)
            setInt(2, parentQuestionCount)
        }.executeQuery()

        val questions = List(parentQuestionCount) { parentIndex ->
            result.next()

            val answer1 = result.getInt("answer_1")
            val answer2 = result.getIntOrNull("answer_2")

            val answers = if (answer2 == null) {
                listOf(TextInputQuestion.Answer(null, listOf(answer1.toString())))
            } else {
                listOf(TextInputQuestion.Answer(null, listOf(answer1.toString(), answer2.toString())))
            }

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf(
                    Description(Description.Type.TextEmphasize, context.getString("score_number_statement_${sectionVariation}_${parentIndex + 1}"))
                ),
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    TextInputQuestion(
                        number = childIndex + 1,
                        inputType = TextInputQuestion.InputType.Number,
                        answers = answers
                    )
                }
            )

        }

        return QuestionGroup(
            number = sectionGroupNumber,
            name = getGroupName(context.bundle),
            descriptions = listOf(Description(Description.Type.Text, context.getString("score_number_group_desc"))),
            parentQuestions = questions
        )
    }

}