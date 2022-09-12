import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.ChromaticScale
import com.donald.abrsmappserver.generator.groupgenerator.NoteCounting
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.question.TruthQuestion
import com.donald.abrsmappserver.server.Server
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import java.util.*

class QuestionGroupGeneratorTest {

    private val random = Random()

    @RepeatedTest(1000)
    //@Test
    fun test() {
        val generator = NoteCounting(Server.sqliteDatabase)
        val context = Context(
            ResourceBundle.getBundle("resources.strings", Locale("en")),
        )
        val group = generator.generateGroup(
            sectionVariation = 1,
            sectionGroupNumber = 1,
            parentQuestionCount = 1,
            context
        )
        val question = group.parentQuestions[0].childQuestions[0] as MultipleChoiceQuestion
        println(question.options.joinToString(","))
        Assertions.assertTrue(question.options.all { it.toInt() > 0 })
    }

}