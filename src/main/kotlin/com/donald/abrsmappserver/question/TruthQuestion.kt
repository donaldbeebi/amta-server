package com.donald.abrsmappserver.question

import org.json.JSONObject

class TruthQuestion(
    number: Int,
    descriptions: List<Description>,
    inputHint: String? = null,
    val answer: Answer
) : Question(number, descriptions, inputHint) {

    override val points: Int
        get() = if (answer.correct) 1 else 0
    override val maxPoints = 1

    override fun acceptVisitor(visitor: QuestionVisitor) {
        visitor.visit(this)
    }

    override fun toPartialJson(): JSONObject {
        return JSONObject().apply {
            put(QUESTION_TYPE, Type.TRUTH.ordinal)
            put("answer", answer.toJson())
        }
    }

    class Answer(var userAnswer: Boolean?, val correctAnswer: Boolean) : Question.Answer {

        override val correct: Boolean
            get() {
                return userAnswer == correctAnswer
            }

        fun toJson(): JSONObject {
            return JSONObject().apply {
                put("user_answer", if (userAnswer == null) JSONObject.NULL else userAnswer)
                put("correct_answer", correctAnswer)
            }
        }

    }

}