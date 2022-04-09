package com.donald.abrsmappserver.utils.music.new

import java.lang.Math.floorMod
import java.util.*

class Interval(
    val fifths: IntervalFifths,
    val octaves: Int
) {

    val number: IntervalNumber
        get() = intervalNumber(fifths, octaves)
    val quality: Quality
        get() = intervalQuality(fifths)
    val isCompound: Boolean
        get() = octaves > 1


    enum class Quality(private val string: String) {

        DIM("diminished"),
        MIN("minor"),
        PER("perfect"),
        MAJ("major"),
        AUG("augmented");

        override fun toString() = string

        fun toString(bundle: ResourceBundle): String {
            return bundle.getString("${string}_quality_string")
        }

        companion object {
            val qualities = listOf(DIM, MIN, PER, MAJ, AUG)
            val perfQualities = listOf(DIM, PER, AUG)
            val imperfQualities = listOf(DIM, MIN, MAJ, AUG)
        }

    }

}

/*
enum class SimInterval : Interval {

    /*  0 */ DIM_2, DIM_6, DIM_3, DIM_7, DIM_4, DIM_U, DIM_5,
    /*  7 */ MIN_2, MIN_6, MIN_3, MIN_7, PER_4, PER_U, PER_5,
    /* 14 */ MAJ_2, MAJ_6, MAJ_3, MAJ_7, AUG_4, AUG_U, AUG_5,
    /* 21 */ AUG_2, AUG_6, AUG_3, AUG_7;

    override val fifths
        get() = ordinal - intervals.size / 2
    override val octaves = 0
    override val number = intervalNumber(fifths, octaves)
    override val quality = intervalQuality(ordinal, number)
    override val isCompound = false

    companion object {
        val intervals = listOf(*values())
    }

}

enum class ComInterval : Interval {

    /*  0 */ DIM_9, DIM_13, DIM_10, DIM_14, DIM_11, DIM_O, DIM_12,
    /*  7 */ MIN_9, MIN_13, MIN_10, MIN_14, PER_11, PER_O, PER_12,
    /* 14 */ MAJ_9, MAJ_13, MAJ_10, MAJ_14, AUG_11, AUG_O, AUG_12,
    /* 21 */ AUG_9, AUG_13, AUG_10, AUG_14;

    override val fifths
        get() = ordinal - intervals.size / 2
    override val octaves = 1
    override val number = intervalNumber(fifths, octaves)
    override val quality = intervalQuality(ordinal, number)
    override val isCompound = true

    companion object {
        val intervals = listOf(*values())
    }

}

 */