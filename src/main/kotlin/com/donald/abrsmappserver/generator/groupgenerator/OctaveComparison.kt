package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.Section7GroupGenerator
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.randomInt
import com.donald.abrsmappserver.utils.getIntOrNull
import com.donald.abrsmappserver.utils.music.toAlphabetUpperFromZero
import java.sql.Connection
import java.util.*

private const val OPTION_COUNT = 4
private const val PARENT_QUESTION_COUNT = 1
private const val CHILD_QUESTION_COUNT = 1

class OctaveComparison(database: Connection) : Section7GroupGenerator(
    "octave_comparison",
    testParentQuestionCount = PARENT_QUESTION_COUNT,
    maxParentQuestionCount = PARENT_QUESTION_COUNT,
    database
) {

    private val random = Random()

    override fun generateGroup(sectionVariation: Int, sectionGroupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val result = database.prepareStatement("""
            SELECT octave_comparison_1, combination_1, combination_2, octave_comparison_2, bar
            FROM questions_octave_comparison
            WHERE variation = ?
            ORDER BY RANDOM() LIMIT ?;
        """.trimIndent()).apply {
            setInt(1, sectionVariation)
            setInt(2, parentQuestionCount)
        }.executeQuery()

        val questions = List(parentQuestionCount) { parentIndex ->
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

            val wrongComparisons = ArrayList<OctaveComparison>(OPTION_COUNT - 1)
            val wrongCombinations = ArrayList<Combination>(OPTION_COUNT - 1)

            randomInt(
                count = OPTION_COUNT - 1,
                range = 0 until OctaveComparison.comparisons.size,
                excluded = intArrayOf(comparison.ordinal)
            ) { int ->
                wrongComparisons += OctaveComparison.comparisons[int]
            }

            randomInt(
                count = OPTION_COUNT - 1,
                range = 0 until Combination.combinations.size,
                excluded = intArrayOf(combination.ordinal)
            ) { int ->
                wrongCombinations += Combination.combinations[int]
            }

            val descriptions = ArrayList<Description>(1 + 3 * 2 + OPTION_COUNT * 2)
            descriptions += Description(Description.Type.Text, context.getString("octave_comparison_question_desc", result.getString("bar")))
            repeat(3) { index ->
                descriptions += Description(Description.Type.Text, context.getString("octave_comparison_bar", index.toAlphabetUpperFromZero()))
                descriptions += Description(Description.Type.Image, "q_octave_comparison_${sectionVariation}_${(index).toAlphabetUpperFromZero()}")
            }

            val statements = ArrayList<String>(OPTION_COUNT)
            statements += combination.toString(context.bundle) + " " + comparison.toString(context.bundle)
            repeat(OPTION_COUNT - 1) { index ->
                statements += wrongCombinations[index].toString(context.bundle) + " " + wrongComparisons[index].toString(context.bundle)
            }
            val dispositions = statements.shuffle()
            statements.forEachIndexed { index, statement ->
                descriptions += Description(Description.Type.Text, context.getString("octave_comparison_statement", index + 1))
                descriptions += Description(Description.Type.TextEmphasize, statement)
            }

            // descriptions += Description(Description.Type.Text, dispositions[0].toString())

            val options = List(OPTION_COUNT) { index -> (index + 1).toString() }

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = descriptions,
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    MultipleChoiceQuestion(
                        number = childIndex + 1,
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
            descriptions = emptyList(),
            parentQuestions = questions
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