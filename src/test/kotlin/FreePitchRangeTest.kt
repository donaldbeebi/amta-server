import com.donald.abrsmappserver.utils.music.FreePitch
import com.donald.abrsmappserver.utils.music.Letter
import com.donald.abrsmappserver.utils.range.step
import com.donald.abrsmappserver.utils.range.toRangeList
import org.junit.jupiter.api.Test

class FreePitchRangeTest {

    @Test
    fun test() {
        val range = (FreePitch(Letter.C, 0, 4)..FreePitch(Letter.C, 0, 5) step 1).toRangeList()
        for (i in range.indices) {
            println(range[i].stringWithOctave())
        }
    }

}