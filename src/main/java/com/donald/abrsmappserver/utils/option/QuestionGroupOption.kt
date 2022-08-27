package com.donald.abrsmappserver.utils.option

import com.donald.abrsmappserver.utils.Result
import com.donald.abrsmappserver.utils.otherwise
import com.donald.abrsmappserver.utils.tryGetInt
import com.donald.abrsmappserver.utils.tryGetString
import org.json.JSONException
import org.json.JSONObject

class QuestionGroupOption(
    val identifier: String,
    val count: Int
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("identifier", identifier)
        put("count", count)
    }
}

fun JSONObject.tryToGroupOption(): Result<QuestionGroupOption, JSONException> {
    val identifier = tryGetString("identifier")
        .otherwise { return Result.Error(it) }
        ?: return Result.Error(JSONException("Value for key 'identifier' is null"))
    val count = tryGetInt("count")
        .otherwise { return Result.Error(it) }
        ?: return Result.Error(JSONException("Value for key 'count' is null"))
    return Result.Value(QuestionGroupOption(identifier, count))
}