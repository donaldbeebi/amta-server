package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.EnglishNumberConverter
import com.donald.abrsmappserver.utils.music.Key
import com.donald.abrsmappserver.utils.music.Mode
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.question.Description
import java.sql.Connection

class MelodyKey(database: Connection) : GroupGenerator("melody_key", database) {

    private val randomForWrongOptions: RandomIntegerGenerator = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(NUMBER_OF_POSSIBLE_WRONG_OPTIONS - 1)
        .build()

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement(
            "SELECT variation, key FROM questions_melody_key " +
                    "ORDER BY RANDOM() LIMIT $NO_OF_QUESTIONS;"
        ).executeQuery()
        val questions = List(NO_OF_QUESTIONS) { index ->
            result.next()
            val variation = result.getInt("variation")
            val key = Key(result.getString("key"))

            // TODO: OPTIMIZE AWAY ALL THESE OBJECT CREATIONS
            // 3. generating wrong options
            val relativeKey = key.relativeKey()
            val parallelKey = key.parallelKey()
            val wrongKeys = arrayOfNulls<Key>(8)
            wrongKeys[0] = key.adjacentKey(-1)
            wrongKeys[1] = key.adjacentKey(1)
            wrongKeys[2] = parallelKey.adjacentKey(-1)
            wrongKeys[3] = parallelKey.adjacentKey(1)
            wrongKeys[4] = parallelKey
            wrongKeys[5] = relativeKey.adjacentKey(-1)
            wrongKeys[6] = relativeKey.adjacentKey(1)
            wrongKeys[7] = relativeKey

            // excluding the relative key if the key is minor
            if (key.mode() != Mode.MAJOR) randomForWrongOptions.exclude(7)

            val options = List(NO_OF_OPTIONS) { i ->
                if (i == 0) {
                    key.string(context.bundle)
                } else {
                    val chosenIndex = randomForWrongOptions.nextInt()
                    randomForWrongOptions.exclude(chosenIndex)
                    wrongKeys[chosenIndex]!!.string(context.bundle)
                }
            }
            val dispositions = options.shuffle()

            randomForWrongOptions.clearAllExcludedIntegers()

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(Description.Type.Image, "q_melody_key_$variation")
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
                    "Choose the correct key of each of these " +
                            EnglishNumberConverter.convert(NO_OF_QUESTIONS) + " melodies."
                )
            )
        )
    }

    companion object {

        private const val NUMBER_OF_POSSIBLE_WRONG_OPTIONS = 8
        private const val NO_OF_QUESTIONS = 3
        private const val NO_OF_OPTIONS = 4

    }

}