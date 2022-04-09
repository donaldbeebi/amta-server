package com.donald.abrsmappserver.question

import org.json.JSONArray
import org.json.JSONObject

class QuestionSection(
    val number: Int,
    val name: String,
    val descriptions: List<Description> = emptyList(),
    val groups: List<QuestionGroup>
) : Iterable<QuestionGroup> {

    val points: Int
        get() = groups.sumOf { it.points }
    val maxPoints: Int
        get() = groups.sumOf { it.maxPoints }

    operator fun contains(group: QuestionGroup): Boolean {
        return groups.any { it === group }
    }

    operator fun contains(question: Question): Boolean {
        groups.forEach { group ->
            if (question in group) return true
        }
        return false
    }

    fun registerImages(imageArray: JSONArray) {
        descriptions.forEach { description ->
            if (description.type == Description.Type.Image) imageArray.put(description.content)
        }
        groups.forEach { group ->
            group.registerImages(imageArray)
        }
    }

    fun registerStrings(stringArray: JSONArray) {
        descriptions.forEach { description ->
            if (description.type.isText) stringArray.put(description.content)
        }
        groups.forEach { group ->
            group.registerStrings(stringArray)
        }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("number", number)
            put("name", name)
            put(
                "descriptions",
                JSONArray().apply {
                    descriptions.forEach { put(it.toJson()) }
                }
            )
            put(
                "groups",
                JSONArray().apply {
                    groups.forEach { put(it.toJson()) }
                }
            )
        }
    }

    override fun iterator(): Iterator<QuestionGroup> {
        return groups.iterator()
    }

}