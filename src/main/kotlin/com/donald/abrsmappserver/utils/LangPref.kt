package com.donald.abrsmappserver.utils

enum class LangPref(val string: String) {

    EN("en");

    companion object {

        fun fromStringOrNull(string: String): LangPref? {
            values().forEach {
                if (it.string == string) return it
            }
            return null
        }

    }

}