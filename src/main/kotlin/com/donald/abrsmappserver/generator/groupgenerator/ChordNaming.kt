package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.DiatonicChord
import com.donald.abrsmappserver.utils.music.Music
import com.donald.abrsmappserver.utils.music.ChordNumber
import com.donald.abrsmappserver.utils.music.ChordInversion
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.Constraint
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.DynamicIntRandom
import java.sql.Connection
import java.sql.ResultSet

private const val CHILD_QUESTION_COUNT = 3
private const val PARENT_QUESTION_COUNT = 1
private const val OPTION_COUNT = 4
val HIGHEST_CHORD_ID = Music.idFromChord(
    ChordNumber.values()[ChordNumber.values().size - 1],
    ChordInversion.values()[ChordInversion.values().size - 1]
)

class ChordNaming(database: Connection) : GroupGenerator(
    "chord_naming",
    PARENT_QUESTION_COUNT,
    database
) {

    private val chordConstraint = Constraint.Builder()
        .withRange(0..HIGHEST_CHORD_ID)
        .build()

    /*
    private val randomForChords = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(HIGHEST_CHORD_ID)
        .build()

     */

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            WITH RECURSIVE loop(variation, key, chord_1, chord_2, chord_3, iteration) AS (
                SELECT variation, key, chord_1, chord_2, chord_3, 1 AS iteration FROM questions_chord_naming
                UNION ALL
                SELECT variation, key, chord_1, chord_2, chord_3, iteration + 1 FROM loop LIMIT ?
            )
            SELECT variation, key, chord_1, chord_2, chord_3 FROM loop
            ORDER BY iteration, RANDOM()
        """.trimIndent()).apply{
            setInt(1, parentQuestionCount)
        }.executeQuery()

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            result.next()
            val variation = result.getInt("variation")
            val keyString = result.getString("key")

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Text,
                        context.getString("chord_naming_key_hint", keyString)
                    ),
                    Description(
                        Description.Type.Image,
                        "q_chord_naming_$variation"
                    )
                ),
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex -> generateChildQuestion(childIndex, result, context)}
            )
        }

        // TODO: FIX RANDOM FOR CHORDS EXCLUDE SEQUENCE // update: what?

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = parentQuestions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("chord_naming_group_desc")
                )
            )
        )
    }

    private fun generateChildQuestion(childIndex: Int, result: ResultSet, context: Context): MultipleChoiceQuestion {
        val chordRandom = DynamicIntRandom(chordConstraint, autoReset = true)
        val correctChord = DiatonicChord(result.getString("chord_" + (childIndex + 1)))
        //randomForChords.exclude(correctChord.id())
        chordRandom.exclude(correctChord.id())

        val options = List<String>(OPTION_COUNT) { i ->
            if (i == 0) {
                correctChord.string()
            } else {
                //val wrongId = randomForChords.nextInt()
                val wrongId = chordRandom.generateAndExclude()
                //randomForChords.exclude(wrongId)
                val wrongChord = DiatonicChord(wrongId)
                wrongChord.string()
            }
        }
        val dispositions = options.shuffle()
        val answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
        //randomForChords.clearAllExcludedIntegers() // newly added here, attempting to fix said bug

        return MultipleChoiceQuestion(
            number = childIndex + 1,
            descriptions = emptyList(),
            optionType = MultipleChoiceQuestion.OptionType.Text,
            options = options,
            answer = answer,
            inputHint = context.getString("chord_naming_input_hint", ('A'.code + childIndex).toChar())
        )
    }

}