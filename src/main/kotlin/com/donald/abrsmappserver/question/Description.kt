package com.donald.abrsmappserver.question

import org.json.JSONException
import org.json.JSONObject

class Description(val type: Type, val content: String) {
    override fun toString(): String {
        return "Type: " + type + " " +
                "Content: " + content
    }

    constructor(type: Int, content: String): this(Type.values()[type], content)

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put(DESCRIPTION_TYPE, type.ordinal)
            put("content", content)
        }
    }

    enum class Type(private val string: String) {

        Text("text"), TextEmphasize("text_emphasize"), TextSpannable("text_spannable"), Image("image"), Score("score");

        override fun toString() = string

        companion object {
            val types = values().toList()
            fun fromString(string: String): Description.Type {
                types.forEach { if (it.string == string) return it }
                throw IllegalArgumentException("No type found for $string")
            }
        }

    }

}

val Description.Type.isText: Boolean
    get() = this == Description.Type.Text || this == Description.Type.TextEmphasize || this == Description.Type.TextSpannable