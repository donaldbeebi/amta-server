package com.donald.abrsmappserver.exercise

import com.donald.abrsmappserver.utils.music.Interval
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGenerator
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import java.sql.Connection
import java.util.*

class Context(
    val bundle: ResourceBundle,
    val sectionVariation: Int
) {

    fun getString(key: String, vararg args: Any?): String {
        return String.format(bundle.getString(key), *args)
    }

}