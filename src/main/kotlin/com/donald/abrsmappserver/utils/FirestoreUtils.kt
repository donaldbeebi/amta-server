package com.donald.abrsmappserver.utils

import com.google.cloud.firestore.DocumentSnapshot

fun DocumentSnapshot.tryGetString(field: String): Result<String?, RuntimeException> {
    if (!contains(field)) return Result.Error(RuntimeException("Field $field does not exist"))
    return try {
        Result.Value(getString(field))
    } catch (e: RuntimeException) {
        Result.Error(e)
    }
}

fun DocumentSnapshot.tryGetLong(field: String): Result<Long?, RuntimeException> {
    if (!contains(field)) return Result.Error(RuntimeException("Field $field does not exist"))
    return try {
        Result.Value(getLong(field))
    } catch (e: RuntimeException) {
        Result.Error(e)
    }
}