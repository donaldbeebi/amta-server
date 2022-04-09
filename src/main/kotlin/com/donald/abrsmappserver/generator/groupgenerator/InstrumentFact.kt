package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGenerator
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.TruthQuestion
import java.sql.Connection
import java.util.*

class InstrumentFact(database: Connection) : GroupGenerator("instrument_fact", database) {

    private val random = Random()
    private val randomForVariation: RandomIntegerGenerator = RandomIntegerGeneratorBuilder()
        .withLowerBound(0)
        .withUpperBound(STATEMENT_KEY_SUFFIXES.size - 1)
        .build()

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val questions = List(NO_OF_QUESTIONS) { index ->
            val variation = randomForVariation.nextInt()
            val statementStringKey = "instrument_fact_truth_statement_${STATEMENT_KEY_SUFFIXES[variation]}"
            val isTrue = random.nextBoolean()

            val statement = when (variation) {
                0, 1 -> statementOfVar1(statementStringKey, isTrue, context)
                2, 3 -> statementOfVar2(variation, statementStringKey, isTrue, context)
                4 -> statementOfVar3(statementStringKey, isTrue, context)
                5 -> statementOfVar4(statementStringKey, isTrue, context)
                6 -> statementOfVar5(statementStringKey, isTrue, context)
                7 -> statementOfVar6(statementStringKey, isTrue, context)
                8 -> statementOfVar7(statementStringKey, isTrue, context)
                9 -> statementOfVar8(statementStringKey, isTrue, context)
                10 -> statementOfVar9(statementStringKey, isTrue, context)
                else -> throw IllegalStateException()
            }

            TruthQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.TextEmphasize,
                        statement)
                ),
                answer = TruthQuestion.Answer(null, isTrue)
            )
        }

        return QuestionGroup(
            number = groupNumber,
            name = getGroupName(context.bundle),
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("general_truth_group_desc")
                )
            )
        )
    }

    private fun statementOfVar1(statementStringKey: String, isTrue: Boolean, context: Context): String {
        var result = database.prepareStatement(
            "SELECT family_id, string_key FROM data_instruments " +
                    "WHERE family_id IN " +
                    "(SELECT id FROM data_families " +
                    "WHERE name IN ('string', 'woodwind', 'brass', 'percussion')) " +
                    "ORDER BY RANDOM() LIMIT 1;"
        ).executeQuery()
        result.next()
        val instrumentString: String = context.getString(result.getString("string_key"))
        val familyId = result.getInt("family_id")
        val familyString: String
        if (isTrue) {
            result = database.prepareStatement(
                ("SELECT string_key FROM data_families " +
                        "WHERE id = ? ;")
            ).apply {
                setInt(1, familyId)
            }.executeQuery()
            result.next()
            familyString = context.getString(result.getString("string_key"))
        } else {
            result = database.prepareStatement(
                ("SELECT string_key FROM data_families " +
                        "WHERE id != ?;")
            ).apply {
                setInt(1, familyId)
            }.executeQuery()
            result.next()
            familyString = context.getString(result.getString("string_key"))
        }
        return context.getString(statementStringKey, instrumentString, familyString)
    }

    private fun statementOfVar2(variation: Int, statementStringKey: String, isTrue: Boolean, context: Context): String {
        var result = database.prepareStatement(
            "SELECT id, string_key FROM data_families " +
                    "WHERE name IN ('string', 'brass', 'woodwind') " +
                    "ORDER BY RANDOM() LIMIT 1;"
        ).executeQuery()
        val highest = random.nextBoolean()
        val highestString: String = if (highest) {
            context.getString("instrument_fact_highest_string")
        } else {
            context.getString("instrument_fact_lowest_string")
        }
        val familyId = result.getInt("id")
        val familyString: String = context.getString(result.getString("string_key"))
        val chosenInstrumentString: String
        if (isTrue) {
            if (highest) {
                result = database.prepareStatement(
                    "SELECT string_key FROM data_instruments " +
                            "WHERE family_id = ? " +
                            "ORDER BY pitch_ranking ASC LIMIT 1;"
                ).apply {
                    setInt(1, familyId)
                }.executeQuery()
                result.next()
                chosenInstrumentString = context.getString(result.getString("string_key"))
            } else {
                result = database.prepareStatement(
                    ("SELECT string_key FROM data_instruments " +
                            "WHERE family_id = ? " +
                            "ORDER BY pitch_ranking DESC LIMIT 1;")
                ).apply {
                    setInt(1, familyId)
                }.executeQuery()
                result.next()
                chosenInstrumentString = context.getString(result.getString("string_key"))
            }
        } else {
            result = database.prepareStatement(
                ("SELECT COUNT(*) FROM data_instruments " +
                        "WHERE family_id = ?;")
            ).apply {
                setInt(1, familyId)
            }.executeQuery()
            val numberOfInstruments = result.getLong("COUNT(*)")
            var index = random.nextInt((numberOfInstruments.toInt()) - 1)
            if (highest) index += 1
            result = database.prepareStatement(
                ("SELECT string_key FROM data_instruments " +
                        "WHERE family_id = ? " +
                        "LIMIT ?, 1;")
            ).apply {
                setInt(1, familyId)
                setInt(2, index)
            }.executeQuery()
            chosenInstrumentString = context.getString(result.getString("string_key"))
        }
        return if (variation == 2) {
            context.getString(
                statementStringKey,
                chosenInstrumentString,
                highestString,
                familyString
            )
        } else {
            context.getString(
                statementStringKey,
                highestString,
                familyString,
                chosenInstrumentString
            )
        }
    }

     private fun statementOfVar3(statementStringKey: String, isTrue: Boolean, context: Context): String {
        val firstInstHigher = random.nextBoolean()
        var result = database.prepareStatement(
            "SELECT string_key, pitch_class FROM data_instruments " +
                    "WHERE pitch_class IS NOT NULL AND pitch_class > 1 " +
                    "ORDER BY RANDOM() LIMIT 1;"
        ).executeQuery()
        result.next()
        val lowerInstStringKey = result.getString("string_key")
        val lowerInstPitchClass = result.getInt("pitch_class")
        result = if (isTrue) {
            database.prepareStatement(
                "SELECT string_key FROM data_instruments " +
                        "WHERE pitch_class IS NOT NULL AND " +
                        "pitch_class < ? " +
                        "ORDER BY RANDOM() LIMIT 1;"
            ).apply {
                setInt(1, lowerInstPitchClass)
            }.executeQuery()
        } else {
            database.prepareStatement(
                "SELECT string_key FROM data_instruments " +
                        "WHERE pitch_class IS NOT NULL AND " +
                        "pitch_class <= ? " +
                        "ORDER BY RANDOM() LIMIT 1;"
            ).apply {
                setInt(1, lowerInstPitchClass)
            }.executeQuery()
        }
        val higherInstStringKey = result.getString("string_key")
        result.next()
        val firstInstString: String
        val secondInstString: String
        val higherString: String = if (firstInstHigher) {
            context.getString("instrument_fact_higher_string")
        } else {
            context.getString("instrument_fact_lower_string")
        }
        if (firstInstHigher == isTrue) {
            // if the first instrument is higher and is true OR
            // if the first instrument is lower and is not true
            firstInstString = context.getString(higherInstStringKey)
            secondInstString = context.getString(lowerInstStringKey)
        } else {
            // if the first instrument is lower and is true OR
            // if the first instrument is higher and is not true
            firstInstString = context.getString(lowerInstStringKey)
            secondInstString = context.getString(higherInstStringKey)
        }
        return context.getString(statementStringKey,
            firstInstString,
            higherString,
            secondInstString
        )
    }

     private fun statementOfVar4(statementStringKey: String, isTrue: Boolean, context: Context): String {
        val result = database.prepareStatement(
            "SELECT string_key, transposing FROM data_instruments " +
                    "WHERE transposing IS NOT NULL " +
                    "ORDER BY RANDOM() LIMIT 1;"
        ).executeQuery()
        result.next()
        val instrumentString: String = context.getString(result.getString("string_key"))
        val transposingString: String = if ((result.getInt("transposing") == 0) == isTrue) {
            context.getString("instrument_fact_non_transposing_string")
        } else {
            context.getString("instrument_fact_transposing_string")
        }
        return context.getString(statementStringKey,
            instrumentString,
            transposingString
        )
    }

     private fun statementOfVar5(statementStringKey: String, isTrue: Boolean, context: Context): String {
        val instrumentString: String
        val techniqueString: String

        // randomly choose a technique
        var result = database.prepareStatement(
            "SELECT id, string_key FROM data_techniques " +
                    "ORDER BY RANDOM() LIMIT 1;"
        ).executeQuery()
        result.next()
        val techniqueId = result.getInt("id")
        techniqueString = context.getString(result.getString("string_key"))
        if (isTrue) {
            // randomly choose a family with this technique
            result = database.prepareStatement(
                "SELECT family_id FROM lookup_families_techniques " +
                        "WHERE technique_id = ? " +
                        "ORDER BY RANDOM() LIMIT 1;"
            ).apply {
                setInt(1, techniqueId)
            }.executeQuery()
            result.next()
            val familyId = result.getInt("family_id")

            // randomly choose an instrument from this family
            result = database.prepareStatement(
                "SELECT string_key FROM data_instruments " +
                        "WHERE family_id = ? " +
                        "ORDER BY RANDOM() LIMIT 1;"
            ).apply {
                setInt(1, familyId)
            }.executeQuery()
        } else {
            // randomly select a family without that technique
            result = database.prepareStatement(
                "SELECT id FROM data_families " +
                        "WHERE id NOT IN (SELECT family_id FROM lookup_families_techniques " +
                        "WHERE technique_id = ?) " +
                        "ORDER BY RANDOM() LIMIT 1;"
            ).apply {
                setInt(1, techniqueId)
            }.executeQuery()
            result.next()
            val familyId = result.getInt("id")

            // randomly choose an instrument from that family
            result = database.prepareStatement(
                ("SELECT string_key FROM data_instruments " +
                        "WHERE family_id = ? " +
                        "ORDER BY RANDOM() LIMIT 1;")
            ).apply {
                setInt(1, familyId)
            }.executeQuery()
        }
        result.next()
        instrumentString = context.getString(result.getString("string_key"))
        return context.getString(statementStringKey,
            instrumentString,
            techniqueString
        )
    }

     private fun statementOfVar6(statementStringKey: String, isTrue: Boolean, context: Context): String {
        var result = database.prepareStatement(
            "SELECT string_key, primary_clef_id, secondary_clef_id FROM data_instruments " +
                    "WHERE primary_clef_id IS NOT NULL " +
                    "ORDER BY RANDOM() LIMIT 1;"
        ).executeQuery()
        result.next()
        val instrumentString: String = context.getString(result.getString("string_key"))
        val primaryClefId = result.getInt("primary_clef_id")
        val secondaryClefId = result.getInt("secondary_clef_id")
        val secondaryNull = result.wasNull()
        if (isTrue) {
            val chosenClefId = if (!secondaryNull && random.nextBoolean()) secondaryClefId else primaryClefId
            result = database.prepareStatement(
                "SELECT string_key FROM data_clefs " +
                        "WHERE id = ?;"
            ).apply {
                setInt(1, chosenClefId)
            }.executeQuery()
        } else {
            if (secondaryNull) {
                result = database.prepareStatement(
                    "SELECT string_key FROM data_clefs " +
                            "WHERE id != ? " +
                            "ORDER BY RANDOM() LIMIT 1;"
                ).apply {
                    setInt(1, primaryClefId)
                }.executeQuery()
            } else {
                result = database.prepareStatement(
                    ("SELECT string_key FROM data_clefs " +
                            "WHERE id NOT IN (?, ?) " +
                            "ORDER BY RANDOM() LIMIT 1;")
                ).apply {
                    setInt(1, primaryClefId)
                    setInt(2, secondaryClefId)
                }.executeQuery()
            }
        }
        result.next()
        val clefString: String = context.getString(result.getString("string_key"))
        return context.getString(statementStringKey,
            instrumentString,
            clefString
        )
    }

     private fun statementOfVar7(statementStringKey: String, isTrue: Boolean, context: Context): String {
        val result = database.prepareStatement(
            "SELECT string_key, definite_pitch FROM data_instruments " +
                    "WHERE definite_pitch IS NOT NULL " +
                    "ORDER BY RANDOM() LIMIT 1;"
        ).executeQuery()
        result.next()
        val instrumentString: String = context.getString(result.getString("string_key"))
        val definiteString: String = if ((result.getInt("definite_pitch") == 0) == isTrue) {
            context.getString("instrument_fact_definite_string")
        } else {
            context.getString("instrument_fact_indefinite_string")
        }
        return context.getString(statementStringKey,
            instrumentString,
            definiteString
        )
    }

     private fun statementOfVar8(statementStringKey: String, isTrue: Boolean, context: Context): String {
        val result = database.prepareStatement(
            "SELECT string_key, reed_count FROM data_instruments " +
                    "WHERE reed_count IS NOT NULL " +
                    "ORDER BY RANDOM() LIMIT 1;"
        ).executeQuery()
        result.next()
        val instrumentString: String = context.getString(result.getString("string_key"))
        val reedString: String = if ((result.getInt("reed_count") == 1) == isTrue) {
            context.getString("instrument_fact_single_reed_string")
        } else {
            context.getString("instrument_fact_double_reed_string")
        }
        return context.getString(statementStringKey,
            instrumentString,
            reedString
        )
    }

     private fun statementOfVar9(statementStringKey: String, isTrue: Boolean, context: Context): String {
        val result = database.prepareStatement(
            "SELECT string_key, pitch_ranking FROM data_instruments " +
                    "WHERE family_id IN (SELECT id FROM data_families WHERE name = 'voice') " +
                    "ORDER BY RANDOM() LIMIT 2;"
        ).executeQuery()
        result.next()
        val firstInstString: String = context.getString(result.getString("string_key"))
        val firstInstPitchRanking = result.getInt("pitch_ranking")
        result.next()
        val secondInstString: String = context.getString(result.getString("string_key"))
        val secondInstPitchRanking = result.getInt("pitch_ranking")
        val higherString: String = if (firstInstPitchRanking < secondInstPitchRanking == isTrue) {
            context.getString("instrument_fact_higher_string")
        } else {
            context.getString("instrument_fact_lower_string")
        }
        return context.getString(statementStringKey,
            firstInstString,
            higherString,
            secondInstString
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 5
        private val STATEMENT_KEY_SUFFIXES = arrayOf(
            "1a", "1b", "2a", "2b", "3", "4", "5", "6", "7", "8", "9"
        )

    }

}