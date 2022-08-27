package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.utils.RandomPitchForIntervalGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Measure.Barline
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom.Random1
import com.donald.abrsmappserver.utils.music.new.EXCLUDED_INTERVALS
import com.donald.abrsmappserver.utils.music.new.isTested
import com.donald.abrsmappserver.utils.range.toRangeList
import java.sql.Connection
import java.util.*

private const val PARENT_QUESTION_COUNT = 3
private const val OPTION_COUNT = 4
private const val CHILD_QUESTION_COUNT = 1
private val INTERVALS = Interval.intervals - EXCLUDED_INTERVALS
private val KEY_ACC_COUNTS = (-4..4).toRangeList()
private val CLEFS = listOf(Clef.Type.Bass, Clef.Type.Treble)

class IntervalNaming(database: Connection) : GroupGenerator(
    "interval_naming",
    PARENT_QUESTION_COUNT,
    database
) {

    private val random = Random()

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val clefRandom = Random1(CLEFS, autoReset = true)
        val intervalRandom = Random1(INTERVALS, autoReset = true)
        val keyAccCountRandom = Random1(KEY_ACC_COUNTS, autoReset = true)
        val randomPitchRandom = RandomPitchForIntervalGenerator().apply {
            setStaffBounds(-7, 15)
        }
        //val relPitchOrdinalRandom = Random1(REL_PITCH_ORDINAL_LIST, autoReset = true)

        val parentQuestions = List(parentQuestionCount) { parentIndex ->
            val noAcc = random.nextBoolean()
            val key = if (noAcc) Key(0, Mode.Major) else Key(keyAccCountRandom.generateAndExclude(), Mode.Major)
            //val clefType = if (random.nextBoolean()) Clef.Type.Bass else Clef.Type.Treble
            val clefType = clefRandom.generateAndExclude()
            val interval = intervalRandom.generateAndExclude()
            val (firstPitch, secondPitch) = randomPitchRandom.randomForInterval(interval, clefType)
            //val firstPitch = randomForPitches.lowerPitch()
            //val secondPitch = randomForPitches.upperPitch()

            val score = Score()
            score.newPart()
            score.newMeasure(Measure.Attributes(1, key, null, 1, arrayOf(Clef(clefType))), Barline(Barline.BarStyle.LIGHT_LIGHT))
            score.addPitchedNote(firstPitch, 4, Note.Type.WHOLE)
            score.addPitchedNote(secondPitch, 4, Note.Type.WHOLE)
            val parentDescriptions = listOf(
                Description(Description.Type.Score, score.toDocument().asXML())
            )
            val answerUsesCompound = random.nextBoolean()
            val correctOption = if (answerUsesCompound) {
                interval.compoundString(context.bundle)
            } else {
                interval.string(context.bundle)
            }

            // TODO: MORE EVEN PROBABILITY DISTRIBUTION
            // generating wrong options
            val wrongOptions = HashSet<String>()//ArrayList<String>()
            // 1. wrong quality
            for (wrongQuality in interval.sameNumberExc()) {
                if (wrongQuality.isTested) {
                    wrongOptions.add(
                        if (random.nextBoolean()) {
                            wrongQuality.string(context.bundle)
                        } else {
                            wrongQuality.compoundString(context.bundle)
                        }
                    )
                }
            }
            // 2. wrong octave
            for (wrongOctave in interval.octaveEquivalentExc()) {
                if (wrongOctave.isTested) {
                    wrongOptions.add(
                        if (random.nextBoolean()) {
                            wrongOctave.string(context.bundle)
                        } else {
                            wrongOctave.compoundString(context.bundle)
                        }
                    )
                }
            }
            // 3. enharmonic
            val enharmonicIntervals = HashSet<Interval>()
            for (enharmonic in interval.enharmonicExc()) {
                if (enharmonic.isTested) {
                    enharmonicIntervals.add(enharmonic)
                    wrongOptions.add(
                        if (random.nextBoolean()) {
                            enharmonic.string(context.bundle)
                        } else {
                            enharmonic.compoundString(context.bundle)
                        }
                    )
                }
            }
            // 4. similar number
            if (interval.number() - 1 >= Music.LOWEST_INTERVAL_NUMBER) {
                for (belowNumber in Interval.intervalsOfNumber(interval.number() - 1)) {
                    if (!enharmonicIntervals.contains(belowNumber) && belowNumber.isTested) {
                        wrongOptions.add(
                            if (random.nextBoolean()) {
                                belowNumber.string(context.bundle)
                            } else {
                                belowNumber.compoundString(context.bundle)
                            }
                        )
                    }
                }
            }
            if (interval.number() + 1 <= Music.HIGHEST_INTERVAL_NUMBER) {
                for (aboveNumber in Interval.intervalsOfNumber(interval.number() + 1)) {
                    if (!enharmonicIntervals.contains(aboveNumber) && aboveNumber.isTested) {
                        wrongOptions.add(
                            if (random.nextBoolean()) {
                                aboveNumber.string(context.bundle)
                            } else {
                                aboveNumber.compoundString(context.bundle)
                            }
                        )
                    }
                }
            }
            // 5. inverse + octave equivalent
            for (octaveEquivalent in interval.octaveEquivalentExc()) {
                if (octaveEquivalent.isTested) {
                    wrongOptions.add(octaveEquivalent.inverse().string(context.bundle))
                }
            }
            // 6. wrong name
            if (interval.isCompound) {
                if (interval.quality() == Interval.Quality.MAJ ||
                    interval.quality() == Interval.Quality.MIN
                ) {
                    wrongOptions.add(
                        Interval.Quality.PER.string(context.bundle) + " " +
                                Interval.stringOfNumber(interval.number(), context.bundle)
                    )
                } else if (interval.quality() == Interval.Quality.PER) {
                    wrongOptions.add(
                        if (random.nextBoolean()) {
                            Interval.Quality.MIN.string(context.bundle)
                        } else {
                            Interval.Quality.MAJ.string(context.bundle) +
                                    " " + Interval.stringOfNumber(interval.number(), context.bundle)
                        }
                    )
                }
            }
            // 7. wrong accidental
            // TODO: PERHAPS ADD BOTH SIMPLE AND COMPOUND??
            if (
            // if one of the alters is not 0
                (firstPitch.alter() != 0 || secondPitch.alter() != 0)
                // and they are not the same
                && firstPitch.alter() != secondPitch.alter()
            ) //if((firstPitch.accidental() != Accidental.NONE || secondPitch.accidental() != Accidental.NONE)
            //	&& firstPitch.accidental() != secondPitch.accidental())
            {
                var firstPitch1 = FreePitch(firstPitch)
                var secondPitch1 = FreePitch(secondPitch)
                firstPitch1.naturalize()
                secondPitch1.naturalize()
                if (firstPitch1.ordinal() > secondPitch1.ordinal()) {
                    val temp = firstPitch1
                    firstPitch1 = secondPitch1
                    secondPitch1 = temp
                }
                val wrongInterval = Interval.between(firstPitch1, secondPitch1)
                if (wrongInterval.isTested) {
                    wrongOptions.add(
                        if (random.nextBoolean()) {
                            wrongInterval.compoundString(context.bundle)
                        } else {
                            wrongInterval.string(context.bundle)
                        }
                    )
                }
            }

            val wrongOptionsArray = wrongOptions.toTypedArray()

            val randomForWrongOptions = RandomIntegerGeneratorBuilder.generator()
                .withLowerBound(0)
                .withUpperBound(wrongOptionsArray.size - 1)
                .build()

            val options = List<String>(OPTION_COUNT) { i ->
                if (i == 0) {
                    correctOption
                }
                else {
                    val randomIndex = randomForWrongOptions.nextInt()
                    randomForWrongOptions.exclude(randomIndex)
                    wrongOptionsArray[randomIndex]
                }
            }

            val dispositions = options.shuffle()

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = parentDescriptions,
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    MultipleChoiceQuestion(
                        number = childIndex + 1,
                        descriptions = emptyList(),
                        optionType = MultipleChoiceQuestion.OptionType.Text,
                        options = options,
                        answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
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
                    context.getString("interval_naming_group_desc")
                )
            )
        )
    }

}