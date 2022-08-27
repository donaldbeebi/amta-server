package com.donald.abrsmappserver.utils

sealed class Result<out V, out E> {
    class Value<out V>(val value: V) : Result<V, Nothing>()
    class Error<out E>(val error: E) : Result<Nothing, E>()
}

inline infix fun <V, E> Result<V, E>.otherwise(block: (E) -> Nothing): V {
    when (this) {
        is Result.Value -> return value
        is Result.Error -> block(error)
    }
}

infix fun <V> Result<V, *>.otherwise(defaultValue: V): V {
    return when (this) {
        is Result.Value -> value
        is Result.Error -> defaultValue
    }
}

fun <V> Result<V, *>.valueOrNull(): V? {
    return when (this) {
        is Result.Value -> value
        is Result.Error -> null
    }
}

fun <E> Result<*, E>.errorOrNull(): E? {
    return when (this) {
        is Result.Value -> null
        is Result.Error -> error
    }
}

inline fun <V, E> Result<V, E>.onValue(block: (V) -> Unit): Result<V, E> {
    if (this is Result.Value) { block(value) }
    return this
}

inline fun <V, E> Result<V, E>.onError(block: (E) -> Unit): Result<V, E> {
    if (this is Result.Error) { block(error) }
    return this
}