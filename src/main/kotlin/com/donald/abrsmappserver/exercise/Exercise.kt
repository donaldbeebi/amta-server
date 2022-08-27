package com.donald.abrsmappserver.exercise

import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.question.Section
import com.donald.abrsmappserver.question.SectionGroup
import org.json.JSONArray
import org.json.JSONObject
import java.lang.StringBuilder
import java.util.*

class Exercise(
    val type: Type,
    val title: String,
    val date: Date,
    private val sectionGroups: List<SectionGroup>
) {

    private val sections: List<Section> = sectionGroups.flatten()
    private val questionGroups: List<QuestionGroup> = sections.flatten()
    private val questions: List<ParentQuestion> = questionGroups.flatten()

    val points: Int
        get() = questions.sumOf { it.points }
    val maxPoints: Int
        get() = questions.sumOf { it.maxPoints }
    val questionCount: Int
        get() = questions.size

    @Deprecated("Use property")
    fun questionCount(): Int {
        return questions.size
    }

    override fun toString(): String {
        val builder = StringBuilder()
        for (group in questionGroups) {
            builder.append(group.toString()).append("\n")
            for (question in group.parentQuestions) {
                builder.append("    ").append(question.toString()).append("\n")
            }
        }
        return builder.toString()
    }

    fun sectionCount(): Int {
        return sectionGroups.size
    }

    fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        val imageArray = JSONArray()
        val sectionGroupArray = JSONArray()
        for (sectionGroup in sectionGroups) {
            sectionGroup.registerImages(imageArray)
            sectionGroupArray.put(sectionGroup.toJson())
        }
        jsonObject.apply {
            put("images", imageArray)
            put(
                "strings",
                JSONArray().apply {
                    sectionGroups.forEach { section -> section.registerStrings(this) }
                }
            )
            put("saved_page_index", 0)
            put("type", type.toString())
            put("title", title)
            put("date", date.time)
            put("time_remaining", 2L * 60 * 60 * 1000)
            put("points", points)
            put("max_points", maxPoints)
            put("section_groups", sectionGroupArray)
        }
        return jsonObject
    }

    enum class Type(private val string: String) {
        TEST("test"), PRACTICE("practice");
        override fun toString() = string
    }

}