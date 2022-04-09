package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import java.lang.StringBuilder
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.CheckBoxQuestion
import com.donald.abrsmappserver.question.Description
import java.sql.Connection

class Transposition(database: Connection) : GroupGenerator("transposition", database) {

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement(
            "SELECT variation, direction_string_key, interval_string_key, " +
                    SELECTION_STRING + " FROM questions_transposition " +
                    "ORDER BY RANDOM() LIMIT " + NO_OF_QUESTIONS + ";"
        ).executeQuery()

        val questions = List(NO_OF_QUESTIONS) { index ->
            result.next()
            val variation = result.getInt("variation")
            val transposition: String = context.getString(result.getString("direction_string_key")).toString() + " " +
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

            val answers = List(NO_OF_ANSWERS) { i ->
                if (result.getInt("check_" + (i + 1)) == 0) {
                    CheckBoxQuestion.Answer(null, false)
                } else {
                    CheckBoxQuestion.Answer(null, true)
                }
            }

            CheckBoxQuestion(
                number = index + 1,
                descriptions = descriptions,
                answers = answers
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = emptyList()
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 1
        private const val NO_OF_ANSWERS = 5
        private val SELECTION_STRING: String = run {
            val builder = StringBuilder()
            for (i in 0 until NO_OF_ANSWERS) {
                if (i != 0) builder.append(", ")
                builder.append("check_").append(i + 1)
            }
            builder.toString()
        }

    }

}