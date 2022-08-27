package com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator

import com.donald.abrsmappserver.utils.ArrayShuffler
import java.sql.Connection
import java.util.*

abstract class AbstractGroupGenerator(
    val identifier: String,
    val testParentQuestionCount: Int,
    protected val database: Connection
) {

    protected fun getGroupName(bundle: ResourceBundle): String {
        return bundle.getString("group_$identifier")
    }

    //protected fun ResourceBundle.getString(key: String, vararg args: Any?): String {
    //    return String.format(this.getString(key), *args)
    //}

    @Deprecated("Use shuffled() instead", ReplaceWith("ArrayShuffler.shuffle(this)", "com.donald.abrsmappserver.utils.ArrayShuffler"))
    protected fun <T> Array<T>.shuffle(): IntArray {
        return ArrayShuffler.shuffle(this)
    }

    @Deprecated("Use shuffled() instead", ReplaceWith("ArrayShuffler.shuffle(this)", "com.donald.abrsmappserver.utils.ArrayShuffler"))
    protected fun <T> List<T>.shuffle(): IntArray {
        return ArrayShuffler.shuffle(this)
    }

    protected fun <T> List<T>.shuffled(): ShuffleResult<T> {
        val shuffled = this.toList()
        val dispositions = shuffled.shuffle().toList()
        return ShuffleResult(shuffled, dispositions)
    }

}