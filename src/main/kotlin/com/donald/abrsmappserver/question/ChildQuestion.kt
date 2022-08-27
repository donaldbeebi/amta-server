package com.donald.abrsmappserver.question

import com.donald.abrsmappserver.utils.forEachArg
import org.json.JSONArray
import org.json.JSONObject

abstract class ChildQuestion(
    val number: Int,
    val descriptions: List<Description> = emptyList(),
    val inputHint: String?
) {

    abstract val points: Int
    abstract val maxPoints: Int

    abstract fun acceptVisitor(visitor: QuestionVisitor)

    open fun registerImages(imageArray: JSONArray) {
        for (description in descriptions) {
            when (description.type) {
                Description.Type.Image -> {
                    imageArray.put(description.content)
                }
                Description.Type.TextSpannable -> {
                    description.content.forEachArg { content ->
                        imageArray.put(content)
                    }
                }
                else -> { /* do nothing */ }
            }
        }
    }

    open fun registerStrings(stringArray: JSONArray) {
        descriptions.forEach { description ->
            if (description.type.isText) stringArray.put(description.content)
            if (inputHint != null) stringArray.put(inputHint)
        }
    }

    protected abstract fun toPartialJson(): JSONObject

    fun toJson(): JSONObject {
        val jsonObject = toPartialJson()
        // 1. number
        jsonObject.put("number", number)

        // 2. descriptions
        val currentArray = JSONArray()
        for (description in descriptions) currentArray.put(description.toJson())
        jsonObject.put("descriptions", currentArray)

        // 3. panel hint
        val inputHint = inputHint
        if (inputHint == null) jsonObject.put("input_hint", JSONObject.NULL) else jsonObject.put("input_hint", inputHint)
        return jsonObject
    }

    enum class Type { MULTIPLE_CHOICE, TEXT_INPUT, CHECK_BOX, TRUTH, INTERVAL_INPUT }

    interface QuestionVisitor {

        fun visit(question: MultipleChoiceQuestion)
        fun visit(question: TextInputQuestion)
        fun visit(question: TruthQuestion)
        fun visit(question: CheckBoxQuestion)
        fun visit(question: IntervalInputQuestion)

    }

    interface Answer {

        val correct: Boolean

    }

}