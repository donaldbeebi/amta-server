package com.donald.abrsmappserver.question

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class TextInputQuestion(
    number: Int,
    descriptions: List<Description>,
    inputHint: String? = null,
    val inputType: InputType,
    val answers: List<Answer>
) : Question(number, descriptions, inputHint) {

    override val points: Int
        get() = answers.count { it.correct }
    override val maxPoints = answers.size

    override fun acceptVisitor(visitor: QuestionVisitor) {
        visitor.visit(this)
    }

    @Throws(JSONException::class)
    override fun toPartialJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put(QUESTION_TYPE, Type.TEXT_INPUT.ordinal)
        jsonObject.put(INPUT_TYPE, inputType.ordinal)
        val array = JSONArray()
        for (answer in answers) {
            array.put(answer.toJson())
        }
        jsonObject.put("answers", array)
        return jsonObject
    }

    enum class InputType {
        Text, Number
    }

    class Answer(var userAnswer: String?, val correctAnswer: String) : Question.Answer {

        override val correct: Boolean
            get() = userAnswer == correctAnswer

        @Throws(JSONException::class)
        fun toJson(): JSONObject {
            return JSONObject().apply {
                put("user_answer", userAnswer ?: JSONObject.NULL)
                put("correct_answer", correctAnswer)
            }
        }

    }

}