package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.utils.music.Music.STAFF_LOWER_BOUND
import com.donald.abrsmappserver.utils.music.Music.STAFF_UPPER_BOUND
import com.donald.abrsmappserver.utils.RandomPitchForIntervalGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.IntervalInputQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.utils.music.new.EXCLUDED_INTERVALS
import java.sql.Connection
import java.util.*

class IntervalDrawing(database: Connection) : GroupGenerator("interval_drawing", database) {

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
            val interval = Interval.values()[randomForInterval.nextInt()] // TODO: REPLACE THIS
            randomForPitches.randomForInterval(interval, clefType)
            val givenPitch = randomForPitches.lowerPitch()
            val correctPitch = randomForPitches.upperPitch()
            val score = Score()
            score.newPart()
            score.newMeasure(
                Measure.Attributes(
                    1,
                    key,
                    null,
                    1, arrayOf(Clef(clefType))
                )
            )
            score.addPitchedNote(givenPitch, 4, Note.Type.WHOLE)

            // WARNING: THIS MAY NOT WORK IF UNISON INTERVAL IS INCLUDED
            var correctAccidental: Accidental? = null
            if (correctPitch.alter() != Music.alterOfLetterInKey(key, correctPitch.letter())) {
                correctAccidental = Accidental.fromAlter(correctPitch.alter())
            }
            IntervalInputQuestion(
                number = index + 1,
                descriptions = emptyList(),
                score = score,
                answer = IntervalInputQuestion.Answer(null, Note.pitchedNote(correctPitch, 4, Note.Type.WHOLE, correctAccidental, 1)),
                requiredInterval = if (random.nextBoolean()) {
                    interval.string(context.bundle)
                } else {
                    interval.compoundString(context.bundle)
                }
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("interval_drawing_group_desc")
                )
            )
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 3

    }

}