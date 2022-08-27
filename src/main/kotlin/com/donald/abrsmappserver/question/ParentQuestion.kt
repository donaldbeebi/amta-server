package com.donald.abrsmappserver.question

import com.donald.abrsmappserver.utils.forEachArg
import org.json.JSONArray
import org.json.JSONObject

class ParentQuestion(
    val number: Int,
    val descriptions: List<Description> = emptyList(),
    val childQuestions: List<ChildQuestion>
) : Iterable<ChildQuestion> {

    val points: Int
        get() = childQuestions.sumOf { it.points }
    val maxPoints: Int
        get() = childQuestions.sumOf { it.maxPoints }

    fun registerImages(imageArray: JSONArray) {
        descriptions.forEach { description ->
            when (description.type) {
                Description.Type.Image -> imageArray.put(description.content)
                Description.Type.TextSpannable -> description.content.forEachArg { imageArray.put(it) }
                else -> {}
            }
        }
        childQuestions.forEach { it.registerImages(imageArray) }
    }

    fun registerStrings(stringArray: JSONArray) {
        childQuestions.forEach { it.registerStrings(stringArray) }
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("number", number)

            put("descriptions",
                JSONArray().apply { descriptions.forEach { put(it.toJson()) } }
            )

            put("child_questions",
                JSONArray().apply { childQuestions.forEach { put(it.toJson()) } }
            )
        }
    }

    override fun iterator(): Iterator<ChildQuestion> = childQuestions.iterator()

}