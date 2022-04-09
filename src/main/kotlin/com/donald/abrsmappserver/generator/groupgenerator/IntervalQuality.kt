package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.utils.RandomPitchForIntervalGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Measure.Barline
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.utils.music.new.EXCLUDED_INTERVALS
import java.sql.Connection
import java.util.*

class IntervalQuality(database: Connection) : GroupGenerator("interval_quality", database) {

    private val random = Random()

    private val randomForInterval = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(Interval.values().size - 1)
        .excludingIf { intervalIndex -> Interval.values()[intervalIndex] in EXCLUDED_INTERVALS }
        .build()

    private val randomForKey = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(-4)
        .withUpperBound(4)
        .build()

    private val randomForPitches = RandomPitchForIntervalGenerator().apply {
        setStaffBounds(STAFF_LOWER_BOUND, STAFF_UPPER_BOUND)
    }

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val questions = List(NO_OF_QUESTIONS) { index ->
            val noAcc: Boolean = random.nextBoolean()
            val key = if (noAcc) Key(0, Mode.MAJOR) else Key(randomForKey.nextInt(), Mode.MAJOR)
            val clefType = if (random.nextBoolean()) Clef.Type.BASS else Clef.Type.TREBLE
            val interval = Interval.values()[randomForInterval.nextInt()]
            randomForPitches.randomForInterval(interval, clefType)
            val firstPitch = randomForPitches.lowerPitch()
            val secondPitch = randomForPitches.upperPitch()
            val score = Score()
            val part = score.newPart("P1")
            val measure = part.newMeasure()
            measure.setAttributes(
                Measure.Attributes(1, key, null, 1, arrayOf(Clef(clefType)))
            )
            measure.addNote(Note.pitchedNote(firstPitch, 4, Note.Type.WHOLE, 1))
            measure.addNote(Note.pitchedNote(secondPitch, 4, Note.Type.WHOLE, 1))
            measure.setBarline(Barline(Barline.BarStyle.LIGHT_LIGHT))

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Score,
                        score.toDocument().asXML()
                    )
                ),
                optionType = MultipleChoiceQuestion.OptionType.Text,
                options = OPTION_LIST.toList(),
                answer = MultipleChoiceQuestion.Answer(null, interval.quality().ordinal)
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("interval_quality_group_desc")
                )
            )
        )
    }

    companion object {

        private const val STAFF_LOWER_BOUND = -7
        private const val STAFF_UPPER_BOUND = 15
        private const val NO_OF_QUESTIONS = 3
        private val OPTION_LIST = List<String>(Interval.Quality.values().size) { i ->
            Interval.Quality.values()[i].string()
        }

    }

}