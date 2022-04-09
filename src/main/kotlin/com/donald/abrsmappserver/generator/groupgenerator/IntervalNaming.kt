package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.RandomPitchForIntervalGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Measure.Barline
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.utils.music.new.EXCLUDED_INTERVALS
import com.donald.abrsmappserver.utils.music.new.isTested
import java.sql.Connection
import java.util.*

class IntervalNaming(database: Connection) : GroupGenerator("interval_naming", database) {

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
        setStaffBounds(-7, 15)
    }

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val questions = List(NO_OF_QUESTIONS) { index ->
            val noAcc: Boolean = random.nextBoolean()
            val key = if (noAcc) Key(0, Mode.MAJOR) else Key(randomForKey.nextInt(), Mode.MAJOR)
            val clefType = if (random.nextBoolean()) Clef.Type.BASS else Clef.Type.TREBLE
            val interval = Interval.values()[randomForInterval.nextInt()]
            randomForPitches.randomForInterval(interval, clefType)
            var firstPitch = randomForPitches.lowerPitch()
            var secondPitch = randomForPitches.upperPitch()
            val score = Score()
            val part = score.newPart("P1")
            val measure = part.newMeasure()
            measure.setAttributes(
                Measure.Attributes(1, key, null, 1, arrayOf(Clef(clefType)))
            )
            measure.addNote(Note.pitchedNote(firstPitch, 4, Note.Type.WHOLE, 1))
            measure.addNote(Note.pitchedNote(secondPitch, 4, Note.Type.WHOLE, 1))
            measure.setBarline(Barline(Barline.BarStyle.LIGHT_LIGHT))
            val questionDescriptions = listOf(
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
            val wrongOptions = ArrayList<String>()
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
            if ((firstPitch.alter() != 0 || secondPitch.alter() != 0)
                && firstPitch.alter() != secondPitch.alter()
            ) //if((firstPitch.accidental() != Accidental.NONE || secondPitch.accidental() != Accidental.NONE)
            //	&& firstPitch.accidental() != secondPitch.accidental())
            {
                firstPitch.naturalize()
                secondPitch.naturalize()
                if (firstPitch.ordinal() > secondPitch.ordinal()) {
                    val temp = firstPitch
                    firstPitch = secondPitch
                    secondPitch = temp
                }
                val wrongInterval = Interval.between(firstPitch, secondPitch)
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
            val randomForWrongOptions = RandomIntegerGeneratorBuilder.generator()
                .withLowerBound(0)
                .withUpperBound(wrongOptions.size - 1)
                .build()

            val options = List<String>(NO_OF_OPTIONS) { i ->
                if (i == 0) {
                    correctOption
                }
                else {
                    val randomIndex = randomForWrongOptions.nextInt()
                    randomForWrongOptions.exclude(randomIndex)
                    wrongOptions[randomIndex]
                }
            }

            val dispositions = options.shuffle()

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = questionDescriptions,
                optionType = MultipleChoiceQuestion.OptionType.Text,
                options = options,
                answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
            )
        }

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("interval_naming_group_desc")
                )
            )
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 3
        private const val NO_OF_OPTIONS = 4

    }

}