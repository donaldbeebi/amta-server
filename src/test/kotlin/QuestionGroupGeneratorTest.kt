import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.InstrumentPitch
import com.donald.abrsmappserver.server.Server
import org.junit.jupiter.api.RepeatedTest
import java.util.*

class GroupGeneratorTest {

    private val random = Random()

    @RepeatedTest(1000)
    fun test() {
        val generator = InstrumentPitch(Server.database)
        val context = Context(
            ResourceBundle.getBundle("resources.strings", Locale("en")),
            //14
        )
    }

}