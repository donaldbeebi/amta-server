package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.music.ChordNumber
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.Question
import java.lang.IllegalStateException
import java.sql.Connection

class CadenceChord(database: Connection) : GroupGenerator("cadence_chord", database) {

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            SELECT variation, chord_number_1, chord_number_2, chord_number_3, chord_number_4, chord_number_5
            FROM questions_cadence_chord
            WHERE variation = 12
            ORDER BY RANDOM() LIMIT 1;
        """.trimIndent()).executeQuery()
        result.next()
        val variation = result.getInt("variation")

        val questions = List<Question>(NO_OF_QUESTIONS) { index ->
            val answerString = result.getString("chord_number_${index + 1}")
            val correctOptionIndices = if (answerString.contains(',')) {
                val answerStrings = answerString.split(',')
                List(answerStrings.size) { i ->
                    ChordNumber.fromString(answerStrings[i])?.ordinal ?: throw IllegalStateException(
                        result.getString(answerStrings[i]) + " is not a valid chord number."
                    )
                }
            } else {
                val currentNumber = ChordNumber.fromString(
                    result.getString("chord_number_${index + 1}")
                ) ?: throw IllegalStateException(
                    result.getString("chord_number_${index + 1}") + " is not a valid chord number."
                )
                listOf(currentNumber.ordinal)
            }

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = emptyList(),
                optionType = MultipleChoiceQuestion.OptionType.Text,
                options = OPTION_LIST.toList(),
                answer = MultipleChoiceQuestion.Answer(null, correctOptionIndices),
                inputHint = context.getString("cadence_chord_input_hint", index + 1)
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("cadence_chord_group_desc")
                ),
                Description(
                    Description.Type.Image,
                    "q_cadence_chord_$variation"
                ),
            ),
            questions = questions
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 5
        private val NO_OF_OPTIONS = ChordNumber.values().size
        private val OPTION_LIST = List<String>(NO_OF_OPTIONS) { index ->
            ChordNumber.values()[index].string()
        }

    }

}