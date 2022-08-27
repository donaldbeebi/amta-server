package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import java.sql.Connection

private const val PARENT_QUESTION_COUNT = 2
private const val CHILD_QUESTION_COUNT = 1
private const val OPTION_COUNT = 4

class OrnamentNaming(database: Connection) : GroupGenerator(
    "ornament_naming",
    PARENT_QUESTION_COUNT,
    database
) {

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val questionResult = database.prepareStatement("""
            WITH loop(variation, ornament_id, iteration) AS (
                SELECT variation, ornament_id, 1 AS iteration FROM questions_ornament_naming
                UNION ALL
                SELECT variation, ornament_id, iteration + 1 FROM loop LIMIT ?
            )
            SELECT variation, ornament_id FROM loop
            ORDER BY iteration, RANDOM()
        """.trimIndent()).apply{
            setInt(1, parentQuestionCount)
        }.executeQuery()
        questionResult.next()

        val questions = List(parentQuestionCount) { parentIndex ->
            val variation = questionResult.getInt("variation")
            val ornamentId = questionResult.getInt("ornament_id")
            val instrumentResult = database.prepareStatement("""
                SELECT string_key
                FROM data_ornaments
                WHERE id = ?;
            """.trimIndent()).apply {
                setInt(1, ornamentId)
            }.executeQuery()
            instrumentResult.next()

            val correctOption = context.getString(instrumentResult.getString("string_key"))

            val ornamentResult = database.prepareStatement("""
                SELECT string_key
                FROM data_ornaments
                WHERE id != ?
                ORDER BY RANDOM() LIMIT ?;
            """.trimIndent()).apply {
                setInt(1, ornamentId)
                setInt(2, OPTION_COUNT - 1)
            }.executeQuery()

            val options = List(OPTION_COUNT) { i ->
                if (i == 0) {
                    correctOption
                } else {
                    ornamentResult.next()
                    context.getString(ornamentResult.getString("string_key"))
                }
            }
            val dispositions = options.shuffle()

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Image,
                        "q_ornament_naming_$variation"
                    )
                ),
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
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("ornament_naming_group_desc")
                )
            )
        )
    }

}