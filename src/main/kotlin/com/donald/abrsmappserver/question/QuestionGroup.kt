package com.donald.abrsmappserver.question

import org.json.JSONArray
import org.json.JSONObject

class Group(
    val number: Int,
    val name: String,
    val descriptions: List<Description> = emptyList(),
    val parentQuestions: List<ParentQuestion>
) : Iterable<ParentQuestion> {

    val points: Int
        get() = parentQuestions.sumOf { it.points }
    val maxPoints: Int
        get() = parentQuestions.sumOf { it.maxPoints }

    operator fun contains(question: ParentQuestion): Boolean {
        return parentQuestions.any { it === question }
    }

    fun registerImages(imageArray: JSONArray) {
        descriptions.forEach { description ->
            if (description.type == Description.Type.Image) {
                imageArray.put(description.content)
            }
        }
        parentQuestions.forEach { question ->
            question.registerImages(imageArray)
        }
    }

    fun registerStrings(stringArray: JSONArray) {
        descriptions.forEach { description ->
            if (description.type.isText) {
                stringArray.put(description.content)
            }
        }
        parentQuestions.forEach { question ->
            question.registerStrings(stringArray)
        }
    }

    fun toJson(): JSONObject {
        val jsonObject = JSONObject()

        // 1. number
        jsonObject.put("number", number)

        // 2. topic
        jsonObject.put("name", name)

        // 3. descriptions
        var currentArray = JSONArray()
        for (description in descriptions) currentArray.put(description.toJson())
        jsonObject.put("descriptions", currentArray)

        // 4. questions
        currentArray = JSONArray()
        for (question in parentQuestions) currentArray.put(question.toJson())
        jsonObject.put("parent_questions", currentArray)
        return jsonObject
    }

    override fun iterator(): Iterator<ParentQuestion> {
        return parentQuestions.iterator()
    }

}