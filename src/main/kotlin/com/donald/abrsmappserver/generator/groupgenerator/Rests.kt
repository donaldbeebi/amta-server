package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import java.lang.StringBuilder
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.CheckBoxQuestion
import com.donald.abrsmappserver.question.Description
import java.sql.Connection

class Rests(database: Connection) : GroupGenerator("rests", database) {

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement(
            "SELECT variation, " + SELECTION_STRING + " FROM questions_rests " +
                    "ORDER BY RANDOM() LIMIT " + NO_OF_QUESTIONS + ";"
        ).executeQuery()

        val questions = List(NO_OF_QUESTIONS) { index ->
            val variation = result.getInt("variation")

            val answers = List(NO_OF_ANSWERS) { i ->
                CheckBoxQuestion.Answer(null, result.getInt("truth_" + (i + 1)) == 1)
            }

            CheckBoxQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Image,
                        "q_rests_$variation"
                    )
                ),
                answers = answers
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("rests_question_desc")
                )
            )
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 1
        private const val NO_OF_ANSWERS = 3
        private val SELECTION_STRING: String = run {
            val builder = StringBuilder()
            for (i in 0 until NO_OF_ANSWERS) {
                if (i != 0) builder.append(", ")
                builder.append("truth_").append(i + 1)
            }
            builder.toString()
        }

    }

}