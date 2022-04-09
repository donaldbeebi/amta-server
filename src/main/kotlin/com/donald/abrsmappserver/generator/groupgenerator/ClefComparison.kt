package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.utils.music.Clef
import com.donald.abrsmappserver.utils.music.toAlphabetUpper
import com.donald.abrsmappserver.exercise.Context
import java.lang.StringBuilder
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.TruthQuestion
import java.sql.Connection
import java.util.*

class ClefComparison(database: Connection) : GroupGenerator("clef_comparison", database) {

    init {
        require(Clef.Type.values().size == 4)
    }

    val random = Random()

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {

        val result1 = database.prepareStatement("""
            SELECT variation, treble_rel_oct, bass_rel_oct, alto_rel_oct, tenor_rel_oct
            FROM questions_clef_comparison
            WHERE variation <= 8
            ORDER BY RANDOM() LIMIT 1;
        """.trimIndent()).executeQuery()
        result1.next()

        val variation = result1.getInt("variation")

        // exclude and shuffle
        var selectedClefs = List(Clef.Type.values().size) { index ->
            val selectedClef = Clef.Type.values()[index]
            Pair(selectedClef, result1.getInt("${selectedClef.string()}_rel_oct"))
        }.sortedBy {
            // sorting it to ensure no two clefs are more than 1 octave apart (please verify)
            it.second
        }

        when (checkClefs(selectedClefs)) {
            Possibility.FIRST_THREE -> {
                selectedClefs = selectedClefs.subList(0, 3)
            }
            Possibility.LAST_THREE -> {
                selectedClefs = selectedClefs.subList(1, 4)
            }
            Possibility.BOTH -> {
                selectedClefs = if (random.nextBoolean()) {
                    selectedClefs.subList(0, 3)
                } else {
                    selectedClefs.subList(1, 4)
                }
            }
            Possibility.NONE -> throw IllegalStateException()
        }
        selectedClefs.shuffle()

        val numberOfClefsToCompare = selectedClefs.size
        val groupDescriptions = ArrayList<Description>(numberOfClefsToCompare * 2 + 1).apply {
            add(
                Description(
                    Description.Type.Text,
                    context.getString("clef_comparison_question_desc")
                )
            )
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
                        "q_clef_comparison_${variation}_${selectedClefs[i].first.string()}"
                    )
                )
            }
        }

        val questions = List(NO_OF_QUESTIONS) { index ->
            // TODO: ENSURE THERE IS AT LEAST 1 TRUE AND AT LEAST 1 FALSE (MAYBE NOT)
            // TODO: MAKE THIS NOT HARD CODED
            val answerIsTrue = random.nextBoolean()

            val firstClefIndex: Int
            val secondClefIndex: Int
            when (index) {
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
                        context.getString("clef_comparison_lower", firstClefIndex.toAlphabetUpper(), secondClefIndex.toAlphabetUpper())
                    } else {
                        if (random.nextBoolean()) {
                            context.getString("clef_comparison_same", firstClefIndex.toAlphabetUpper(), secondClefIndex.toAlphabetUpper())
                        } else {
                            context.getString("clef_comparison_higher", firstClefIndex.toAlphabetUpper(), secondClefIndex.toAlphabetUpper())
                        }
                    }
                }

                0 -> {
                    if (answerIsTrue) {
                        context.getString("clef_comparison_same", firstClefIndex.toAlphabetUpper(), secondClefIndex.toAlphabetUpper())
                    } else {
                        if (random.nextBoolean()) {
                            context.getString("clef_comparison_higher", firstClefIndex.toAlphabetUpper(), secondClefIndex.toAlphabetUpper())
                        } else {
                            context.getString("clef_comparison_lower", firstClefIndex.toAlphabetUpper(), secondClefIndex.toAlphabetUpper())
                        }
                    }
                }

                1 -> {
                    if (answerIsTrue) {
                        context.getString("clef_comparison_higher", firstClefIndex.toAlphabetUpper(), secondClefIndex.toAlphabetUpper())
                    } else {
                        if (random.nextBoolean()) {
                            context.getString("clef_comparison_same", firstClefIndex.toAlphabetUpper(), secondClefIndex.toAlphabetUpper())
                        } else {
                            context.getString("clef_comparison_lower", firstClefIndex.toAlphabetUpper(), secondClefIndex.toAlphabetUpper())
                        }
                    }
                }

                else -> {
                    // TODO: THIS HAPPENED
                    throw IllegalStateException()
                }

            }

            TruthQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.TextEmphasize,
                        truthStatement
                    )
                ),
                answer = TruthQuestion.Answer(null, answerIsTrue)
            )
        }

        /*
        val result = database.prepareStatement(
            "SELECT variation, $SELECTION_STRING " +
                    "FROM questions_clef_comparison ORDER BY RANDOM() LIMIT 1;"
        ).executeQuery()
        result.next()
        val variation = result.getInt("variation")

        val descriptions = ArrayList<Description>(NO_OF_QUESTIONS * 2 + 1)
        descriptions += Description(
            Description.Type.TEXT,
            bundle.getString("clef_comparison_question_desc")
        )
        val barString: String = bundle.getString("clef_comparison_bar_string")
        for (i in 0 until NO_OF_QUESTIONS) {
            descriptions += Description(
                Description.Type.TEXT,
                barString + " " + ('A'.code + i).toChar()
            )
            descriptions += Description(
                Description.Type.IMAGE,
                "q_clef_comparison_" + variation + "_" + (i + 1)
            )
        }

        val questions = List(NO_OF_QUESTIONS) { index ->
            TruthQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.TEXT_EMPHASIZE,
                        result.getString("statement_" + (index + 1))
                    )
                ),
                answer = TruthQuestion.Answer(null, result.getInt("truth_" + (index + 1)) == 1)
            )
        }

        return QuestionGroup(
            name = getGroupName(bundle),
            number = groupNumber,
            questions = questions,
            descriptions = descriptions
        )

         */

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = groupDescriptions
        )
    }

    private fun checkClefs(clefs: List<Pair<Clef.Type, Int>>): Possibility {
        assert(clefs.isSorted())
        // checking the first three
        val firstThreePossible = (clefs[2].second - clefs[0].second) <= 1
        val lastThreePossible = (clefs[3].second - clefs[1].second) <= 1
        return when {
            firstThreePossible && lastThreePossible -> Possibility.BOTH
            firstThreePossible && !lastThreePossible -> Possibility.FIRST_THREE
            !firstThreePossible && lastThreePossible -> Possibility.LAST_THREE
            !firstThreePossible && !lastThreePossible -> Possibility.NONE
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

    private enum class Possibility { FIRST_THREE, LAST_THREE, BOTH, NONE }

    companion object {

        private const val NO_OF_QUESTIONS = 3
        private val SELECTION_STRING: String = run {
            val builder = StringBuilder()
            for (i in 0 until NO_OF_QUESTIONS) {
                if (i != 0) builder.append(", ")
                builder.append("statement_").append(i + 1).append(", ")
                    .append("truth_").append(i + 1)
            }
            builder.toString()
        }

    }

}