package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.Description
import java.sql.Connection

class OrnamentNaming(database: Connection) : GroupGenerator("ornament_naming", database) {

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val questions = List(NO_OF_QUESTIONS) { index ->
            var result = database.prepareStatement(
                "SELECT variation, ornament_id FROM questions_ornament_naming " +
                        "ORDER BY RANDOM() LIMIT 1;"
            ).executeQuery()
            result.next()
            val variation = result.getInt("variation")
            val ornamentId = result.getInt("ornament_id")
            result = database.prepareStatement(
                "SELECT string_key FROM data_ornaments " +
                        "WHERE id = ?;"
            ).apply {
                setInt(1, ornamentId)
            }.executeQuery()
            result.next()

            val correctOption = context.getString(result.getString("string_key"))

            result = database.prepareStatement(
                "SELECT string_key FROM data_ornaments " +
                        "WHERE id != ? " +
                        "ORDER BY RANDOM() LIMIT ?;"
            ).apply {
                setInt(1, ornamentId)
                setInt(2, NO_OF_OPTIONS - 1)
            }.executeQuery()

            val options = List(NO_OF_OPTIONS) { i ->
                if (i == 0) {
                    correctOption
                } else {
                    result.next()
                    context.getString(result.getString("string_key"))
                }
            }
            val dispositions = options.shuffle()

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Image,
                        "q_ornament_naming_$variation"
                    )
                ),
                optionType = MultipleChoiceQuestion.OptionType.Text,
                options = options,
                answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("ornament_naming_group_desc")
                )
            )
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 2
        private const val NO_OF_OPTIONS = 4

    }

}