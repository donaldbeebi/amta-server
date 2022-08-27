package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.EnglishNumberConverter
import com.donald.abrsmappserver.utils.music.Key
import com.donald.abrsmappserver.utils.music.Mode
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.DynamicIntRandom
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.buildConstraint
import java.sql.Connection

private const val PARENT_QUESTION_COUNT = 3
private const val CHILD_QUESTION_COUNT = 1
private const val OPTION_COUNT = 4

class MelodyKey(database: Connection) : GroupGenerator(
    "melody_key",
    PARENT_QUESTION_COUNT,
    database
) {

    /*
    private val randomForWrongOptions: RandomIntegerGenerator = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(NUMBER_OF_POSSIBLE_WRONG_OPTIONS - 1)
        .build()
     */

    private val wrongTypeConstraint = buildConstraint {
        withRange(WrongType.values().indices)
    }

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            WITH loop(variation, key, iteration) AS (
                SELECT variation, key, 1 AS iteration FROM questions_melody_key
                UNION ALL
                SELECT variation, key, iteration + 1 FROM loop LIMIT ?
            )
            SELECT variation, key FROM loop ORDER BY iteration, RANDOM()
        """.trimIndent()).apply {
            setInt(1, parentQuestionCount)
        }.executeQuery()

        val questions = List(parentQuestionCount) { index ->
            result.next()
            val variation = result.getInt("variation")
            val key = Key(result.getString("key"))

            // TODO: OPTIMIZE AWAY ALL THESE OBJECT CREATIONS
            // 3. generating wrong options
            /*
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

             */

            // excluding the relative key if the key is minor
            //var options: List<String>
            //var dispositions: Array<Int>
            /*
            val shuffleResult = using(wrongTypeConstraint) { wrongOptionRandom ->
                if (key.mode() != Mode.Major) wrongOptionRandom.exclude(7)

                val options = List(OPTION_COUNT) { i ->
                    if (i == 0) {
                        key.string(context.bundle)
                    } else {
                        val chosenIndex = wrongOptionRandom.generateAndExclude()
                        wrongKeys[chosenIndex]!!.string(context.bundle)
                    }
                }
                options.shuffled()
            }
             */

            val (shuffledOptions, dispositions) = run {
                val random = DynamicIntRandom(wrongTypeConstraint, autoReset = false)
                if (key.mode() != Mode.Major) random.exclude(7)

                val options = List(OPTION_COUNT) { i ->
                    if (i == 0) {
                        key.string(context.bundle)
                    } else {
                        val wrongOptionIndex = random.generateAndExclude()
                        generateWrongKey(key, WrongType.types[wrongOptionIndex]).string(context.bundle)
                    }
                }
                options.shuffled()
            }


            ParentQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(Description.Type.Image, "q_melody_key_$variation")
                ),
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    MultipleChoiceQuestion(
                        number = childIndex + 1,
                        optionType = MultipleChoiceQuestion.OptionType.Text,
                        options = shuffledOptions,
                        answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
                    )
                }
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    "Choose the correct key of each of these " +
                            EnglishNumberConverter.convert(parentQuestionCount) + " melodies."
                )
            )
        )
    }

}

private enum class WrongType {
    AdjAbove, AdjBelow, Para, ParaAbove, ParaBelow, Rel, RelAbove, RelBelow;
    companion object {
        val types = values().toList()
    }
}

private fun generateWrongKey(correctKey: Key, wrongType: WrongType): Key {
    /*
    wrongKeys[0] = key.adjacentKey(-1)
    wrongKeys[1] = key.adjacentKey(1)
    wrongKeys[2] = parallelKey.adjacentKey(-1)
    wrongKeys[3] = parallelKey.adjacentKey(1)
    wrongKeys[4] = parallelKey
    wrongKeys[5] = relativeKey.adjacentKey(-1)
    wrongKeys[6] = relativeKey.adjacentKey(1)
    wrongKeys[7] = relativeKey
     */
    return when (wrongType) {
        WrongType.AdjAbove -> correctKey.adjacentKey(1)
        WrongType.AdjBelow -> correctKey.adjacentKey(-1)
        WrongType.Para -> correctKey.parallelKey()
        WrongType.ParaAbove -> correctKey.parallelKey().adjacentKey(1)
        WrongType.ParaBelow -> correctKey.parallelKey().adjacentKey(-1)
        WrongType.Rel -> correctKey.relativeKey()
        WrongType.RelAbove -> correctKey.relativeKey().adjacentKey(1)
        WrongType.RelBelow -> correctKey.relativeKey().adjacentKey(-1)
    }
}