package com.donald.abrsmappserver.utils.RandomIntegerGenerator

import com.donald.abrsmappserver.utils.music.new.span
import java.util.*

inline fun randomInt(
    count: Int,
    range: IntRange,
    noinline exclusionPredicate: ((int: Int) -> Boolean)? = null,
    block: (Int) -> Unit
) {
    // initializing
    val excludedInts = TreeSet<Int>()

}

inline fun randomInt(count: Int, range: IntRange, excluded: IntArray, block: (Int) -> Unit) {
    val random = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(range.first)
        .withUpperBound(range.last)
        .excluding(*excluded)
        .build()
    repeat(count) {
        val int = random.nextIntAndExclude()
        block(int)
    }
}

inline fun randomIntIndexed(count: Int, range: IntRange, excluded: IntArray, block: (Int, Int) -> Unit) {
    val random = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(range.first)
        .withUpperBound(range.last)
        .excluding(*excluded)
        .build()
    repeat(count) { index ->
        val int = random.nextIntAndExclude()
        block(index, int)
    }
}

class Constraint private constructor(
    val range: IntRange,
    val excludedInts: Set<Int>,
) {
    val count: Int = range.span - excludedInts.size

    class Builder {
        private lateinit var range: IntRange
        private var allowsReset = false
        private var excludedInts = TreeSet<Int>()
        fun withRange(range: IntRange): Builder {
            require(range.span > 0 && range.step == 1) { "Range's span must be greater than 0 and range's step must be exactly 1!" }
            this.range = range
            return this
        }
        fun excluding(vararg ints: Int): Builder {
            require(this::range.isInitialized) { "Range has not been set!" }
            require(ints.all { it in range }) { "Some values in $ints are not within range $range" }
            ints.forEach { excludedInts.add(it) }
            return this
        }
        fun excludingIf(predicate: (Int) -> Boolean): Builder {
            require(this::range.isInitialized) { "Range has not been set!" }
            range.forEach { int ->
                val exclude = predicate(int)
                if (exclude) excludedInts.add(int)
            }
            return this
        }
        fun build(): Constraint {
            require(this::range.isInitialized) { "Range has not been set!" }
            return Constraint(range, excludedInts)
        }
    }
}

fun buildConstraint(block: Constraint.Builder.() -> Unit): Constraint {
    val builder = Constraint.Builder()
    builder.block()
    return builder.build()
}

sealed class RandomInt(constraint: Constraint) {
    private val random = Random()
    protected val range = constraint.range
    protected abstract val excludedInts: Set<Int>
    val possibleIntCount: Int
        get() = (range.span + 1) - excludedInts.size

    open fun generate(): Int {
        require(possibleIntCount > 0) { "All of the possible integers have been excluded!" }

        // 1. generate an integer within the bound of the number of possible values
        // e.g. Possible values: 0, 1, 2, 3, 4; excluded: 2; new possible values: 0, 1, 2, 3

        // 1. generate an integer within the bound of the number of possible values
        // e.g. Possible values: 0, 1, 2, 3, 4; excluded: 2; new possible values: 0, 1, 2, 3
        var randomInt = random.nextInt(possibleIntCount)
        randomInt += range.first

        // 2. adjusting the numbers to match the exclusion
        // e.g. generated: 2; after adjustment: 3;
        // very similar to shifting the frame
        // 0, 1, 2, 3 --> 0, 1, _, 3, 4
        //                        +1 +1
        // for every integer excluded

        // 2. adjusting the numbers to match the exclusion
        // e.g. generated: 2; after adjustment: 3;
        // very similar to shifting the frame
        // 0, 1, 2, 3 --> 0, 1, _, 3, 4
        //                        +1 +1
        // for every integer excluded
        for (excludedInt in excludedInts) {
            // if the generated integer is less than the current excluded integer, then it is fine
            if (randomInt < excludedInt) {
                break
            } else randomInt++
        }
        return randomInt
    }
}

class StaticRandomInt(constraint: Constraint) : RandomInt(constraint) {
    override val excludedInts = constraint.excludedInts
}

class DynamicRandomInt(constraint: Constraint, val autoReset: Boolean) : RandomInt(constraint) {
    // TODO: PERHAPS COMBINE THESE? SINCE RESET IS NO LONGER USED
    private val hardExcludedInts = constraint.excludedInts
    override val excludedInts = TreeSet(constraint.excludedInts)

    @Deprecated("Use the constructor with auto reset")
    constructor(constraint: Constraint) : this(constraint, false)

    override fun generate(): Int {
        if (possibleIntCount <= 0 && autoReset) {
            reset()
        }
        return super.generate()
    }

    fun generateAndExclude(): Int {
        return generate().also { exclude(it) }
    }

    fun exclude(int: Int) {
        require(int in range)
        excludedInts.add(int)
    }

    fun exclude(vararg ints: Int) {
        require(ints.all { it in range })
        ints.forEach { excludedInts.add(it) }
    }

    fun excludeIf(predicate: (Int) -> Boolean) {
        hardExcludedInts.forEach { int ->
            val excluded = predicate(int)
            if (excluded) excludedInts.add(int)
        }
    }

    private fun reset() {
        excludedInts.apply {
            clear()
            addAll(hardExcludedInts)
        }
    }
}

@Deprecated("Use instantiation")
inline fun <R> using(
    constraint: Constraint,
    block: (DynamicRandomInt) -> R
): R {
    val randomInt = DynamicRandomInt(constraint)
    return block(randomInt)
}

@Deprecated("Use instantiation")
inline fun <R> using(
    constraint1: Constraint,
    constraint2: Constraint,
    block: (DynamicRandomInt, DynamicRandomInt) -> R
): R {
    val randomInt1 = DynamicRandomInt(constraint1)
    val randomInt2 = DynamicRandomInt(constraint2)
    return block(randomInt1, randomInt2)
}

@Deprecated("Use instantiation")
inline fun <R> using(
    constraint1: Constraint,
    constraint2: Constraint,
    constraint3: Constraint,
    block: (DynamicRandomInt, DynamicRandomInt, DynamicRandomInt) -> R
): R {
    val randomInt1 = DynamicRandomInt(constraint1)
    val randomInt2 = DynamicRandomInt(constraint2)
    val randomInt3 = DynamicRandomInt(constraint3)
    return block(randomInt1, randomInt2, randomInt3)
}

@Deprecated("Use instantiation")
inline fun <R> using(
    constraint1: Constraint,
    constraint2: Constraint,
    constraint3: Constraint,
    constraint4: Constraint,
    block: (DynamicRandomInt, DynamicRandomInt, DynamicRandomInt, DynamicRandomInt) -> R
): R {
    val randomInt1 = DynamicRandomInt(constraint1)
    val randomInt2 = DynamicRandomInt(constraint2)
    val randomInt3 = DynamicRandomInt(constraint3)
    val randomInt4 = DynamicRandomInt(constraint4)
    return block(randomInt1, randomInt2, randomInt3, randomInt4)
}


/*
inline fun RandomIntegerGenerator.using(block: RandomIntegerGenerator.() -> Unit) {
    block()
    clearAllExcludedIntegers()
}

 */