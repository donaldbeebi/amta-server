package com.donald.abrsmappserver.utils.option

import com.donald.abrsmappserver.utils.Result
import com.donald.abrsmappserver.utils.otherwise
import com.donald.abrsmappserver.utils.tryGetJSONArray
import com.donald.abrsmappserver.utils.tryGetJSONObject
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class PracticeOptions(val sectionOptions: List<SectionOption>) : Iterable<SectionOption> {
    override fun iterator() = sectionOptions.iterator()
    fun toJson(): JSONObject {
        val practiceOptions = JSONArray()
        forEach { sectionOption -> practiceOptions.put(sectionOption) }
        return JSONObject().apply { put("sections", practiceOptions) }
    }
    fun countCost(costPerQuestion: Int) = sumOf { sectionOption ->
        sectionOption.questionGroupOptions.sumOf { it.count * costPerQuestion }
    }
}

fun JSONObject.tryGetPracticeOptions(key: String): Result<PracticeOptions?, JSONException> {
    val optionsJson = tryGetJSONObject(key)
        .otherwise { e -> return Result.Error(e) }
        ?: return Result.Error(JSONException("Value for key '$key' is null"))
    val sections = optionsJson.tryGetJSONArray("sections")
        .otherwise { return Result.Error(it) }
        ?: return Result.Value(null)
    return Result.Value(
        PracticeOptions(
            List(sections.length()) { optionIndex ->
                val sectionOptionJson = sections.tryGetJSONObject(optionIndex)
                    .otherwise { return Result.Error(it) }
                    ?: return Result.Error(JSONException("Value at index $optionIndex is null"))
                    sectionOptionJson.tryToSectionOption() otherwise { return Result.Error(it) }
            }
        )
    )
}