import com.donald.abrsmappserver.utils.RandomIntegerGenerator.Combination
import org.junit.jupiter.api.Test

class CombinationTest {

    @Test
    fun test() {
        val combination = Combination(22, 5)
        repeat(6) {
            println(combination.nextBoolean())
        }
    }

}