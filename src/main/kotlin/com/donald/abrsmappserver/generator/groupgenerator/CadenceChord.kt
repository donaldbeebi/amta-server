package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.*
import com.donald.abrsmappserver.utils.music.ChordNumber
import java.lang.IllegalStateException
import java.sql.Connection
import java.sql.ResultSet

private const val CHILD_QUESTION_COUNT = 5
private const val PARENT_QUESTION_COUNT = 1
private val OPTION_COUNT = ChordNumber.values().size
private val OPTION_LIST = List<String>(OPTION_COUNT) { index ->
    ChordNumber.values()[index].string()
}

class CadenceChord(database: Connection) : GroupGenerator(
    "cadence_chord",
    PARENT_QUESTION_COUNT,
    database
) {

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            WITH RECURSIVE loop(variation, chord_number_1, chord_number_2, chord_number_3, chord_number_4, chord_number_5, iteration) AS (
                SELECT variation, chord_number_1, chord_number_2, chord_number_3, chord_number_4, chord_number_5, 1 AS iteration FROM questions_cadence_chord
                UNION ALL
                SELECT variation, chord_number_1, chord_number_2, chord_number_3, chord_number_4, chord_number_5, iteration + 1 from loop
                LIMIT ?
            )
            SELECT variation, chord_number_1, chord_number_2, chord_number_3, chord_number_4, chord_number_5 FROM loop
            ORDER BY iteration, RANDOM()
        """.trimIndent()).apply {
            setInt(1, parentQuestionCount)
        }.executeQuery()

        result.next()
        val variation = result.getInt("variation")

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf (
                    Description(
                        Description.Type.Image,
                        "q_cadence_chord_$variation"
                    )
                ),
                childQuestions = List<ChildQuestion>(CHILD_QUESTION_COUNT) { childIndex -> generateChildQuestion(childIndex, result, context) }
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("cadence_chord_group_desc")
                )
            ),
            parentQuestions = parentQuestions
        )

    }

    private fun generateChildQuestion(childIndex: Int, result: ResultSet, context: Context): MultipleChoiceQuestion {
        val answerString = result.getString("chord_number_${childIndex + 1}")
        val correctOptionIndices = if (answerString.contains(',')) {
            val answerStrings = answerString.split(',')
            List(answerStrings.size) { i ->
                ChordNumber.fromString(answerStrings[i])?.ordinal ?: throw IllegalStateException(
                    result.getString(answerStrings[i]) + " is not a valid chord number."
                )
            }
        } else {
            val currentNumber = ChordNumber.fromString(
                result.getString("chord_number_${childIndex + 1}")
            ) ?: throw IllegalStateException(
                result.getString("chord_number_${childIndex + 1}") + " is not a valid chord number."
            )
            listOf(currentNumber.ordinal)
        }

        return MultipleChoiceQuestion(
            number = childIndex + 1,
            descriptions = emptyList(),
            optionType = MultipleChoiceQuestion.OptionType.Text,
            options = OPTION_LIST.toList(),
            answer = MultipleChoiceQuestion.Answer(null, correctOptionIndices),
            inputHint = context.getString("cadence_chord_input_hint", childIndex + 1)
        )
    }

}