package com.donald.abrsmappserver.question

import org.json.JSONArray
import org.json.JSONObject

class SectionGroup(
    val number: Int,
    val name: String,
    val sections: List<Section>
) : Iterable<Section> {

    fun registerImages(imageArray: JSONArray) {
        sections.forEach { section ->
            section.registerImages(imageArray)
        }
    }

    fun registerStrings(stringArray: JSONArray) {
        sections.forEach { section ->
            section.registerStrings(stringArray)
        }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("number", number)
            put("name", name)
            put(
                "sections",
                JSONArray().apply { sections.forEach { put(it.toJson()) } }
            )
        }
    }

    override fun iterator(): Iterator<Section> = sections.iterator()

}