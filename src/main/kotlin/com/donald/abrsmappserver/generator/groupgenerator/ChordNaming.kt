package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import java.lang.StringBuilder
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.DiatonicChord
import com.donald.abrsmappserver.utils.music.Music
import com.donald.abrsmappserver.utils.music.ChordNumber
import com.donald.abrsmappserver.utils.music.ChordInversion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.question.Description
import java.sql.Connection

class ChordNaming(database: Connection) : GroupGenerator("chord_naming", database) {

    private val randomForChords = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(HIGHEST_CHORD_ID)
        .build()

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement(
            "SELECT variation, key, $SELECTION_STRING FROM questions_chord_naming " +
                    "ORDER BY RANDOM() LIMIT 1;"
        ).executeQuery()
        result.next()
        val variation = result.getInt("variation")
        val keyString = result.getString("key")

        // TODO: FIX RANDOM FOR CHORDS EXCLUDE SEQUENCE

        val questions = List(NO_OF_QUESTIONS) { index ->
            val correctChord = DiatonicChord(result.getString("chord_" + (index + 1)))
            randomForChords.exclude(correctChord.id())
            //randomForChords.clearAllExcludedIntegers()

            val options = List<String>(NO_OF_OPTIONS) { i ->
                if (i == 0) {
                    correctChord.string()
                } else {
                    val wrongId = randomForChords.nextInt()
                    randomForChords.exclude(wrongId)
                    val wrongChord = DiatonicChord(wrongId)
                    wrongChord.string()
                }
            }
            val dispositions = options.shuffle()
            val answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
            randomForChords.clearAllExcludedIntegers() // newly added here, attempting to fix said bug

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = emptyList(),
                optionType = MultipleChoiceQuestion.OptionType.Text,
                options = options,
                answer = answer,
                inputHint = context.getString("chord_naming_input_hint", ('A'.code + index).toChar())
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("chord_naming_group_desc")
                ),
                Description(
                    Description.Type.Text,
                    context.getString("chord_naming_key_hint", keyString)
                ),
                Description(
                    Description.Type.Image,
                    "q_chord_naming_$variation"
                )
            )
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 3
        private const val NO_OF_OPTIONS = 4
        private val SELECTION_STRING: String = run {
            val builder = StringBuilder()
            for (i in 0 until NO_OF_QUESTIONS) {
                if (i != 0) builder.append(", ")
                builder.append("chord_").append(i + 1)
            }
            builder.toString()
        }
        val HIGHEST_CHORD_ID = Music.idFromChord(
            ChordNumber.values()[ChordNumber.values().size - 1],
            ChordInversion.values()[ChordInversion.values().size - 1]
        )

    }

}