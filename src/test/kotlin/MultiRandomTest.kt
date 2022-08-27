import com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom.Random4
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MultiRandomTest {

    private enum class Enum { Computer, Tablet, Phone }

    private val random = Random4(
        listOf(1, 2, 5, 12, 51),
        listOf("apple", "pear", "mango", "kiwi"),
        listOf(Enum.Computer, Enum.Tablet, Enum.Phone),
        listOf(true, false),
        autoReset = true
    )

    private val data = HashMap<String, Int>()

    @Test
    fun test() {
        repeat(random.combinationCount * 2) {
            val (int, string, enum, bool) = random.generateAndExclude()
            val resultString = "int: $int, string: $string, enum: $enum, bool: $bool"
            println(resultString)
            val entry = data[resultString]
            if (entry == null) {
                data[resultString] = 1
            } else {
                data[resultString] = entry + 1
            }
        }
        Assertions.assertTrue(data.all { (_, count) -> count == 2 })
    }

}