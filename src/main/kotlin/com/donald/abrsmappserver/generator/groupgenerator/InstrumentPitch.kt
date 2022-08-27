package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.Section7GroupGenerator
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.Constraint
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.using
import java.sql.Connection
import java.sql.SQLException

private const val OPTION_COUNT = 4
private const val PARENT_QUESTION_COUNT = 1
private const val CHILD_QUESTION_COUNT = 1

class InstrumentPitch(database: Connection) : Section7GroupGenerator(
    "instrument_pitch",
    testParentQuestionCount = PARENT_QUESTION_COUNT,
    maxParentQuestionCount = PARENT_QUESTION_COUNT,
    database
) {

    private val wrongInstrumentConstraint = Constraint.Builder()
        .withRange(1..4)
        .build()

    /*
    private val randomForWrongInstruments = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(1)
        .withUpperBound(4)
        .build()

     */

    override fun generateGroup(sectionVariation: Int, sectionGroupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        assert(parentQuestionCount == 1)
        val questionResult = database.prepareStatement("""
            SELECT correct_instrument FROM questions_instrument_pitch
            WHERE section_variation = ?
            ORDER BY RANDOM() LIMIT ?;
        """.trimIndent()).apply {
            setInt(1, sectionVariation)
            setInt(2, parentQuestionCount) // TODO: THIS WILL CAUSE A BUG WHEN parentQuestionCount > 1
        }.executeQuery()

        val correctInstrument = questionResult.getString("correct_instrument")
        val correctInstrumentName = context.getString("${correctInstrument}_name")

        val instrumentResult = database.prepareStatement("""
            SELECT wrong_instrument_1, wrong_instrument_2, wrong_instrument_3, wrong_instrument_4
            FROM data_instrument_pitch
            WHERE correct_instrument = ?
        """.trimIndent()).apply {
            setString(1, correctInstrument)
        }.executeQuery()

        val options = using(wrongInstrumentConstraint) { instrumentRandom ->
            List(OPTION_COUNT) { i ->
                if (i == 0) {
                    correctInstrumentName
                }
                else {
                    val wrongInstrumentKey = "wrong_instrument_${instrumentRandom.generateAndExclude()}"
                    val wrongInstrumentName = try {
                        instrumentResult.getString(wrongInstrumentKey)
                    } catch (e: SQLException) {
                        throw SQLException("$wrongInstrumentKey with section variation $sectionVariation")
                    }
                    context.getString("${wrongInstrumentName}_name")
                }
            }
        }

        val dispositions = options.shuffle()

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            ParentQuestion(
                number = parentIndex + 1,
                descriptions = emptyList(),
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    MultipleChoiceQuestion(
                        number = childIndex,
                        descriptions = emptyList(),
                        options = options,
                        optionType = MultipleChoiceQuestion.OptionType.Text,
                        answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
                    )
                }
            )
        }

        return QuestionGroup(
            number = sectionGroupNumber,
            name = getGroupName(context.bundle),
            descriptions = listOf(
                Description(
                    Description.Type.Text, context.getString(
                        "instrument_pitch_group_desc",
                        context.getString("instrument_pitch_bar_$sectionVariation")
                    )
                )
            ),
            parentQuestions = parentQuestions
        )
    }

}