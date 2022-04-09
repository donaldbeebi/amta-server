package com.donald.abrsmappserver.utils

import com.donald.abrsmappserver.question.Description
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

fun JSONObject.getJSONObjectOrNull(key: String): JSONObject? {
    return try {
        getJSONObject(key)
    } catch (e: JSONException) {
        null
    }
}

fun JSONObject.getJSONArrayOrNull(key: String): JSONArray? {
    return try {
        getJSONArray(key)
    } catch (e: JSONException) {
        null
    }
}

fun JSONObject.getIntOrNull(key: String): Int? {
    return try {
        getInt(key)
    } catch (e: JSONException) {
        null
    }
}

fun JSONObject.getStringOrNull(key: String): String? {
    return try {
        getString(key)
    } catch (e: JSONException) {
        null
    }
}

fun JSONArray.getJSONObjectOrNull(index: Int): JSONObject? {
    return try {
        getJSONObject(index)
    } catch (e: JSONException) {
        null
    }
}

fun JSONArray.getJSONArrayOrNull(index: Int): JSONArray? {
    return try {
        getJSONArray(index)
    } catch (e: JSONException) {
        null
    }
}

fun parseJSONObject(string: String): JSONObject? {
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

fun JSONObject.getDescriptionType(): Description.Type {
    return Description.Type.fromString(getString("type"))
}