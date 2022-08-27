package com.donald.abrsmappserver.generator.sectiongenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.Section7GroupGenerator
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.Section
import com.donald.abrsmappserver.question.SectionGroup
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom.Random1
import com.donald.abrsmappserver.utils.option.QuestionGroupOption
import com.donald.abrsmappserver.utils.range.toRangeList
import org.json.JSONArray
import java.sql.Connection
import java.util.*

abstract class Section7Generator(
    identifier: String,
    variationCount: Int,
    private val generators: List<Section7GroupGenerator>,
    database: Connection
) : AbstractSectionGenerator(identifier, database) {

    private val groupMap = HashMap<String, Section7GroupGenerator>(generators.size).apply {
        generators.forEach { generator ->
            put(generator.identifier, generator)
        }
    }

    private val variations = (1 until variationCount).toRangeList()

    abstract fun generateDescriptions(sectionVariation: Int, context: Context): List<Description>

    override fun generateSectionTest(sectionGroupNumber: Int, context: Context): SectionGroup {
        val variation = Random1(variations, autoReset = true).generate()

        val descriptions = generateDescriptions(variation, context)

        // groups
        var currentGroupNumber = 1
        val groups = List(generators.size) { index ->
            generators[index].generateGroup(
                variation,
                currentGroupNumber,
                generators[index].testParentQuestionCount,
                context
            ).also { currentGroupNumber++ }
        }

        return SectionGroup(
            number = sectionGroupNumber,
            name = context.getString("section_$identifier"),
            listOf(
                Section(
                    number = sectionGroupNumber,
                    descriptions = descriptions,
                    questionGroups = groups
                )
            )
        )
    }

    override fun generateSectionPractice(sectionGroupNumber: Int, context: Context, groupOptions: List<QuestionGroupOption>): SectionGroup? {
        val variationRandom = Random1(variations, autoReset = true)

        val options = ArrayList<Pair<Section7GroupGenerator, Int>>(groupOptions.size)

        // 1. calculating the number of sections generated
        var totalGroupCount = 0
        for (option in groupOptions) {
            val count = option.count
            if (count <= 0) break
            val identifier = option.identifier
            val generator = groupMap[identifier] ?: return null

            options += Pair(generator, count)
            totalGroupCount += count
        }
        /*for (i in 0 until groupOptions.length()) {
            groupOptions.getJSONObjectOrNull(i)?.let { option ->
                val count = option.getIntOrNull("count") ?: return null
                val identifier = option.getStringOrNull("identifier") ?: return null
                val generator = groupMap[identifier] ?: return null

                options += Pair(generator, count)
                totalGroupCount += count
            } ?: return null
        }*/

        // 2. generating sections
        val sections = ArrayList<Section>()
        var remainingGroupCount = totalGroupCount
        var currentSectionNumber = 1
        while (remainingGroupCount > 0) {
            // generating a section
            val questionGroups = ArrayList<QuestionGroup>()
            var currentGroupNumber = 1
            val sectionVariation = variationRandom.generateAndExclude()
            options.forEachIndexed { index, option ->
                val (generator, count) = option
                val toGenerateCount = count.coerceAtMost(generator.maxParentQuestionCount)

                if (toGenerateCount > 0) {
                    questionGroups += generator.generateGroup(sectionVariation, currentGroupNumber, toGenerateCount, context)
                    currentGroupNumber++

                    options[index] = Pair(generator, count - toGenerateCount)
                    remainingGroupCount -= toGenerateCount
                }
            }
            sections += Section(
                number = currentSectionNumber,
                descriptions = generateDescriptions(sectionVariation, context),
                questionGroups = questionGroups
            )
            currentSectionNumber++
        }

        return SectionGroup(
            number = sectionGroupNumber,
            name = context.getString("section_$identifier"),
            sections = sections
        )
    }

}