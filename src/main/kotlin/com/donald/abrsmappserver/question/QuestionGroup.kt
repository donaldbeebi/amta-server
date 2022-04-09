package com.donald.abrsmappserver.question

import org.json.JSONArray
import org.json.JSONObject

class QuestionGroup(
    val number: Int,
    val name: String,
    val descriptions: List<Description>,
    val questions: List<Question>
) : Iterable<Question> {

    val points: Int
        get() = questions.sumOf { it.points }
    val maxPoints: Int
        get() = questions.sumOf { it.maxPoints }

    operator fun contains(question: Question): Boolean {
        return questions.any { it === question }
    }

    fun registerImages(imageArray: JSONArray) {
        descriptions.forEach { description ->
            if (description.type == Description.Type.Image) {
                imageArray.put(description.content)
            }
        }
        questions.forEach { question ->
            question.registerImages(imageArray)
        }
    }

    fun registerStrings(stringArray: JSONArray) {
        descriptions.forEach { description ->
            if (description.type.isText) {
                stringArray.put(description.content)
            }
        }
        questions.forEach { question ->
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
        for (question in questions) currentArray.put(question.toJson())
        jsonObject.put("questions", currentArray)
        return jsonObject
    }

    override fun iterator(): Iterator<Question> {
        return questions.iterator()
    }

}