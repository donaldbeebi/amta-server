package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.Section7GroupGenerator
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.TruthQuestion
import java.sql.Connection

private const val PARENT_QUESTION_COUNT = 5
private const val CHILD_QUESTION_COUNT = 1

class ScoreFact(database: Connection) : Section7GroupGenerator(
    "score_fact",
    testParentQuestionCount = PARENT_QUESTION_COUNT,
    maxParentQuestionCount = PARENT_QUESTION_COUNT,
    database
) {

    override fun generateGroup(sectionVariation: Int, sectionGroupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            SELECT number, answer FROM questions_score_fact
            WHERE section_variation = ?
            ORDER BY RANDOM() LIMIT ?;
        """.trimIndent()).apply{
            setInt(1, sectionVariation)
            setInt(2, parentQuestionCount)
        }.executeQuery()

        val questions = List(parentQuestionCount) { parentIndex ->
            result.next()
            val statement = context.getString("score_fact_truth_statement_${sectionVariation}_${result.getInt("number")}")
            val isTrue = result.getInt("answer") != 0

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf(Description(Description.Type.TextEmphasize, statement)),
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    TruthQuestion(
                        number = childIndex + 1,
                        answer = TruthQuestion.Answer(null, isTrue)
                    )
                }
            )
        }

        return QuestionGroup(
            number = sectionGroupNumber,
            name = getGroupName(context.bundle),
            descriptions = listOf(Description(Description.Type.Text, context.getString("general_truth_group_desc"))),
            parentQuestions = questions
        )
    }

}