package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.Section7GroupGenerator
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.buildConstraint
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.using
import java.sql.Connection

private const val PARENT_QUESTION_COUNT = 1
private const val CHILD_QUESTION_COUNT = 1
private const val OPTION_COUNT = 4

class NoteCounting(database: Connection) : Section7GroupGenerator(
    "note_counting",
    testParentQuestionCount = PARENT_QUESTION_COUNT,
    maxParentQuestionCount = PARENT_QUESTION_COUNT,
    database
) {

    /*
    private val randomForFirstOptionCountOffset = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(OPTION_COUNT - 1)
        .build()

     */

    private val firstOptionCountOffsetConstraint = buildConstraint {
        withRange(0 until OPTION_COUNT)
    }

    override fun generateGroup(sectionVariation: Int, sectionGroupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            SELECT count FROM questions_note_counting
            WHERE sectionVariation = ?
            LIMIT ?;
        """.trimIndent()).apply {
            setInt(1, sectionVariation)
            setInt(2, parentQuestionCount)
        }.executeQuery()

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            result.next()

            val count = result.getInt("count")
            val firstOptionCountOffset = using(firstOptionCountOffsetConstraint) { random ->
                random.excludeIf { count - it <= 0 }
                random.generate()
            }
            val firstOptionCount = count - firstOptionCountOffset
            val correctAnswerIndex = firstOptionCountOffset


            ParentQuestion(
                number = parentIndex + 1,
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    MultipleChoiceQuestion(
                        number = childIndex + 1,
                        options = List(OPTION_COUNT) { i ->
                            (firstOptionCount + i).toString()
                        },
                        optionType = MultipleChoiceQuestion.OptionType.Text,
                        answer = MultipleChoiceQuestion.Answer(null, correctAnswerIndex)
                    )
                }
            )
        }

        return QuestionGroup(
            number = sectionGroupNumber,
            name = getGroupName(context.bundle),
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("note_counting_group_desc", context.getString("note_counting_note_$sectionVariation"))
                )
            ),
            parentQuestions = parentQuestions
        )
    }

}