package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.Description
import java.sql.Connection
import java.util.*

class MusicalTerm(database: Connection) : GroupGenerator("musical_term", database) {

    private val random = Random()

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val result1 = database.prepareStatement(
            "SELECT id FROM data_terms " +
                    "ORDER BY RANDOM() LIMIT ?;"
        ).apply {
            setInt(1, NO_OF_QUESTIONS)
        }.executeQuery()

        val questions = List(NO_OF_QUESTIONS) { index ->
            result1.next()
            var result2 = database.prepareStatement(
                "SELECT name, meaning_id FROM data_terms " +
                        "WHERE id = ?;"
            ).apply {
                setInt(1, result1.getInt("id"))
            }.executeQuery()
            result2.next()
            val questionDescriptions = listOf(
                Description(
                    Description.Type.TextEmphasize,
                    result2.getString("name")
                )
            )
            val meaningId = result2.getInt("meaning_id")

            // getting the correct option
            result2 = database.prepareStatement(
                "SELECT primary_string_key, secondary_string_key FROM data_term_meanings " +
                        "WHERE id = ?;"
            ).apply {
                setInt(1, meaningId)
            }.executeQuery()
            result2.next()
            var primaryStringKey = result2.getString("primary_string_key")
            var secondaryStringKey = result2.getString("secondary_string_key")
            var secondaryNull = result2.wasNull()
            val correctOption = context.getString(
                if (!secondaryNull && random.nextBoolean()) secondaryStringKey else primaryStringKey
            )

            // adding wrong options
            result2 = database.prepareStatement(
                "SELECT primary_string_key, secondary_string_key FROM data_term_meanings " +
                        "WHERE id != ? " +
                        "ORDER BY RANDOM() LIMIT ?;"
            ).apply{
                setInt(1, meaningId)
                setInt(2, NO_OF_OPTIONS - 1)
            }.executeQuery()
            val options = List(NO_OF_OPTIONS) { i ->
                if (i == 0) {
                    correctOption
                } else {
                    result2.next()
                    primaryStringKey = result2.getString("primary_string_key")
                    secondaryStringKey = result2.getString("secondary_string_key")
                    secondaryNull = result2.wasNull()
                    context.getString(
                        if (!secondaryNull && random.nextBoolean()) secondaryStringKey else primaryStringKey
                    )
                }
            }
            val dispositions = options.shuffle()

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = questionDescriptions,
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
                    context.getString("musical_term_group_desc")
                )
            )
        )
    }

    companion object {

        // TODO: SORROW V.S. SORROWFUL?
        private const val NO_OF_QUESTIONS = 3
        private const val NO_OF_OPTIONS = 4

    }

}