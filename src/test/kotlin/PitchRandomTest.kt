import com.donald.abrsmappserver.utils.RandomIntegerGenerator.ConstrainedPitchRandom
import com.donald.abrsmappserver.utils.music.Clef
import com.donald.abrsmappserver.utils.music.Letter
import com.donald.abrsmappserver.utils.music.Music
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.RepeatedTest

private val StaffRange = -3..11
private val PitchConstraintRange = Music.relIdFromLetter(Letter.F, -1)..Music.relIdFromLetter(Letter.G, 0)

class PitchRandomTest {

    @RepeatedTest(500)
    fun test() {
        val pitchRandom = ConstrainedPitchRandom(
            StaffRange,
            Clef.Type.types,
            PitchConstraintRange,
            autoReset = true
        )
        val (clefType, pitch) = pitchRandom.generateAndExclude()
        val relStep = pitch.octave() * 7 + pitch.letter().step() - clefType.baseAbsStep()
        println(clefType.toString() + " " + pitch.stringWithOctave() + " " + relStep.toString())
        Assertions.assertTrue(relStep in -3..11)
    }

}