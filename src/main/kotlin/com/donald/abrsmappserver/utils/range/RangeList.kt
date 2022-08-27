package com.donald.abrsmappserver.utils.range

import com.donald.abrsmappserver.utils.music.fmod
import kotlin.math.abs

abstract class RangeList<T> protected constructor(
    val first: T,
    val last: T,
    val absStep: Int = 1,
    val toOrdinal: T.() -> Int,
    val toElement: Int.() -> T
) : List<T> {

    private val firstOrdinal: Int = first.toOrdinal()
    private val lastOrdinal: Int = last.toOrdinal()


    init {
        require(absStep > 0)
    }

    val step = when {
        lastOrdinal < firstOrdinal -> -absStep
        else -> absStep
    }

    //protected abstract fun T.toOrdinal(): Int

    //protected abstract fun Int.toElement(): T

    final override val size = count(firstOrdinal, lastOrdinal, absStep)

    final override fun contains(element: T): Boolean {
        val ordinal = element.toOrdinal()
        return ordinal in firstOrdinal..lastOrdinal && abs(ordinal - firstOrdinal) fmod absStep == 0
    }

    final override fun containsAll(elements: Collection<T>): Boolean = elements.all { it in this }

    final override fun get(index: Int): T {
        if (index !in 0 until size) throw IndexOutOfBoundsException("IntProgList of size $size accessed with index $index")
        return (firstOrdinal + step * index).toElement()
    }

    final override fun isEmpty(): Boolean = size == 0

    final override fun indexOf(element: T): Int {
        val ordinal = element.toOrdinal()
        if (ordinal !in firstOrdinal..lastOrdinal) return -1
        val distanceFromStart = abs(ordinal - firstOrdinal)
        if (distanceFromStart % absStep != 0) return -1
        return distanceFromStart / absStep
    }

    final override fun lastIndexOf(element: T): Int = indexOf(element)

    final override fun iterator(): ListIterator<T> = listIterator()

    final override fun listIterator(): ListIterator<T> = RangeListIterator(0)

    final override fun listIterator(index: Int): ListIterator<T> = RangeListIterator(0)

    final override fun subList(fromIndex: Int, toIndex: Int): List<T> {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "$firstOrdinal..$lastOrdinal absStep $absStep"

    inner class RangeListIterator(var index: Int) : ListIterator<T> {

        override fun hasNext(): Boolean = index < size

        override fun hasPrevious(): Boolean = index >= 0

        override fun next(): T {
            if (!hasNext()) throw NoSuchElementException()
            return get(index).also { index++ }
        }

        override fun nextIndex(): Int {
            //if (!hasNext()) throw IndexOutOfBoundsException()
            return index
        }

        override fun previous(): T {
            if (!hasPrevious()) throw NoSuchElementException()
            return get(index).also { index-- }
        }

        override fun previousIndex(): Int {
            //if (!hasPrevious()) throw NoSuchElementException()
            return index - 1
        }

    }

}

private fun count(first: Int, last: Int, absStep: Int): Int {
    require(absStep > 0)
    val span = abs(last - first) + 1
    return (span + absStep - 1) / absStep
}

/*
infix fun Int.listTo(last: Int) = IntRangeList(first = this, last)
infix fun Int.listUntil(lastExclusive: Int) = IntRangeList(first = this, lastExclusive - 1)
infix fun IntRangeList.absStep(absStep: Int) = IntRangeList(this.first, this.last, absStep)

fun IntProgression.toIntRangeList(): IntRangeList{
    require(
        last > first && step > 0
                || last < first && step < 0
                || last == first
    ) { "Int progression $this does not qualify for a conversion to IntRangeList" }
    return IntRangeList(first, last, abs(step))
}

 */