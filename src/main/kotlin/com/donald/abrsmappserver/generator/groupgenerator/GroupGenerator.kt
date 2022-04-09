package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.ArrayShuffler
import com.donald.abrsmappserver.question.QuestionGroup
import java.sql.Connection
import java.util.*

abstract class GroupGenerator protected constructor(
    val identifier: String,
    protected val database: Connection
) {

    abstract fun generateGroup(groupNumber: Int, context: Context): QuestionGroup

    protected fun getGroupName(bundle: ResourceBundle): String {
        return bundle.getString("group_$identifier")
    }

    //protected fun ResourceBundle.getString(key: String, vararg args: Any?): String {
    //    return String.format(this.getString(key), *args)
    //}

    protected fun <T> Array<T>.shuffle(): IntArray {
        return ArrayShuffler.shuffle(this)
    }

    protected fun <T> List<T>.shuffle(): IntArray {
        return ArrayShuffler.shuffle(this)
    }

}