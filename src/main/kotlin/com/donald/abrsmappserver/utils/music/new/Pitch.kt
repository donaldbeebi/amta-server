package com.donald.abrsmappserver.utils.music.new

class Pitch(
    val fifths: PitchFifths,
    val octave: Octave
) {

    val ordinal: Ordinal
        get() = fifths.value.asPitchFifthsValueToOrdinal(octave)

    val letter: Letter
        get() = fifths.letter

    val alter: Alter
        get() = fifths.alter

    constructor(pitch: Pitch) : this(pitch.fifths, pitch.octave)

    fun sharpen(): Pitch? {
        return fifths.sharpen()?.let {
            Pitch(it, octave)
        }
    }

    fun flatten(): Pitch? {
        return fifths.flatten()?.let {
            Pitch(it, octave)
        }
    }

    fun naturalize(): Pitch? {
        return fifths.naturalize()?.let {
            Pitch(it, octave)
        }
    }

    override fun toString(): String {
        return "$letter${alter.asAlterToSymbol()}$octave"
    }

    fun toStringWithoutOctave(): String {
        return "$letter${alter.asAlterToSymbol()}"
    }

    operator fun component1() = letter

    operator fun component2() = alter

    operator fun component3() = octave

    companion object Factory {

        operator fun invoke(letter: Letter, alter: Int, octave: Int): Pitch {
            if (alter.isInvalidAlterValue) throwInvalidAlterValue()
            return Pitch(
                PitchFifths(pitchFifthsValue(letter, alter)),
                octave
            )
        }

    }

}