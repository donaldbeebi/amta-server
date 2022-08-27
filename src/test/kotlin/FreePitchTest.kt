import com.donald.abrsmappserver.utils.music.FreePitch
import com.donald.abrsmappserver.utils.music.new.PITCH_FIFTHS_VALUE_RANGE
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.RepeatedTest
import java.util.*

class FreePitchTest {

    @RepeatedTest(100)
    fun test() {
        val random = Random()
        val pitch = FreePitch(random.nextInt(PITCH_FIFTHS_VALUE_RANGE.last + 1))
        if (pitch.alter() != 0) {
            val naturalized = FreePitch(pitch).apply { naturalize() }
            println(pitch.stringWithOctave() + " " + naturalized.stringWithOctave())
            if (naturalized.alter() != 0) {
                Assertions.assertTrue(false)
            }
        }
    }

}