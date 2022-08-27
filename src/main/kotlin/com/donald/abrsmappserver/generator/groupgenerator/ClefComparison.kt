package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.utils.music.Clef
import com.donald.abrsmappserver.utils.music.toAlphabetUpperFromZero
import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.TruthQuestion
import java.sql.Connection
import java.util.*

private const val PARENT_QUESTION_COUNT = 1
private const val CHILD_QUESTION_COUNT = 3

class ClefComparison(database: Connection) : GroupGenerator(
    "clef_comparison",
    PARENT_QUESTION_COUNT,
    database
) {

    init {
        require(Clef.Type.values().size == 4)
    }

    private val random = Random()

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        /*
        val result1 = database.prepareStatement("""
            SELECT variation, treble_rel_oct, bass_rel_oct, alto_rel_oct, tenor_rel_oct
            FROM questions_clef_comparison
            WHERE variation <= 8
            ORDER BY RANDOM() LIMIT 1;
        """.trimIndent()).executeQuery()
        result1.next()
         */

        val result = database.prepareStatement("""
            WITH loop(variation, treble_rel_oct, bass_rel_oct, alto_rel_oct, tenor_rel_oct, iteration) AS (
                SELECT variation, treble_rel_oct, bass_rel_oct, alto_rel_oct, tenor_rel_oct, 1 AS iteration FROM questions_clef_comparison
                UNION ALL
                SELECT variation, treble_rel_oct, bass_rel_oct, alto_rel_oct, tenor_rel_oct, iteration + 1 FROM loop LIMIT ?
            )
            SELECT variation, treble_rel_oct, bass_rel_oct, alto_rel_oct, tenor_rel_oct FROM loop ORDER BY iteration, RANDOM()
        """.trimIndent()).apply {
            setInt(1, parentQuestionCount)
        }.executeQuery()

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            result.next()

            val variation = result.getInt("variation")

            // exclude and shuffle
            var selectedClefs = List(Clef.Type.values().size) { index ->
                val selectedClef = Clef.Type.values()[index]
                Pair(selectedClef, result.getInt("${selectedClef.string()}_rel_oct"))
            }.sortedBy {
                // sorting it to ensure no two clefs are more than 1 octave apart (please verify)
                it.second
            }

            when (checkClefs(selectedClefs)) {
                Possibility.FirstThree -> {
                    selectedClefs = selectedClefs.subList(0, 3)
                }
                Possibility.LastThree -> {
                    selectedClefs = selectedClefs.subList(1, 4)
                }
                Possibility.Both -> {
                    selectedClefs = if (random.nextBoolean()) {
                        selectedClefs.subList(0, 3)
                    } else {
                        selectedClefs.subList(1, 4)
                    }
                }
                Possibility.None -> throw IllegalStateException()
            }
            val (shuffledClefs, _) = selectedClefs.shuffled()

            val numberOfClefsToCompare = shuffledClefs.size

            val parentQuestionDescriptions = ArrayList<Description>(numberOfClefsToCompare * 2).apply {
                repeat(numberOfClefsToCompare) { i ->
                    add(
                        Description(
                            Description.Type.Text,
                            context.getString("clef_comparison_bar_string", ('A'.code + i).toChar().toString())
                        )
                    )
                    add(
                        Description(
                            Description.Type.Image,
                            "q_clef_comparison_${variation}_${shuffledClefs[i].first.string()}"
                        )
                    )
                }
            }

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = parentQuestionDescriptions,
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex -> generateChildQuestion(childIndex, shuffledClefs, context) }
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = parentQuestions,
            descriptions = listOf (
                Description(
                    Description.Type.Text,
                    context.getString("clef_comparison_question_desc")
                )
            )
        )
    }

    private fun checkClefs(clefs: List<Pair<Clef.Type, Int>>): Possibility {
        assert(clefs.isSorted())
        // checking the first three
        val firstThreePossible = (clefs[2].second - clefs[0].second) <= 1
        val lastThreePossible = (clefs[3].second - clefs[1].second) <= 1
        return when {
            firstThreePossible && lastThreePossible -> Possibility.Both
            firstThreePossible && !lastThreePossible -> Possibility.FirstThree
            !firstThreePossible && lastThreePossible -> Possibility.LastThree
            !firstThreePossible && !lastThreePossible -> Possibility.None
            else -> throw IllegalStateException()
        }
    }

    private fun List<Pair<Clef.Type, Int>>.isSorted(): Boolean {
        this.forEachIndexed { index, pair ->
            if (index == 0) return@forEachIndexed
            val previousPair = this[index - 1]
            if (pair.second < previousPair.second) return false
        }
        return true
    }

    private fun generateChildQuestion(childIndex: Int, selectedClefs: List<Pair<Clef.Type, Int>>, context: Context): TruthQuestion {
        // TODO: ENSURE THERE IS AT LEAST 1 TRUE AND AT LEAST 1 FALSE (MAYBE NOT)
        // TODO: MAKE THIS NOT HARD CODED
        val answerIsTrue = random.nextBoolean()

        val firstClefIndex: Int
        val secondClefIndex: Int
        when (childIndex) {
            0 -> { firstClefIndex = 0; secondClefIndex = 1 }
            1 -> { firstClefIndex = 0; secondClefIndex = 2 }
            2 -> { firstClefIndex = 1; secondClefIndex = 2 }
            else -> { throw IllegalStateException() }
        }
        val firstClef = selectedClefs[firstClefIndex]
        val secondClef = selectedClefs[secondClefIndex]

        val truthStatement = when (firstClef.second - secondClef.second) {

            -1 -> {
                if (answerIsTrue) {
                    context.getString("clef_comparison_lower", firstClefIndex.toAlphabetUpperFromZero(), secondClefIndex.toAlphabetUpperFromZero())
                } else {
                    if (random.nextBoolean()) {
                        context.getString("clef_comparison_same", firstClefIndex.toAlphabetUpperFromZero(), secondClefIndex.toAlphabetUpperFromZero())
                    } else {
                        context.getString("clef_comparison_higher", firstClefIndex.toAlphabetUpperFromZero(), secondClefIndex.toAlphabetUpperFromZero())
                    }
                }
            }

            0 -> {
                if (answerIsTrue) {
                    context.getString("clef_comparison_same", firstClefIndex.toAlphabetUpperFromZero(), secondClefIndex.toAlphabetUpperFromZero())
                } else {
                    if (random.nextBoolean()) {
                        context.getString("clef_comparison_higher", firstClefIndex.toAlphabetUpperFromZero(), secondClefIndex.toAlphabetUpperFromZero())
                    } else {
                        context.getString("clef_comparison_lower", firstClefIndex.toAlphabetUpperFromZero(), secondClefIndex.toAlphabetUpperFromZero())
                    }
                }
            }

            1 -> {
                if (answerIsTrue) {
                    context.getString("clef_comparison_higher", firstClefIndex.toAlphabetUpperFromZero(), secondClefIndex.toAlphabetUpperFromZero())
                } else {
                    if (random.nextBoolean()) {
                        context.getString("clef_comparison_same", firstClefIndex.toAlphabetUpperFromZero(), secondClefIndex.toAlphabetUpperFromZero())
                    } else {
                        context.getString("clef_comparison_lower", firstClefIndex.toAlphabetUpperFromZero(), secondClefIndex.toAlphabetUpperFromZero())
                    }
                }
            }

            else -> {
                throw IllegalStateException()
            }

        }

        return TruthQuestion(
            number = childIndex + 1,
            descriptions = listOf(
                Description(
                    Description.Type.TextEmphasize,
                    truthStatement
                )
            ),
            answer = TruthQuestion.Answer(null, answerIsTrue)
        )
    }

    private enum class Possibility { FirstThree, LastThree, Both, None }

}