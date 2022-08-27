package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import java.sql.Connection
import java.util.*

// TODO: SORROW V.S. SORROWFUL?
private const val PARENT_QUESTION_COUNT = 3
private const val CHILD_QUESTION_COUNT = 1
private const val OPTION_COUNT = 4

class MusicalTerm(database: Connection) : GroupGenerator(
    "musical_term",
    PARENT_QUESTION_COUNT,
    database
) {

    private val random = Random()

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val result1 = database.prepareStatement("""
            WITH RECURSIVE loop(id, iteration) AS (
                SELECT id, 1 AS iteration FROM data_terms
                UNION ALL
                SELECT id, iteration + 1 FROM loop LIMIT ?
            )
            SELECT id FROM loop ORDER BY iteration, RANDOM()
        """.trimIndent()).apply {
            setInt(1, parentQuestionCount)
        }.executeQuery()

        val questions = List(parentQuestionCount) { parentIndex ->
            result1.next()
            var result2 = database.prepareStatement(
                "SELECT name, meaning_id FROM data_terms " +
                        "WHERE id = ?;"
            ).apply {
                setInt(1, result1.getInt("id"))
            }.executeQuery()
            result2.next()
            val parentDescriptions = listOf(
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
                setInt(2, OPTION_COUNT - 1)
            }.executeQuery()
            val options = List(OPTION_COUNT) { i ->
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

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = parentDescriptions,
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
                    context.getString("musical_term_group_desc")
                )
            )
        )
    }

}