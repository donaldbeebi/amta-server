package com.donald.abrsmappserver.question

import org.json.JSONArray
import org.json.JSONObject

class MultipleChoiceQuestion(
    number: Int,
    descriptions: List<Description> = emptyList(),
    inputHint: String? = null,
    val options: List<String>,
    val optionType: OptionType,
    val answer: Answer
) : ChildQuestion(number, descriptions, inputHint) {

    override val points: Int
        get() = if (answer.correct) 1 else 0
    override val maxPoints = 1

    override fun acceptVisitor(visitor: QuestionVisitor) {
        visitor.visit(this)
    }

    override fun registerImages(imageArray: JSONArray) {
        super.registerImages(imageArray)
        if (optionType == OptionType.Image) {
            for (option in options) {
                imageArray.put(option)
            }
        }
    }

    override fun toPartialJson(): JSONObject {
        val jsonObject = JSONObject()
        val array = JSONArray()
        jsonObject.put(QUESTION_TYPE, Type.MULTIPLE_CHOICE.ordinal)
        for (option in options) array.put(option)
        jsonObject.put("options", array)
        jsonObject.put(OPTION_TYPE, optionType.ordinal)
        jsonObject.put("answer", answer.toJson())
        return jsonObject
    }

    enum class OptionType {
        Text, Image, Score
    }

    class Answer(var userAnswer: Int?, val correctAnswers: List<Int>) : ChildQuestion.Answer {

        constructor(userAnswer: Int?, correctAnswer: Int) : this(userAnswer, listOf(correctAnswer))

        override val correct: Boolean
            get() = correctAnswers.any { it == userAnswer }

        fun toJson(): JSONObject {
            val jsonObject = JSONObject()
            userAnswer?.let{ jsonObject.put("user_answer", userAnswer) }
                ?: jsonObject.put("user_answer", JSONObject.NULL)
            jsonObject.put(
                "correct_answers",
                JSONArray().apply { correctAnswers.forEach { correctAnswer -> put(correctAnswer) } }
            )
            return jsonObject
        }

    }

}