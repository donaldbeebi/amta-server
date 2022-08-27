package com.donald.abrsmappserver.utils

import com.donald.abrsmappserver.utils.user.Profile
import com.donald.abrsmappserver.utils.user.Profile.LangPref.Companion.tryGetProfileLangPref
import com.donald.abrsmappserver.utils.user.Profile.Type.Companion.tryGetProfileType
import java.sql.ResultSet
import java.sql.SQLException

fun ResultSet.tryGetString(columnLabel: String): Result<String?, SQLException> {
    val string: String? = try {
        getString(columnLabel)
    } catch (e: SQLException) {
        return Result.Error(e)
    }
    return Result.Value(string)
}

fun ResultSet.tryGetInt(columnLabel: String): Result<Int?, SQLException> {
    val int: Int? = try {
        val result = getInt(columnLabel)
        if (wasNull()) null
        else result
    } catch (e: SQLException) {
        return Result.Error(e)
    }
    return Result.Value(int)
}

fun ResultSet.tryGetProfile(uid: String): Result<Profile, SQLException> {
    return try {
        val nickname = tryGetString("nickname")
            .otherwise { return Result.Error(it) }
            ?: return Result.Error(SQLException("Value for key 'nickname' is null"))
        val langPref = tryGetProfileLangPref("lang_pref")
            .otherwise { return Result.Error(it) }
            ?: return Result.Error(SQLException("Value for key 'lang_pref' is null"))
        val type = tryGetProfileType("type")
            .otherwise { return Result.Error(it) }
            ?: return Result.Error(SQLException("Value for key 'type' is null"))
        val points = tryGetInt("points")
            .otherwise { return Result.Error(it) }
            ?: return Result.Error(SQLException("Value for key 'points' is null"))
        Result.Value(Profile(uid, nickname, langPref, type, points))
    } catch (e: SQLException) {
        return Result.Error(e)
    }
}