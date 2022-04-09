package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Cadence
import java.lang.IllegalArgumentException
import com.donald.abrsmappserver.utils.music.Key
import com.donald.abrsmappserver.question.Description
import java.sql.Connection

class CadenceNaming(database: Connection) : GroupGenerator("cadence_naming", database) {

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement(
            "SELECT variation, key, cadence FROM questions_cadence_naming " +
                    "ORDER BY RANDOM() LIMIT $NO_OF_QUESTIONS;"
        ).executeQuery()

        val questions = List(NO_OF_QUESTIONS) { index ->
            result.next()
            val variation = result.getInt("variation")
            val key = Key(result.getString("key"))
            val correctCadence = Cadence.fromString(result.getString("cadence"))
                ?: throw IllegalArgumentException(result.getString("cadence") + " is not a cadence.")

            val options = List<String>(NO_OF_OPTIONS) { i ->
                Cadence.values()[i].string(context.bundle)
            }
            val dispositions = options.shuffle()
            val answer = MultipleChoiceQuestion.Answer(null, correctCadence.ordinal + dispositions[correctCadence.ordinal])

            MultipleChoiceQuestion(
                number = index + 1,
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
                optionType = MultipleChoiceQuestion.OptionType.Text,
                options = options,
                answer = answer
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
            questions = questions
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 2
        private const val NO_OF_OPTIONS = 3

    }

}