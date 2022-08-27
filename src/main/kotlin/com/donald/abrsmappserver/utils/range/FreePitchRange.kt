package com.donald.abrsmappserver.utils.range

import com.donald.abrsmappserver.utils.music.FreePitch
import com.donald.abrsmappserver.utils.music.Music

class FreePitchRange(
    override val start: FreePitch,
    override val endInclusive: FreePitch,
    val step: Int = 1
) : Iterable<FreePitch>, ClosedRange<FreePitch>
{

    override fun iterator() = FreePitchIterator(start, endInclusive, step)

    class FreePitchIterator(
        private var ordinal: Int,
        private val endOrdinalInclusive: Int,
        private val step: Int
    ) : Iterator<FreePitch> {

        constructor(start: FreePitch, endInclusive: FreePitch, step: Int)
            : this(start.ordinal(), endInclusive.ordinal(), step)

        init {
            require(step >= 1) { "Invalid step of $step" }
        }

        override fun hasNext(): Boolean {
            return ordinal <= endOrdinalInclusive
        }

        override fun next(): FreePitch {
            if (!hasNext()) throw NoSuchElementException()
            val next = Music.idFromOrdinal(ordinal)
            ordinal += step
            return FreePitch(next)
        }

    }
}

infix fun FreePitchRange.step(step: Int) = FreePitchRange(start, endInclusive, step)