package com.donald.abrsmappserver.generator.sectiongenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.Section
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.SectionGroup
import com.donald.abrsmappserver.utils.option.QuestionGroupOption
import org.json.JSONArray
import java.sql.Connection
import java.util.*

abstract class SectionGenerator(
    identifier: String,
    private val generators: List<GroupGenerator>,
    database: Connection
) : AbstractSectionGenerator(identifier, database) {

    private val groupMap = HashMap<String, GroupGenerator>(generators.size).apply {
        generators.forEach { generator ->
            put(generator.identifier, generator)
        }
    }
    val groupCount: Int
        get() = generators.size

    override fun generateSectionTest(sectionGroupNumber: Int, context: Context): SectionGroup {
        // groups
        var currentGroupNumber = 1
        val groups = List(generators.size) { index ->
            generators[index].generateGroup(
                currentGroupNumber,
                generators[index].testParentQuestionCount,
                context
            ).also { currentGroupNumber++ }
        }

        return SectionGroup(
            number = sectionGroupNumber,
            name = context.getString("section_$identifier"),
            sections = listOf(
                Section(
                    number = sectionGroupNumber,
                    questionGroups = groups
                )
            )
        )
    }

    /**
     * @return a question section upon successful generation; null if any one of the specified section identifiers is not valid
     */
    override fun generateSectionPractice(sectionGroupNumber: Int, context: Context, groupOptions: List<QuestionGroupOption>): SectionGroup? {
        // groups
        //var currentGroupNumber = startGroupNumber
        var currentGroupNumber = 1
        val questionGroups = ArrayList<QuestionGroup>()
        //for (i in 0 until groupOptions.length()) {
        for (option in groupOptions) {
            //val option = groupOptions.getJSONObjectOrNull(i) ?: return null
            val count = option.count
            val identifier = option.identifier
            val generator = groupMap[identifier] ?: return null
            questionGroups += generator.generateGroup(currentGroupNumber, count, context).also { currentGroupNumber++ }

        }

        return SectionGroup(
            number = sectionGroupNumber,
            name = context.getString("section_$identifier"),
            listOf(
                Section(
                    number = sectionGroupNumber,
                    questionGroups = questionGroups
                )
            )
        )
    }
}