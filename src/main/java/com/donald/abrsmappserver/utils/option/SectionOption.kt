package com.donald.abrsmappserver.utils.option

import com.donald.abrsmappserver.utils.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class SectionOption(
    val identifier: String,
    val questionGroupOptions: List<QuestionGroupOption>
) : Iterable<QuestionGroupOption> {
    override fun iterator() = questionGroupOptions.iterator()
    fun toJson() = JSONObject().apply {
        put("identifier", identifier)
        put(
            "groups",
            JSONArray().apply { questionGroupOptions.forEach { put(it.toJson()) } }
        )
    }
}

fun JSONObject.tryToSectionOption(): Result<SectionOption, JSONException> {
    val identifier = tryGetString("identifier")
        .otherwise { return Result.Error(it) }
        ?: return Result.Error(JSONException("Value for key 'identifier' is null"))

    val optionArray = tryGetJSONArray("groups")
        .otherwise { return Result.Error(it) }
        ?: return Result.Error(JSONException("Value for key 'options' is null"))

    val options = List(optionArray.length()) { optionIndex ->
        val groupOptionJson = optionArray.tryGetJSONObject(optionIndex)
            .otherwise { return Result.Error(it) }
            ?: return Result.Error(JSONException("Group option at index $optionIndex is null"))
        groupOptionJson.tryToGroupOption()
            .otherwise { return Result.Error(it) }
    }

    return Result.Value(SectionOption(identifier, options))
}