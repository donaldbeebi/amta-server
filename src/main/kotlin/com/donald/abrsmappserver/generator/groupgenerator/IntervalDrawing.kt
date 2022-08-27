package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.utils.music.Music.STAFF_LOWER_BOUND
import com.donald.abrsmappserver.utils.music.Music.STAFF_UPPER_BOUND
import com.donald.abrsmappserver.utils.RandomPitchForIntervalGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.IntervalInputQuestion
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom.Random1
import com.donald.abrsmappserver.utils.music.new.EXCLUDED_INTERVALS
import com.donald.abrsmappserver.utils.range.toRangeList
import java.sql.Connection
import java.util.*

private const val LOWEST_FIRST_PITCH_FIFTHS = 7
private const val HIGHEST_FIRST_PITCH_FIFTHS = 27
private const val PARENT_QUESTION_COUNT = 4
private const val CHILD_QUESTION_COUNT = 1
private val INTERVALS = Interval.intervals - EXCLUDED_INTERVALS
private val KEY_ACC_COUNTS = (-4..4).toRangeList()
private val CLEFS = listOf(Clef.Type.Bass, Clef.Type.Treble)

class IntervalDrawing(database: Connection) : GroupGenerator(
    "interval_drawing",
    PARENT_QUESTION_COUNT,
    database
) {

    private val random = Random()

    /*
    private val intervalConstraint = Constraint.Builder()
        .withRange(0 until Interval.values().size)
        .excludingIf { intervalIndex -> Interval.values()[intervalIndex] in EXCLUDED_INTERVALS }
        .build()

    private val keyConstraint = Constraint.Builder()
        .withRange(-4..4)
        .build()

     */

    /*
    private val randomForInterval = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(Interval.values().size - 1)
        .excludingIf { intervalIndex -> Interval.values()[intervalIndex] in EXCLUDED_INTERVALS }
        .build()

    private val randomForKey = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(-4)
        .withUpperBound(4)
        .build()
     */

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val intervalRandom = Random1(INTERVALS, autoReset = true)
        val pitchRandom = RandomPitchForIntervalGenerator().apply {
            setStaffBounds(STAFF_LOWER_BOUND, STAFF_UPPER_BOUND)
            setRelIdBounds(LOWEST_FIRST_PITCH_FIFTHS, HIGHEST_FIRST_PITCH_FIFTHS)
        }
        val keyAccCountRandom = Random1(KEY_ACC_COUNTS, autoReset = true)

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            ParentQuestion(
                number = parentIndex + 1,
                descriptions = emptyList(),
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex -> generateChildQuestion(childIndex, intervalRandom, keyAccCountRandom, pitchRandom, context) }
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = parentQuestions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("interval_drawing_group_desc")
                )
            )
        )
    }

    private fun generateChildQuestion(
        childIndex: Int,
        intervalRandom: Random1<Interval>,
        keyAccCountRandom: Random1<Int>,
        pitchRandom: RandomPitchForIntervalGenerator,
        context: Context
    ): IntervalInputQuestion {
        val noAcc = random.nextBoolean()
        //val key = if (noAcc) Key(0, Mode.MAJOR) else Key(randomForKey.nextInt(), Mode.MAJOR)
        val key = if (noAcc) Key(0, Mode.Major) else Key(keyAccCountRandom.generateAndExclude(), Mode.Major)
        val clefType = if (random.nextBoolean()) Clef.Type.Bass else Clef.Type.Treble
        val interval = intervalRandom.generateAndExclude() // TODO: REPLACE THIS
        val (givenPitch, correctPitch) = pitchRandom.randomForInterval(interval, clefType)

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
        return IntervalInputQuestion(
            number = childIndex + 1,
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

}