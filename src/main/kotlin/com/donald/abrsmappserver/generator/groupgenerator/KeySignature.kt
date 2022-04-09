package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Clef
import com.donald.abrsmappserver.utils.music.Key
import com.donald.abrsmappserver.utils.music.Mode
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.question.Description
import java.sql.Connection

class KeySignature(database: Connection) : GroupGenerator("key_signature", database) {

    private val randomForClefs = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(Clef.Type.values().size - 1)
        .build()

    // for choosing key signature based on number of accidentals
    private val randomForAccidentals = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(-6)
        .withUpperBound(5)
        .excludingIf { n: Int -> n >= -2 && n <= 2 }
        .build()

    private val randomForMajorMinor = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(1)
        .build()

    private val randomForWrongNumberOfAccidentals = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(1)
        .withUpperBound(2)
        .build()

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {

        val questions = List(NO_OF_QUESTIONS) { index ->
            val correctClefIndex = randomForClefs.nextInt()
            val correctClef = Clef.Type.values()[correctClefIndex]
            randomForClefs.exclude(correctClefIndex)
            val correctAccidentals = randomForAccidentals.nextInt()
            if (correctAccidentals > 0) randomForAccidentals.excludeIf { n: Int -> n > 0 } else randomForAccidentals.excludeIf { n: Int -> n < 0 }
            val key: Key
            val generatesMajor = randomForMajorMinor.nextInt()
            randomForMajorMinor.exclude(generatesMajor)
            key = if (generatesMajor == 1) Key(correctAccidentals, Mode.MAJOR) else Key(correctAccidentals, Mode.N_MINOR)
            var numberOfAccidentals = correctAccidentals
            val accidentalSymbol: Char
            if (numberOfAccidentals > 0) {
                accidentalSymbol = 's'
            } else {
                accidentalSymbol = 'b'
                numberOfAccidentals = -numberOfAccidentals
            }

            val options = List(NO_OF_OPTIONS) { i ->
                if (i == 0) {
                    "g_key_signature_" + correctClef.string() + "_" + accidentalSymbol + numberOfAccidentals
                }
                else {
                    var wrongClefIndex = i - 1
                    if (wrongClefIndex >= correctClefIndex) wrongClefIndex += 1
                    val wrongClef = Clef.Type.values()[wrongClefIndex]
                    val numberOfWrongAccidentals = randomForWrongNumberOfAccidentals.nextInt()
                    "g_key_signature_" + wrongClef.string() + "_" + accidentalSymbol + numberOfAccidentals + "w" + numberOfWrongAccidentals
                }
            }
            val dispositions = options.shuffle()
            randomForClefs.clearAllExcludedIntegers()

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Text,
                        context.getString("key_signature_question_desc", key.string(context.bundle))
                    )
                ),
                optionType = MultipleChoiceQuestion.OptionType.Image,
                options = options,
                answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
            )
        }

        randomForAccidentals.clearAllExcludedIntegers()
        randomForMajorMinor.clearAllExcludedIntegers()

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = emptyList()
        )
    }

    companion object {
        private const val NO_OF_QUESTIONS = 2
        private const val NO_OF_OPTIONS = 4
    }

}