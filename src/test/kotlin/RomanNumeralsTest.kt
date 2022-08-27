import org.junit.jupiter.api.Test
import java.lang.Math.pow

private val romanNumeralsMap = listOf(
    1000 to "m",
    900 to "cm",
    500 to "d",
    400 to "cd",
    100 to "c",
    90 to "xc",
    50 to "l",
    40 to "xl",
    10 to "x",
    9 to "ix",
    5 to "v",
    4 to "iv",
    1 to "i"
)

/*
private val map: Map<Int, Char> = hashMapOf(
       1 to 'i',
       5 to 'v',
      10 to 'x',
      50 to 'l',
     100 to 'c',
     500 to 'd',
    1000 to 'm'
)

private fun getSymbol(firstDigit: Int, digitCount: Int) {
    firstDigit * digitCount
}

private fun getAnchor(int: Int) {
    require(int > 0)
    // 1. digit count
    val digits = int % 10

}

 */

// TODO: POTENTIAL OPTIMIZATION -> NO NEED TO LOOK UP THE PAIRS THAT HAVE BEEN PREVIOUSLY LOOKED UP
fun Int.toRomanNumerals(): String {
    require(this > 0)
    var value = this
    val builder = StringBuilder()
    while (value > 0) {
        for ((romanValue, romanString) in romanNumeralsMap) {
            if (value >= romanValue) {
                builder.append(romanString)
                value -= romanValue
                break
            }
        }
    }
    return builder.toString()
}

fun Int.toAlphabet(): String {
    //require(this in 1..26)
    require(this > 0)
    var value = this - 1

    val firstLetter = (value % 26 + 'a'.code).toChar()
    value /= 26
    if (value == 0) return firstLetter.toString()

    val builder = StringBuilder()
    builder.append(firstLetter)
    while (value > 0) {
        builder.append((value % 26 + 'a'.code - 1).toChar())
        value /= 26
    }
    return builder.reverse().toString()
}

class RomanNumeralsTest {
    @Test
    fun test() {
        for (int in 1..99) {
            //println("$int -> ${int.toRomanNumerals()}")
            println(int.toAlphabet())
        }
    }
}