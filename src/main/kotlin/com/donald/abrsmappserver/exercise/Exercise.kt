package com.donald.abrsmappserver.exercise

import com.donald.abrsmappserver.question.QuestionSection
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.Question
import org.json.JSONArray
import org.json.JSONObject
import java.lang.StringBuilder
import java.util.*

class Exercise(
    val type: Type,
    val title: String,
    val date: Date,
    private val sections: List<QuestionSection>
) {

    private val groups: List<QuestionGroup> = sections.flatten()
    private val questions: List<Question> = groups.flatten()

    val points: Int
        get() = questions.sumOf { it.points }
    val maxPoints: Int
        get() = questions.sumOf { it.maxPoints }
    val questionCount: Int
        get() = questions.size

    fun sectionOf(group: QuestionGroup): QuestionSection {
        sections.forEach { section ->
            if (group in section) return section
        }
        throw IllegalStateException("Question group is not found in this section")
    }

    fun sectionOf(question: Question): QuestionSection {
        sections.forEach { section ->
            if (question in section) return section
        }
        throw IllegalStateException("Question is not found in this section")
    }

    fun groupOf(question: Question): QuestionGroup {
        groups.forEach { group ->
            if (question in group) return group
        }
        throw IllegalStateException("Question is not found in this section")
    }

    fun sectionAt(sectionIndex: Int): QuestionSection {
        return sections[sectionIndex]
    }

    fun groupAt(groupIndex: Int): QuestionGroup {
        return groups[groupIndex]
    }

    fun questionAt(questionIndex: Int): Question {
        return questions[questionIndex]
    }

    fun groupCount(): Int {
        return groups.size
    }

    fun questionAt(groupIndex: Int, localQuestionIndex: Int): Question {
        return groups[groupIndex].questions[localQuestionIndex]
    }

    @Deprecated("Use property")
    fun questionCount(): Int {
        return questions.size
    }

    fun questionIndexOf(question: Question): Int {
        for (i in questions.indices) {
            if (questions[i] === question) return i
        }
        return -1
    }

    fun groupIndexOf(group: QuestionGroup): Int {
        for (i in groups.indices) {
            if (groups[i] === group) return i
        }
        return -1
    }

    fun sectionIndexOf(section: QuestionSection): Int {
        for (i in sections.indices) {
            if (sections[i] === section) return i
        }
        return -1
    }

    override fun toString(): String {
        val builder = StringBuilder()
        for (group in groups) {
            builder.append(group.toString()).append("\n")
            for (question in group.questions) {
                builder.append("    ").append(question.toString()).append("\n")
            }
        }
        return builder.toString()
    }

    fun sectionCount(): Int {
        return sections.size
    }

    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        val imageArray = JSONArray()
        val sectionArray = JSONArray()
        for (section in sections) {
            section.registerImages(imageArray)
            sectionArray.put(section.toJson())
        }
        jsonObject.apply {
            put("images", imageArray)
            put(
                "strings",
                JSONArray().apply {
                    sections.forEach { section -> section.registerStrings(this) }
                }
            )
            put("saved_page_index", 0)
            put("type", type.toString())
            put("title", title)
            put("date", date.time)
            put("time_remaining", 2L * 60 * 60 * 1000)
            put("points", points)
            put("max_points", maxPoints)
            put("sections", sectionArray)
        }
        return jsonObject
    }

    enum class Type(private val string: String) {
        TEST("test"), PRACTICE("practice");
        override fun toString() = string
    }

}