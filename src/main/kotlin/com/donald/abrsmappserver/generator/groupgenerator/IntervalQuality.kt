package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.utils.RandomPitchForIntervalGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Measure.Barline
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom.Random1
import com.donald.abrsmappserver.utils.music.new.EXCLUDED_INTERVALS
import com.donald.abrsmappserver.utils.range.toRangeList
import java.sql.Connection
import java.util.*

private const val STAFF_LOWER_BOUND = -7
private const val STAFF_UPPER_BOUND = 15
private const val PARENT_QUESTION_COUNT = 3
private const val CHILD_QUESTION_COUNT = 1
private val OPTION_LIST = List<String>(Interval.Quality.values().size) { i ->
    Interval.Quality.values()[i].string()
}
private val INTERVALS = Interval.intervals - EXCLUDED_INTERVALS
private val KEY_ACC_COUNTS = (-4..4).toRangeList()
private val CLEFS = listOf(Clef.Type.Bass, Clef.Type.Treble)

class IntervalQuality(database: Connection) : GroupGenerator(
    "interval_quality",
    PARENT_QUESTION_COUNT,
    database
) {

    private val random = Random()

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val keyAccCountRandom = Random1(KEY_ACC_COUNTS, autoReset = true)
        val intervalRandom = Random1(INTERVALS, autoReset = true)
        val clefRandom = Random1(CLEFS, autoReset = true)
        val randomForPitches = RandomPitchForIntervalGenerator().apply {
            setStaffBounds(STAFF_LOWER_BOUND, STAFF_UPPER_BOUND)
        }

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            val noAcc: Boolean = random.nextBoolean()
            val key = if (noAcc) Key(0, Mode.Major) else Key(keyAccCountRandom.generateAndExclude(), Mode.Major)
            val clefType = clefRandom.generateAndExclude()
            val interval = intervalRandom.generateAndExclude()
            val (firstPitch, secondPitch) = randomForPitches.randomForInterval(interval, clefType)

            val score = Score()
            val part = score.newPart("P1")
            val measure = part.newMeasure()
            measure.setAttributes(
                Measure.Attributes(1, key, null, 1, arrayOf(Clef(clefType)))
            )
            measure.addNote(Note.pitchedNote(firstPitch, 4, Note.Type.WHOLE, 1))
            measure.addNote(Note.pitchedNote(secondPitch, 4, Note.Type.WHOLE, 1))
            measure.setBarline(Barline(Barline.BarStyle.LIGHT_LIGHT))

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Score,
                        score.toDocument().asXML()
                    )
                ),
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    MultipleChoiceQuestion(
                        number = childIndex + 1,
                        descriptions = emptyList(),
                        optionType = MultipleChoiceQuestion.OptionType.Text,
                        options = OPTION_LIST.toList(),
                        answer = MultipleChoiceQuestion.Answer(null, interval.quality().ordinal)
                    )
                }
            )
        }


        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = parentQuestions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("interval_quality_group_desc")
                )
            )
        )
    }

}