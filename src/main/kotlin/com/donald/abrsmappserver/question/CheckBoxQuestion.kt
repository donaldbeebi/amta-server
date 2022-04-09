package com.donald.abrsmappserver.question
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CheckBoxQuestion(
    number: Int,
    descriptions: List<Description>,
    inputHint: String? = null,
    val answers: List<Answer>
) : Question(number, descriptions, inputHint) {

    override val points: Int
        get() = answers.count { it.correct }
    override val maxPoints = answers.size

    override fun acceptVisitor(visitor: QuestionVisitor) {
        visitor.visit(this)
    }

    override fun toPartialJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put(QUESTION_TYPE, Type.CHECK_BOX.ordinal)
        val array = JSONArray()
        for (answer in answers) {
            array.put(answer.toJson())
        }
        jsonObject.put("answers", array)
        return jsonObject
    }

    class Answer(var userAnswer: Boolean?, val correctAnswer: Boolean) : Question.Answer {

        override val correct: Boolean
            get() = userAnswer == correctAnswer

        fun toJson(): JSONObject {
            val jsonObject = JSONObject()
            jsonObject.put("user_answer", if (userAnswer == null) JSONObject.NULL else userAnswer)
            jsonObject.put("correct_answer", correctAnswer)
            return jsonObject
        }

    }
}