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

private const val VARIATION_COUNT = 10
private const val PARENT_QUESTION_COUNT = 1
private const val CHILD_QUESTION_COUNT = 1
private const val OPTION_COUNT = 3

class NoteGrouping(database: Connection) : GroupGenerator(
    "note_grouping",
    PARENT_QUESTION_COUNT,
    database
) {

    private val random = Random()
    /*
    private val randomForWrongOptions = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(1).withUpperBound(OPTION_COUNT)
        .build()

     */
    private val variationConstraint = buildConstraint {
        withRange(1..VARIATION_COUNT)
    }

    private val wrongOptionConstraint = buildConstraint {
        withRange(1..OPTION_COUNT)
    }

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val variationRandom = DynamicIntRandom(variationConstraint, autoReset = true)

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            val variation: Int = variationRandom.generateAndExclude()

            val wrongOptionRandom = DynamicIntRandom(wrongOptionConstraint, autoReset = false)
            val images = List(OPTION_COUNT) { i ->
                if (i == 0) {
                    "a_note_grouping_" + (variation + 1) + "_t"
                } else {
                    "a_note_grouping_" + (variation + 1) + "_f" + wrongOptionRandom.generateAndExclude()
                }
            }

            val (shuffledImages, dispositions) = images.shuffled()
            //val dispositions = ArrayShuffler.shuffle(images)
            val descriptions = ArrayList<Description>(OPTION_COUNT * 2)
            for (i in 0 until OPTION_COUNT) {
                descriptions += Description(
                    Description.Type.Text,
                    context.getString("note_grouping_index", i + 1)
                )
                descriptions += Description(
                    Description.Type.Image,
                    shuffledImages[i]
                )
            }

            val options = List(OPTION_COUNT) { i ->
                context.getString("note_grouping_index", i + 1)
            }

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = descriptions,
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    MultipleChoiceQuestion(
                        number = childIndex + 1,
                        optionType = MultipleChoiceQuestion.OptionType.Text,
                        options = options,
                        answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
                    )
                }
            )
        }

        return QuestionGroup(
            number = groupNumber,
            name = getGroupName(context.bundle),
            parentQuestions = parentQuestions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("note_grouping_question_desc")
                )
            )
        )
    }

}