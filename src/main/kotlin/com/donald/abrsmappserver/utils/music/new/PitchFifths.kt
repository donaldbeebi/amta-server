package com.donald.abrsmappserver.utils.music.new

import com.donald.abrsmappserver.utils.music.Music
import java.lang.Math.floorMod

class PitchFifths(val value: PitchFifthsValue) {

    init {
        if (value.isInvalidPitchFifthsValue) throwInvalidPitchFifthsValue()
    }

    val ordinal: Int
        get() = value.asPitchFifthsValueToOrdinal()

    val letter: Letter
        get() = value.asPitchFifthsValueToLetter()

    val alter: Int
        get() = value.asPitchFifthsValueToAlter()

    fun sharpen(): PitchFifths? {
        return if (alter < 2) {
            PitchFifths(value + Music.FIFTH_TABLE_COLUMN_COUNT)
        } else {
            null
        }
    }

    fun flatten(): PitchFifths? {
        return if (alter > 2) {
            return PitchFifths(value - Music.FIFTH_TABLE_COLUMN_COUNT)
        } else {
            null
        }
    }

    fun naturalize(): PitchFifths? {
        return if (alter != 0) {
            PitchFifths(floorMod(value, PITCH_TABLE_COLUMN_COUNT) + PITCH_TABLE_COLUMN_COUNT * 2,)
        } else {
            null
        }
    }

    //override fun toString() = "${letter.string}$alter"

    companion object Factory {

        operator fun invoke(letter: Letter, alter: Int): PitchFifths {
            if (alter.isInvalidAlterValue) throwInvalidAlterValue()
            return PitchFifths(pitchFifthsValue(letter, alter))
        }

    }

}