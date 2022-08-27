package com.donald.abrsmappserver.generator.sectiongenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.SectionGroup
import com.donald.abrsmappserver.utils.option.QuestionGroupOption
import org.json.JSONArray
import java.sql.Connection

abstract class AbstractSectionGenerator(
    val identifier: String,
    protected val database: Connection
) {
    abstract fun generateSectionTest(sectionGroupNumber: Int, context: Context): SectionGroup
    abstract fun generateSectionPractice(sectionGroupNumber: Int, context: Context, groupOptions: List<QuestionGroupOption>): SectionGroup?
}