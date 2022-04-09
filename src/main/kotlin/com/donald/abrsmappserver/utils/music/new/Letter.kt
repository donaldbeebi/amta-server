package com.donald.abrsmappserver.utils.music.new

import java.lang.Math.floorMod
import java.util.*

enum class Letter(val step: Int, private val string: String) {

    F(3, "F"), C(0, "C"), G(4, "G"), D(1, "D"), A(5, "A"), E(2, "E"), B(6, "B");

    val fifths = ordinal

    override fun toString() = string

    fun toStringForImage() = string.lowercase(Locale.getDefault())

    companion object {

        val cardinality = values().size
        val lettersByFifths = listOf(F, C, G, D, A, E, B)
        val lettersBySteps = listOf(C, D, E, F, G, A, B)

        fun fromLetterFifths(fifths: Int): Letter = lettersByFifths[fifths]

        fun fromSteps(steps: Int): Letter = lettersBySteps[steps]

        fun fromAbsStep(absStep: Int) = lettersBySteps[floorMod(absStep, cardinality)]

        fun fromString(string: String): Letter? {
            lettersByFifths.forEach {
                if (it.string == string) return it
            }
            return null
        }

    }

}