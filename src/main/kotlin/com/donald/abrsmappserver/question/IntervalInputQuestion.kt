package com.donald.abrsmappserver.question

import com.donald.abrsmappserver.utils.music.Note
import com.donald.abrsmappserver.utils.music.Score
import org.json.JSONException
import org.json.JSONObject

class IntervalInputQuestion(
    number: Int,
    descriptions: List<Description>,
    inputHint: String? = null,
    val score: Score,
    val requiredInterval: String,
    val answer: Answer
) : Question(number, descriptions, inputHint) {

    override val points: Int
        get() = if (answer.correct) 1 else 0
    override val maxPoints = 1

    override fun acceptVisitor(visitor: QuestionVisitor) {
        visitor.visit(this)
    }

    @Throws(JSONException::class)
    override fun toPartialJson(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put(QUESTION_TYPE, Type.INTERVAL_INPUT.ordinal)
        jsonObject.put("score", score.toDocument().asXML())
        jsonObject.put("required_interval", requiredInterval)
        jsonObject.put("answer", answer.toJson())
        return jsonObject
    }

    class Answer(var userAnswer: Note?, val correctAnswer: Note) : Question.Answer {

        override val correct: Boolean
            get() = userAnswer?.pitch()?.equals(correctAnswer.pitch()) ?: false

        @Throws(JSONException::class)
        fun toJson(): JSONObject {
            return JSONObject().apply {
                put("user_answer", userAnswer?.toJson() ?: JSONObject.NULL)
                put("correct_answer", correctAnswer.toJson())
            }
        }

    }

}