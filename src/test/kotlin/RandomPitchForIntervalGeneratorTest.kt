import com.donald.abrsmappserver.utils.RandomPitchForIntervalGenerator
import com.donald.abrsmappserver.utils.music.*
import org.junit.jupiter.api.Test

class RandomPitchForIntervalGeneratorTest {

    private val random = RandomPitchForIntervalGenerator()

    @Test
    fun test() {
        val score = Score()
        val part = score.newPart()
        val measure = score.newMeasure(
            Measure.Attributes(
                1,
                Key(Letter.C, 0, Mode.Major),
                Time(1, 4),
                1,
                arrayOf(Clef(Clef.Type.Treble))
            )
        )
        val note = score.addPitchedNote(FreePitch(Letter.C, 1, 6), 1, Note.Type.HALF)
        println(note.pitch().stringWithOctave() + " " + note.accidental())
        println(score.toDocument().asXML())
    }

}