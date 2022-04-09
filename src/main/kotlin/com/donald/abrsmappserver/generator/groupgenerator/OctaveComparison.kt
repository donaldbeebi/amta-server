package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.randomInt
import com.donald.abrsmappserver.utils.getIntOrNull
import com.donald.abrsmappserver.utils.music.toAlphabetUpper
import java.sql.Connection
import java.util.*

private const val NO_OF_OPTIONS = 4
private const val NO_OF_QUESTIONS = 1

class OctaveComparison(database: Connection) : GroupGenerator("octave_comparison", database) {

    private val random = Random()

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            SELECT octave_comparison_1, combination_1, combination_2, octave_comparison_2, bar
            FROM questions_octave_comparison
            WHERE variation = ?
            ORDER BY RANDOM() LIMIT ?;
        """.trimIndent()).apply {
            setInt(1, context.sectionVariation)
            setInt(2, NO_OF_QUESTIONS)
        }.executeQuery()

        val questions = List(NO_OF_QUESTIONS) { questionIndex ->
            result.next()
            val comparison: OctaveComparison
            val combination: Combination
            run {
                val comparison1 = result.getIntOrNull("octave_comparison_1") ?: throw IllegalStateException()
                val combination1 = result.getString("combination_1") ?: throw IllegalStateException()
                val comparison2: Int? = result.getIntOrNull("octave_comparison_2")
                val combination2: String? = result.getString("combination_2")

                if (comparison2 != null && combination2 != null) {
                    val one = random.nextBoolean()
                    comparison = OctaveComparison.fromInt(if (one) comparison1 else comparison2)
                    combination = Combination.fromString(if (one) combination1 else combination2)
                } else {
                    comparison = OctaveComparison.fromInt(comparison1)
                    combination = Combination.fromString(combination1)
                }
            }

            val wrongComparisons = ArrayList<OctaveComparison>(NO_OF_OPTIONS - 1)
            val wrongCombinations = ArrayList<Combination>(NO_OF_OPTIONS - 1)

            randomInt(
                count = NO_OF_OPTIONS - 1,
                range = 0 until OctaveComparison.comparisons.size,
                excluded = intArrayOf(comparison.ordinal)
            ) { int ->
                wrongComparisons += OctaveComparison.comparisons[int]
            }

            randomInt(
                count = NO_OF_OPTIONS - 1,
                range = 0 until Combination.combinations.size,
                excluded = intArrayOf(combination.ordinal)
            ) { int ->
                wrongCombinations += Combination.combinations[int]
            }

            val descriptions = ArrayList<Description>(1 + 3 * 2 + NO_OF_OPTIONS * 2)
            descriptions += Description(Description.Type.Text, context.getString("octave_comparison_question_desc", result.getString("bar")))
            repeat(3) { index ->
                descriptions += Description(Description.Type.Text, context.getString("octave_comparison_bar", index.toAlphabetUpper()))
                descriptions += Description(Description.Type.Image, "q_octave_comparison_${context.sectionVariation}_${(index).toAlphabetUpper()}")
            }

            val statements = ArrayList<String>(NO_OF_OPTIONS)
            statements += combination.toString(context.bundle) + " " + comparison.toString(context.bundle)
            repeat(NO_OF_OPTIONS - 1) { index ->
                statements += wrongCombinations[index].toString(context.bundle) + " " + wrongComparisons[index].toString(context.bundle)
            }
            val dispositions = statements.shuffle()
            statements.forEachIndexed { index, statement ->
                descriptions += Description(Description.Type.Text, context.getString("octave_comparison_statement", index + 1))
                descriptions += Description(Description.Type.TextEmphasize, statement)
            }

            // descriptions += Description(Description.Type.Text, dispositions[0].toString())

            val options = List(NO_OF_OPTIONS) { index -> (index + 1).toString() }

            MultipleChoiceQuestion(
                number = questionIndex + 1,
                descriptions = descriptions,
                options = options,
                optionType = MultipleChoiceQuestion.OptionType.Text,
                answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
            )
        }

        return QuestionGroup(
            number = groupNumber,
            name = getGroupName(context.bundle),
            descriptions = emptyList(),
            questions = questions
        )
    }

    private enum class OctaveComparison(private val int: Int, private val stringKey: String) {
        TwoOctavesHigher(2, "octave_comparison_two_octaves_higher"),
        OneOctaveHigher(1, "octave_comparison_one_octave_higher"),
        //Same(0, "octave_comparison_same"),
        OneOctaveLower(-1, "octave_comparison_one_octave_lower"),
        TwoOctavesLower(-2, "octave_comparison_two_octaves_lower");

        fun toString(bundle: ResourceBundle): String {
            return bundle.getString(stringKey)
        }
        companion object {
            val comparisons = values().toList()
            fun fromInt(int: Int): OctaveComparison {
                comparisons.forEach { if (it.int == int) return it }
                throw IllegalArgumentException("Octave comparison with int $int not found")
            }
        }
    }

    private enum class Combination(val string: String, val stringKey: String) {
        A("A", "octave_comparison_a"),
        B("B", "octave_comparison_b"),
        C("C", "octave_comparison_c"),
        AB("AB", "octave_comparison_ab"),
        AC("AC", "octave_comparison_ac"),
        BC("BC", "octave_comparison_bc"),
        ABC("ABC", "octave_comparison_abc");

        fun toString(bundle: ResourceBundle): String {
            return bundle.getString(stringKey)
        }

        companion object {
            val combinations = values().toList()
            fun fromString(string: String): Combination {
                combinations.forEach { if (it.string == string) return it }
                throw IllegalArgumentException("Combination with string $string not found")
            }
        }
    }

}