package com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.QuestionGroup
import java.sql.Connection

abstract class GroupGenerator protected constructor(
    identifier: String,
    testParentQuestionCount: Int,
    database: Connection
) : AbstractGroupGenerator(identifier, testParentQuestionCount, database) {

    abstract fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup

}

data class ShuffleResult<T>(val shuffled: List<T>, val dispositions: List<Int>)

fun ShuffleResult<*>.newIndex(oldIndex: Int): Int = oldIndex + dispositions[oldIndex]