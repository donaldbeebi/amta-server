package com.donald.abrsmappserver.question

import org.json.JSONArray
import org.json.JSONObject

class Section(
    val number: Int,
    //val name: String,
    val descriptions: List<Description> = emptyList(),
    val questionGroups: List<QuestionGroup>
) : Iterable<QuestionGroup> {

    val points: Int
        get() = questionGroups.sumOf { it.points }
    val maxPoints: Int
        get() = questionGroups.sumOf { it.maxPoints }

    operator fun contains(questionGroup: QuestionGroup): Boolean {
        return questionGroups.any { it === questionGroup }
    }

    operator fun contains(question: ParentQuestion): Boolean {
        return questionGroups.any { question in it }
    }

    operator fun contains(question: ChildQuestion): Boolean {
        return questionGroups.any { group ->
            group.parentQuestions.any { parentQuestion ->
                question in parentQuestion.childQuestions
            }
        }
    }

    fun registerImages(imageArray: JSONArray) {
        descriptions.forEach { description ->
            if (description.type == Description.Type.Image) imageArray.put(description.content)
        }
        questionGroups.forEach { group ->
            group.registerImages(imageArray)
        }
    }

    fun registerStrings(stringArray: JSONArray) {
        descriptions.forEach { description ->
            if (description.type.isText) stringArray.put(description.content)
        }
        questionGroups.forEach { group ->
            group.registerStrings(stringArray)
        }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("number", number)
            //put("name", name)
            put(
                "descriptions",
                JSONArray().apply {
                    descriptions.forEach { put(it.toJson()) }
                }
            )
            put(
                "groups",
                JSONArray().apply {
                    questionGroups.forEach { put(it.toJson()) }
                }
            )
        }
    }

    override fun iterator(): Iterator<QuestionGroup> {
        return questionGroups.iterator()
    }

}