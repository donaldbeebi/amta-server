package com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.QuestionGroup
import java.sql.Connection

abstract class Section7GroupGenerator(
    identifier: String,
    testParentQuestionCount: Int,
    val maxParentQuestionCount: Int,
    database: Connection
) : AbstractGroupGenerator(
    identifier,
    testParentQuestionCount,
    database
) {

    abstract fun generateGroup(sectionVariation: Int, sectionGroupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup

}