package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.TextInputQuestion
import com.donald.abrsmappserver.question.Description
import java.sql.Connection

// TODO: PROCEDURAL?
class Rhythm(database: Connection) : GroupGenerator("rhythm", database) {

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            SELECT variation, answer FROM questions_rhythm
            ORDER BY RANDOM() LIMIT $NO_OF_QUESTIONS;
        """.trimIndent()).executeQuery()

        val questions = List(NO_OF_QUESTIONS) { index ->
            result.next()
            val variation = result.getInt("variation")
            val answer = result.getString("answer")

            TextInputQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.TextSpannable,
                        context.getString("rhythm_question_$variation")
                    )
                ),
                answers = listOf(TextInputQuestion.Answer(null, answer)),
                inputType = TextInputQuestion.InputType.Number
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("rhythm_question_desc")
                )
            )
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 3

    }

}