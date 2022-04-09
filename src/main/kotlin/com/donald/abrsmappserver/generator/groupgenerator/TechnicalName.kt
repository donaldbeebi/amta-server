package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomPitchGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.TruthQuestion
import java.lang.IllegalArgumentException
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.question.Description
import java.sql.Connection
import java.util.*
import com.donald.abrsmappserver.utils.music.TechnicalName as NoteTechnicalName

class TechnicalName(database: Connection) : GroupGenerator("technical_name", database) {

    private val random = Random()

    private val randomForKeyId = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(Key.idFromFifths(-7, Mode.values()[0]))
        .withUpperBound(Key.idFromFifths(7, Mode.values()[Mode.values().size - 1]))
        .excludingIf { id -> Key.modeFromId(id) == Mode.N_MINOR }
        .build()

    private val randomForError = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(QuestionError.values().size - 1)
        .build()

    private val randomForPitch = RandomPitchGenerator().apply {
        setStaffBounds(-6, 14)
    }

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val questions = List(NO_OF_QUESTIONS) { index ->
            val isWrong: Boolean = random.nextBoolean()
            val keyId = randomForKeyId.nextInt()
            val key = Key(keyId)
            randomForKeyId.exclude(keyId)
            var clefType = if (random.nextBoolean()) Clef.Type.BASS else Clef.Type.TREBLE
            val pitch = randomForPitch.nextPitch(clefType, key)

            var finalPitch: AbsolutePitch? = pitch
            var technicalName = pitch.technicalName()
            if (!nameMixingIsPossible(technicalName)) {
                randomForError.exclude(QuestionError.NAME_MIXING.ordinal)
            }
            if (!naturalPitchIsPossible(pitch)) {
                randomForError.exclude(QuestionError.NATURAL_PITCH.ordinal)
            }
            if (isWrong) {
                val error = QuestionError.values()[randomForError.nextInt()]
                randomForError.clearAllExcludedIntegers()
                when (error) {
                    QuestionError.NAME_MIXING -> {
                        technicalName = wrongTechnicalName(technicalName, random)
                    }
                    QuestionError.NATURAL_PITCH -> {
                        pitch.naturalize()
                    }
                    QuestionError.WRONG_CLEF -> {
                        val wrongClefType = if (clefType == Clef.Type.BASS) Clef.Type.TREBLE else Clef.Type.BASS
                        val wrongAbsStep = pitch.absStep() - clefType.baseAbsStep() + wrongClefType.baseAbsStep()
                        clefType = wrongClefType
                        finalPitch = FreePitch(
                            Music.letterFromAbsStep(wrongAbsStep),
                            pitch.alter(),
                            Music.octaveFromAbsStep(wrongAbsStep)
                        )
                    }
                }
            }
            val score = Score()
            val part = score.newPart("P1")
            val measure = part.newMeasure()
            measure.setAttributes(
                Measure.Attributes(
                    1,
                    Key(0, Mode.MAJOR),
                    null,
                    1, arrayOf(Clef(clefType))
                )
            )
            measure.addNote(Note.pitchedNote(finalPitch, 4, Note.Type.WHOLE, 1))

            TruthQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Score,
                        score.toDocument().asXML()
                    ),
                    Description(
                        Description.Type.TextEmphasize,
                        context.getString(
                            "technical_name_truth_statement",
                            technicalName.string(context.bundle),
                            // TODO: OPTIMIZE WORK AROUND
                            key.simpleString(context.bundle)
                        )
                    )
                ),
                answer = TruthQuestion.Answer(null, !isWrong)
            )
        }
        randomForKeyId.clearAllExcludedIntegers()

        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("general_truth_group_desc")
                )
            )
        )
    }

    private enum class QuestionError { NAME_MIXING, NATURAL_PITCH, WRONG_CLEF }

    companion object {

        private const val NO_OF_QUESTIONS = 3
        private fun wrongTechnicalName(correctTechnicalName: NoteTechnicalName, random: Random): NoteTechnicalName {
            return when (correctTechnicalName) {
                NoteTechnicalName.TONIC -> /* if (random.nextBoolean()) NoteTechnicalName.SUBTONIC else */ NoteTechnicalName.SUPERTONIC
                NoteTechnicalName.SUPERTONIC -> /* if (random.nextBoolean()) NoteTechnicalName.TONIC else NoteTechnicalName.SUBTONIC */ NoteTechnicalName.TONIC
                NoteTechnicalName.MEDIANT -> NoteTechnicalName.SUBMEDIANT
                NoteTechnicalName.SUBDOMINANT -> NoteTechnicalName.DOMINANT
                NoteTechnicalName.DOMINANT -> NoteTechnicalName.SUBDOMINANT
                NoteTechnicalName.SUBMEDIANT -> NoteTechnicalName.MEDIANT
                //NoteTechnicalName.SUBTONIC -> if (random.nextBoolean()) NoteTechnicalName.TONIC else NoteTechnicalName.SUPERTONIC
                else -> throw IllegalArgumentException(correctTechnicalName.string() + " does not have a similar name.")
            }
        }

        private fun nameMixingIsPossible(technicalName: NoteTechnicalName): Boolean {
            return technicalName != NoteTechnicalName.LEADING_TONE
        }

        private fun naturalPitchIsPossible(pitch: DiatonicPitch): Boolean {
            return pitch.alter() != 0
        }

    }

}