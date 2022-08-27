package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Cadence
import java.lang.IllegalArgumentException
import com.donald.abrsmappserver.utils.music.Key
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import java.sql.Connection
import java.sql.ResultSet

private const val CHILD_QUESTION_COUNT = 1
private const val PARENT_QUESTION_COUNT = 2
private const val OPTION_COUNT = 3

class CadenceNaming(database: Connection) : GroupGenerator(
    "cadence_naming",
    PARENT_QUESTION_COUNT,
    database
) {

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            WITH RECURSIVE loop(variation, key, cadence, iteration) AS (
                SELECT variation, key, cadence, 1 AS iteration FROM questions_cadence_naming
                UNION ALL
                SELECT variation, key, cadence, iteration + 1 FROM loop LIMIT ?
            )
            SELECT variation, key, cadence FROM loop
            ORDER BY iteration, RANDOM()
        """.trimIndent()).apply{
            setInt(1, parentQuestionCount)
        }.executeQuery()

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            result.next()
            val variation = result.getInt("variation")
            val key = Key(result.getString("key"))

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Text,
                        context.getString("cadence_naming_key_hint", key.string(context.bundle))
                    ),
                    Description(
                        Description.Type.Image,
                        "q_cadence_naming_$variation"
                    )
                ),
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex -> generateChildQuestion(childIndex, result, context) }
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("cadence_naming_group_desc")
                )
            ),
            parentQuestions = parentQuestions
        )
    }

    private fun generateChildQuestion(childIndex: Int, result: ResultSet, context: Context): MultipleChoiceQuestion {
        val correctCadence = Cadence.fromString(result.getString("cadence"))
            ?: throw IllegalArgumentException(result.getString("cadence") + " is not a cadence.")

        val options = List<String>(OPTION_COUNT) { i ->
            Cadence.values()[i].string(context.bundle)
        }
        val dispositions = options.shuffle()
        val answer = MultipleChoiceQuestion.Answer(null, correctCadence.ordinal + dispositions[correctCadence.ordinal])

        return MultipleChoiceQuestion(
            number = childIndex + 1,
            descriptions = emptyList(),
            optionType = MultipleChoiceQuestion.OptionType.Text,
            options = options,
            answer = answer
        )
    }

}