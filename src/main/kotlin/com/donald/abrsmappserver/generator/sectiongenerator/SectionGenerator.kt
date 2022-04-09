package com.donald.abrsmappserver.generator.sectiongenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.getIntOrNull
import com.donald.abrsmappserver.utils.getJSONObjectOrNull
import com.donald.abrsmappserver.utils.getStringOrNull
import com.donald.abrsmappserver.generator.groupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.QuestionSection
import com.donald.abrsmappserver.question.QuestionGroup
import org.json.JSONArray
import java.sql.Connection
import java.util.*

@Deprecated("No longer used")
fun interface DescriptionBuilder {
    fun build(variation: Int, bundle: ResourceBundle): Description
}

open class SectionGenerator(
    val identifier: String,
    private val descriptionBuilders: List<DescriptionBuilder> = emptyList(),
    private val generators: List<GroupGenerator>,
    private val variationCount: Int = 1,
    protected val database: Connection
) {

    private val random = Random()

    private val groupMap = HashMap<String, GroupGenerator>(generators.size).apply {
        generators.forEach { generator ->
            put(generator.identifier, generator)
        }
    }

    val groupCount: Int
        get() = generators.size

    init { require(variationCount > 0) }

    protected open fun generateDescriptions(context: Context): List<Description> = emptyList()

    fun generateSectionTest(sectionNumber: Int, startGroupNumber: Int, bundle: ResourceBundle): QuestionSection {
        // context
        val sectionVariation = random.nextInt(variationCount) + 1
        val context = Context(bundle, sectionVariation)

        // descriptions
        val descriptions = generateDescriptions(context)

        // groups
        var currentGroupNumber = startGroupNumber
        val groups = List(generators.size) { index ->
            generators[index].generateGroup(currentGroupNumber, context).also { currentGroupNumber++ }
        }

        return QuestionSection(
            number = sectionNumber,
            name = context.getString("section_$identifier"),
            descriptions = descriptions,
            groups = groups
        )
    }

    /**
     * @return a question section upon successful generation; null if any one of the specified section identifiers is not valid
     */
    fun generateSectionPractice(sectionNumber: Int, startGroupNumber: Int, bundle: ResourceBundle, groupOption: JSONArray): QuestionSection? {
        // context
        val sectionVariation = random.nextInt(variationCount) + 1
        val context = Context(bundle, sectionVariation)

        // descriptions
        val descriptions = generateDescriptions(context)

        // groups
        var currentGroupNumber = startGroupNumber
        val groups = ArrayList<QuestionGroup>()
        for (i in 0 until groupOption.length()) {
            val option = groupOption.getJSONObjectOrNull(i) ?: return null
            val count = option.getIntOrNull("count") ?: return null
            val identifier = option.getStringOrNull("identifier") ?: return null
            repeat(count) {
                val generator = groupMap[identifier] ?: return null
                groups += generator.generateGroup(currentGroupNumber, context).also { currentGroupNumber++ }
            }
        }

        return QuestionSection(
            number = sectionNumber,
            name = context.getString("section_$identifier"),
            descriptions = descriptions,
            groups = groups
        )
    }

}