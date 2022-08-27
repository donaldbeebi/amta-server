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
import java.util.*

private const val PARENT_QUESTION_COUNT = 1
private const val CHILD_QUESTION_COUNT = 1
private const val OPTION_COUNT = 3
private val QUESTION_VARIATION_NAMES = arrayOf("1c", "2c", "3c", "4c", "5c", "6c", "1s", "2s", "3s")

class SimpleCompound(database: Connection) : GroupGenerator(
    "simple_compound",
    PARENT_QUESTION_COUNT,
    database
) {

    private val variationConstraint = buildConstraint {
        withRange(QUESTION_VARIATION_NAMES.indices)
    }

    //private val random = Random()

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val variationRandom = DynamicIntRandom(variationConstraint, autoReset = true)
        val questions = List(parentQuestionCount) { parentIndex ->
            val variation: Int = variationRandom.generateAndExclude()
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

            val (shuffledImages, dispositions) = run {
                val images = List(OPTION_COUNT) { i ->
                    "a_simple_compound_" + variationString + "_" + (i + 1)
                }
                images.shuffled()
            }

            val descriptions = ArrayList<Description>(3 + OPTION_COUNT * 2)
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
            for (i in shuffledImages.indices) {
                descriptions += Description(
                    Description.Type.Text,
                    context.getString("simple_compound_index", i + 1)
                )
                descriptions += Description(
                    Description.Type.Image,
                    shuffledImages[i]
                )
            }
            val options = List(OPTION_COUNT) { i ->
                context.getString("simple_compound_index", i + 1)
            }

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = descriptions,
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    MultipleChoiceQuestion(
                        number = childIndex + 1,
                        //descriptions = descriptions,
                        optionType = MultipleChoiceQuestion.OptionType.Text,
                        options = options,
                        answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
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