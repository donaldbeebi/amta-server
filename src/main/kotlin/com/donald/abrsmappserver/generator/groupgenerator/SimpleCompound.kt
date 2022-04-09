package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.Description
import java.sql.Connection
import java.util.*

class SimpleCompound(database: Connection) : GroupGenerator("simple_compound", database) {

    private val random = Random()

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val questions = List(NO_OF_QUESTIONS) { index ->
            val variation: Int = random.nextInt(QUESTION_VARIATION_NAMES.size)
            val variationString = QUESTION_VARIATION_NAMES[variation]
            val arg1: String
            val arg2: String
            if (variationString[variationString.length - 1] == 's') {
                arg1 = context.getString("simple_compound_simple_time_string")
                arg2 = context.getString("simple_compound_compound_time_string")
            } else {
                arg1 = context.getString("simple_compound_compound_time_string")
                arg2 = context.getString("simple_compound_simple_time_string")
            }
            val images = Array(NO_OF_OPTIONS) { i ->
                "a_simple_compound_" + variationString + "_" + (i + 1)
            }
            val dispositions = images.shuffle()

            val descriptions = ArrayList<Description>(3 + NO_OF_OPTIONS * 2)
            descriptions += Description(
                Description.Type.Text,
                context.getString("simple_compound_question_desc_1", arg1)
            )
            descriptions += Description(
                Description.Type.Image,
                "q_simple_compound_$variationString"
            )
            descriptions += Description(
                Description.Type.Text,
                context.getString("simple_compound_question_desc_2", arg2)
            )
            for (i in images.indices) {
                descriptions += Description(
                    Description.Type.Text,
                    context.getString("simple_compound_index", i + 1)
                )
                descriptions += Description(
                    Description.Type.Image,
                    images[i]
                )
            }
            val options = List(NO_OF_OPTIONS) { i ->
                context.getString("simple_compound_index", i + 1)
            }

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = descriptions,
                optionType = MultipleChoiceQuestion.OptionType.Text,
                options = options,
                answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
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
        private const val NO_OF_OPTIONS = 3
        private val QUESTION_VARIATION_NAMES = arrayOf("1c", "2c", "3c", "4c", "5c", "6c", "1s", "2s", "3s")

    }

}