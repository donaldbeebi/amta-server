package com.donald.abrsmappserver.utils

import kotlin.math.abs

class IntRangeList(
    val first: Int,
    val last: Int,
    val absStep: Int = 1
) : List<Int> {

    init {
        require(absStep > 0)
    }

    val step = when {
        last < first -> -absStep
        else -> absStep
    }

    override val size = count(first, last, absStep)

    override fun contains(element: Int): Boolean = element in first..last

    override fun containsAll(elements: Collection<Int>): Boolean = elements.all { it in first..last }

    override fun get(index: Int): Int {
        if (index !in 0 until size) throw IndexOutOfBoundsException("IntProgList of size $size accessed with index $index")
        return first + step * index
    }

    override fun isEmpty(): Boolean = size == 0

    override fun indexOf(element: Int): Int {
        if (element !in first..last) return -1
        val distanceFromStart = abs(element - first)
        if (distanceFromStart % absStep != 0) return -1
        return distanceFromStart / absStep
    }

    override fun lastIndexOf(element: Int): Int = indexOf(element)

    override fun iterator(): ListIterator<Int> = listIterator()

    override fun listIterator(): ListIterator<Int> = IntProgListIterator(0)

    override fun listIterator(index: Int): ListIterator<Int> = IntProgListIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): IntRangeList {
        TODO("Not yet implemented")
    }

    override fun toString(): String = "$first..$last absStep $absStep"

    inner class IntProgListIterator(var index: Int) : ListIterator<Int> {
        override fun hasNext(): Boolean = index < size

        override fun hasPrevious(): Boolean = index >= 0

        override fun next(): Int {
            if (!hasNext()) throw NoSuchElementException()
            return get(index).also { index++ }
        }

        override fun nextIndex(): Int {
            //if (!hasNext()) throw IndexOutOfBoundsException()
            return index
        }

        override fun previous(): Int {
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

val IntProgression.count: Int
    get() {
    val span = abs(last - first) + 1
    return (span + step - 1) / step
}

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