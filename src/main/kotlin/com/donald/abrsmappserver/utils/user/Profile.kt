package com.donald.abrsmappserver.utils.user

import com.donald.abrsmappserver.utils.*
import com.google.cloud.firestore.DocumentSnapshot
import org.json.JSONException
import org.json.JSONObject
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

data class Profile(
    val uid: String,
    val nickname: String,
    val langPref: LangPref,
    val type: Type,
    val points: Int
) {
    enum class Type(val jsonValue: String) {
        Google("google");
        val databaseValue: String
            get() = jsonValue
        companion object {
            val types = values()

            fun fromOrdinal(ordinal: Int) = types[ordinal]

            fun fromJsonValueOrNull(jsonValue: String) = types.firstOrNull { it.jsonValue == jsonValue }
            fun fromJsonValue(jsonValue: String) = types.first { it.jsonValue == jsonValue }

            fun JSONObject.tryGetProfileType(key: String): Result<Type?, JSONException> {
                val jsonValue = tryGetString(key).otherwise { return Result.Error(it) }
                    ?: return Result.Value(null)
                return Result.Value(
                    fromJsonValueOrNull(jsonValue) ?: return Result.Error(JSONException("Value $jsonValue cannot be converted to Profile.Type"))
                )
            }

            fun fromDatabaseValueOrNull(databaseValue: String): Type? {
                return types.firstOrNull { it.databaseValue == databaseValue }
            }
            fun fromDatabaseValue(databaseValue: String): Type {
                return types.first { it.databaseValue == databaseValue }
            }

            fun PreparedStatement.setProfileType(parameterIndex: Int, value: Type) {
                setString(parameterIndex, value.databaseValue)
            }
            fun ResultSet.tryGetProfileType(columnLabel: String): Result<Type?, SQLException> {
                val databaseValue = tryGetString(columnLabel).otherwise { return Result.Error(it) }
                    ?: return Result.Value(null)
                return Result.Value(
                    fromDatabaseValueOrNull(databaseValue) ?: return Result.Error(SQLException("Value $databaseValue cannot be converted to Profile.Type"))
                )
            }
            fun DocumentSnapshot.tryGetProfileType(field: String): Result<Type?, RuntimeException> {
                if (!contains(field)) return Result.Error(RuntimeException("Field $field does not exist"))
                return try {
                    val databaseValue = tryGetString(field) otherwise { return Result.Error(it) }
                    val type = databaseValue?.let { fromDatabaseValueOrNull(it) }
                        ?: return Result.Error(RuntimeException("Value $databaseValue is not a valid profile type"))
                    Result.Value(type)
                } catch (e: RuntimeException) {
                    Result.Error(e)
                }
            }
        }
    }
    enum class LangPref(val code: String) {
        English("en");
        val jsonValue: String
            get() = code
        val databaseValue: String
            get() = code
        companion object {
            val langPrefs = values()
            fun fromCode(code: String) = langPrefs.first { it.code == code }

            fun fromJsonValueOrNull(jsonValue: String) = langPrefs.firstOrNull { it.jsonValue == jsonValue }
            fun fromJsonValue(jsonValue: String) = langPrefs.first { it.jsonValue == jsonValue }

            fun JSONObject.tryGetProfileLangPref(key: String): Result<LangPref?, JSONException> {
                val jsonValue = tryGetString(key).otherwise { return Result.Error(it) }
                    ?: return Result.Value(null)
                return Result.Value(
                    fromJsonValueOrNull(jsonValue) ?: return Result.Error(JSONException("Value $jsonValue cannot be converted to Profile.LangPref"))
                )
            }

            fun fromDatabaseValueOrNull(databaseValue: String) = langPrefs.firstOrNull { it.databaseValue == databaseValue }
            fun fromDatabaseValue(databaseValue: String) = langPrefs.first { it.databaseValue == databaseValue }

            fun PreparedStatement.setProfileLangPref(parameterIndex: Int, value: LangPref) {
                setString(parameterIndex, value.databaseValue)
            }
            fun ResultSet.tryGetProfileLangPref(columnLabel: String): Result<LangPref?, SQLException> {
                val databaseValue = tryGetString(columnLabel).otherwise { return Result.Error(it) }
                    ?: return Result.Value(null)
                return Result.Value(
                    fromDatabaseValueOrNull(databaseValue) ?: return Result.Error(SQLException("Value $databaseValue cannot be converted to Profile.LangPref"))
                )
            }

            fun DocumentSnapshot.tryGetProfileLangPref(field: String): Result<LangPref?, RuntimeException> {
                if (!contains(field)) return Result.Error(RuntimeException("Field $field does not exist"))
                return try {
                    val databaseValue = tryGetString(field) otherwise { return Result.Error(it) }
                    val langPref = databaseValue?.let { fromDatabaseValueOrNull(it) }
                        ?: return Result.Error(RuntimeException("Value $databaseValue is not a valid profile language preference"))
                    Result.Value(langPref)
                } catch (e: RuntimeException) {
                    Result.Error(e)
                }
            }
        }
    }
    fun toJson() = JSONObject().apply {
        put("uid", uid)
        put("nickname", nickname)
        put("lang_pref", langPref.jsonValue)
        put("type", type.jsonValue)
        put("points", points)
    }

    fun toMap() = mapOf(
        "nickname" to nickname,
        "lang_pref" to langPref.jsonValue,
        "type" to type.databaseValue,
        "points" to points
    )
}