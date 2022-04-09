package com.donald.abrsmappserver.utils

import java.sql.ResultSet

fun ResultSet.getIntOrNull(columnIndex: String): Int? {
    val int = getInt(columnIndex)
    return if (wasNull()) null else int
}