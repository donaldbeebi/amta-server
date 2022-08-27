import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.ChromaticScale
import com.donald.abrsmappserver.question.TruthQuestion
import com.donald.abrsmappserver.server.Server
import org.junit.jupiter.api.Test
import java.util.*

class QuestionGroupGeneratorTest {

    private val random = Random()

    //@RepeatedTest(1000)
    @Test
    fun test() {
        val generator = ChromaticScale(Server.sqliteDatabase)
        val context = Context(
            ResourceBundle.getBundle("resources.strings", Locale("en")),
        )
        val group = generator.generateGroup(
            groupNumber = 1,
            parentQuestionCount = 10,
            context
        )
        val question = group.parentQuestions[0].childQuestions[0] as TruthQuestion
        val score = question.descriptions[1]
    }

}