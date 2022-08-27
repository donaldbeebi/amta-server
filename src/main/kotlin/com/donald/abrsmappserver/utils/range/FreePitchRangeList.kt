package com.donald.abrsmappserver.utils.range

import com.donald.abrsmappserver.utils.music.FreePitch
import com.donald.abrsmappserver.utils.music.Music
import kotlin.math.abs

class FreePitchRangeList(
    first: FreePitch,
    last: FreePitch,
    absStep: Int = 1
) : RangeList<FreePitch>(
    first,
    last,
    absStep,
    toOrdinal = { this.ordinal() },
    toElement = { FreePitch(Music.idFromOrdinal(this)) }
)

infix fun FreePitch.listTo(last: FreePitch) = FreePitchRangeList(first = this, last)

infix fun FreePitchRangeList.absStep(absStep: Int) = FreePitchRangeList(this.first, this.last, absStep)

fun FreePitchRange.toRangeList(): FreePitchRangeList{
    val startOrdinal = start.ordinal()
    val endInclusiveOrdinal = endInclusive.ordinal()
    require(
        endInclusiveOrdinal > startOrdinal && step > 0
                || endInclusiveOrdinal < startOrdinal && step < 0
                || endInclusiveOrdinal == startOrdinal
    ) { "FreePitchRange $this does not qualify for a conversion to FreePitchRangeList" }
    return FreePitchRangeList(start, endInclusive, abs(step))
}