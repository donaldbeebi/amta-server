package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.question.Description
import java.sql.Connection
import java.util.*

class TimeSignature(database: Connection) : GroupGenerator("time_signature", database) {

    private val randomForTimeSignatures = RandomIntegerGeneratorBuilder
        .generator()
        .withLowerBound(0)
        .withUpperBound(TIME_SIGNATURE_POSSIBLE_ANSWERS.size - 1)
        .build()

    private val randomForOptions: RandomIntegerGenerator = RandomIntegerGeneratorBuilder
        .generator()
        .withLowerBound(0).withUpperBound(TIME_SIGNATURE_POSSIBLE_ANSWERS.size - 1)
        .build()

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        /*
        val result = database.prepareStatement(
            "SELECT variation, time_signature FROM questions_time_signature " +
                    "ORDER BY RANDOM() LIMIT " + NO_OF_QUESTIONS + ";"
        ).executeQuery()
         */
        // TODO: EXCLUDE CORRECT ANSWERS FROM THE POOL BEFORE HAND
        val results = ArrayList<Pair<Int, String>>(NO_OF_QUESTIONS)
        repeat(NO_OF_QUESTIONS) {
            val whereClause = run {
                if (results.size == 0) {
                    ""
                } else {
                    val builder = StringBuilder()
                    builder.append("WHERE time_signature NOT IN (")
                    results.forEachIndexed { index, result ->
                        builder.append("'${result.second}'")
                        if (index != results.size - 1) builder.append(", ")
                    }
                    builder.append(")")
                }
            }

            val result = database.prepareStatement("""
                SELECT variation, time_signature
                FROM questions_time_signature $whereClause
                ORDER BY RANDOM() LIMIT 1;
            """.trimIndent()).executeQuery()
            result.next()

            val variation = result.getInt("variation")
            val correctTimeSignature = result.getString("time_signature")
            val correctTimeSignatureIndex = timeSignatureIndex(result.getString("time_signature"))
            results += Pair(variation, correctTimeSignature)
            randomForOptions.exclude(correctTimeSignatureIndex)
        }


        val questions = List(NO_OF_QUESTIONS) { index ->
            val (variation, timeSignature) = results[index]

            val options = List(NO_OF_OPTIONS) { i ->
                if (i == 0) {
                    "a_time_signature_$timeSignature"
                } else {
                    val randomIndex = randomForOptions.nextInt()
                    randomForOptions.exclude(randomIndex)
                    "a_time_signature_" + TIME_SIGNATURE_POSSIBLE_ANSWERS[randomIndex]
                }
            }
            val dispositions = options.shuffle()

            MultipleChoiceQuestion(
                number = index + 1,
                optionType = MultipleChoiceQuestion.OptionType.Image,
                options = options,
                descriptions = listOf( // TODO: DEBUG
                    Description(
                        Description.Type.Image,
                        "q_time_signature_$variation"
                    ),
                    /*
                    // TODO: DEBUG REMOVE THIS
                    Description(
                        Description.Type.Text,
                        timeSignature
                    )

                     */
                ),
                answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
            )
        }
        randomForOptions.clearAllExcludedIntegers()

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("time_signature_question_desc")
                )
            )
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 3
        private const val NO_OF_OPTIONS = 3
        private val TIME_SIGNATURE_POSSIBLE_ANSWERS = arrayOf(
            "2_2", "2_4", "2_8", "3_2", "3_4", "3_8", "4_2", "4_4", "4_8", "5_4", "5_8",
            "6_4", "6_8", "6_16", "7_4", "7_8", "9_4", "9_8", "9_16", "12_4", "12_8", "12_16"
        )

        private fun timeSignatureIndex(timeSignature: String): Int {
            for (i in TIME_SIGNATURE_POSSIBLE_ANSWERS.indices) {
                if (timeSignature == TIME_SIGNATURE_POSSIBLE_ANSWERS[i]) {
                    return i
                }
            }
            return -1
        }

    }

}