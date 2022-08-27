import com.donald.abrsmappserver.utils.RandomIntegerGenerator.ConstrainedPitchRandom
import com.donald.abrsmappserver.utils.music.Clef
import com.donald.abrsmappserver.utils.music.Interval
import com.donald.abrsmappserver.utils.music.Letter
import com.donald.abrsmappserver.utils.music.Music
import org.junit.jupiter.api.RepeatedTest
import java.util.*
import kotlin.math.abs

class ProgressionCountTest {

    private val random = Random()
    /*
    private val stepRandom = StaticIntRandom(
        constraint = buildConstraint {
            withRange(-5..5)
            excluding(0)
        }
    )

     */
    private val staffRange = -3..11
    private val pitchConstraintRange = Music.relIdFromLetter(Letter.F, -1)..Music.relIdFromLetter(Letter.G, 0)

    private val pitchRandom = ConstrainedPitchRandom(
        staffRange,
        Clef.Type.types,
        pitchConstraintRange,
        autoReset = false
    )

    @RepeatedTest(1)
    fun test() {
        while (pitchRandom.hasNext) {
            val (clefType, pitch) = pitchRandom.generateAndExclude()
            println(clefType.string())
            println(pitch.stringWithOctave())
            println(pitch.constrainStartPitchId)
            repeat(12) {
                print(pitch.stringWithOctave() + " ")
                pitch.translateUp(Interval.MIN_2)
            }
            println()
        }
    }

    private fun count(progression: IntProgression): Int {
        val span = abs(progression.last - progression.first) + 1
        return (span + progression.step - 1) / progression.step
    }

}