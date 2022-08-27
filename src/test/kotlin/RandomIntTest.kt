import com.donald.abrsmappserver.utils.RandomIntegerGenerator.DynamicIntRandom
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.buildConstraint
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom.Random1
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom.Random2
import com.donald.abrsmappserver.utils.music.Clef
import org.junit.jupiter.api.Test

class RandomIntTest {

    private val list1 = Clef.Type.types
    private val list2 = List(15) { i -> i + 1 }

    private val random1 = Random1(list1, autoReset = true)
    private val random2 = Random1(list2, autoReset = true)
    private val random3 = Random2(list1, list2, autoReset = true)

    @Test
    fun test() {
        repeat(random1.combinationCount * random2.combinationCount) {
            println("${random1.generateAndExclude()}_${random2.generateAndExclude()}")
        }
    }

}