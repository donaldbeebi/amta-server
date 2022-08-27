package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.QuestionGroup
import java.sql.Connection

class SectionFact(database: Connection) : GroupGenerator("section_fact", database) {

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        TODO("Not yet implemented")
    }
    
}