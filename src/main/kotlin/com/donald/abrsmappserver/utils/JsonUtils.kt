package com.donald.abrsmappserver.utils

import com.donald.abrsmappserver.utils.option.PracticeOptions
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun JSONObject.tryGetJSONObject(key: String): Result<JSONObject?, JSONException> {
    val jsonObject: JSONObject? = try {
        getJSONObject(key)
    } catch (e: JSONException) {
        return Result.Error(e)
    }
    return Result.Value(jsonObject)
}

fun JSONObject.tryGetJSONArray(key: String): Result<JSONArray?, JSONException> {
    val jsonArray = try {
        getJSONArray(key)
    } catch (e: JSONException) {
        return Result.Error(e)
    }
    return Result.Value(jsonArray)
}

fun JSONObject.tryGetInt(key: String): Result<Int?, JSONException> {
    val int = try {
        if (isNull(key)) return Result.Value(null)
        getInt(key)
    } catch (e: JSONException) {
        return Result.Error(e)
    }
    return Result.Value(int)
}

fun JSONObject.tryGetString(key: String): Result<String?, JSONException> {
    val string: String? = try {
        getString(key)
    } catch (e: JSONException) {
        return Result.Error(e)
    }
    return Result.Value(string)
}

fun JSONArray.tryGetJSONObject(index: Int): Result<JSONObject?, JSONException> {
    val jsonObject: JSONObject? = try {
        getJSONObject(index)
    } catch (e: JSONException) {
        return Result.Error(e)
    }
    return Result.Value(jsonObject)
}

fun JSONArray.tryGetJSONArray(index: Int): Result<JSONArray?, JSONException> {
    val jsonArray = try {
        getJSONArray(index)
    } catch (e: JSONException) {
        return Result.Error(e)
    }
    return Result.Value(jsonArray)
}

fun parseJsonObject(string: String): JSONObject? {
    return try {
        JSONObject(string)
    } catch (e: JSONException) {
        null
    }
}

inline fun JSONArray.forEachJSONObject(block: (JSONObject) -> Unit) {
    for (i in 0 until length()) {
        block(getJSONObject(i))
    }
}