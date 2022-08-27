package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomPitchGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.TruthQuestion
import java.lang.IllegalArgumentException
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.buildConstraint
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom.Random1
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.using
import com.donald.abrsmappserver.utils.music.new.relPitchOrdinalRange
import com.donald.abrsmappserver.utils.range.toRangeList
import java.sql.Connection
import java.util.*
import kotlin.collections.ArrayList
import com.donald.abrsmappserver.utils.music.TechnicalName as NoteTechnicalName

private const val PARENT_QUESTION_COUNT = 3
private const val CHILD_QUESTION_COUNT = 1
private val STAFF_RANGE = -6..14
private val REL_STEP_LIST = STAFF_RANGE.toRangeList()
private val KEY_ID_LIST = ArrayList<Int>().apply {
    val lowerBound = Key.idFromFifths(-7, Mode.values()[0])
    val upperBound = Key.idFromFifths(7, Mode.values()[Mode.values().size - 1])
    for (keyId in lowerBound..upperBound) {
        if (Key.modeFromId(keyId) != Mode.NatMinor) add(keyId)
    }
}
private val CLEF_TYPES = listOf(Clef.Type.Bass, Clef.Type.Treble)

/*
 * variables
 * 1. clef type
 * 2. key
 * 3. rel step <-- variation
 */

class TechnicalName(database: Connection) : GroupGenerator(
    "technical_name",
    PARENT_QUESTION_COUNT,
    database
) {

    private val random = Random()

    /*
    private val randomForKeyId = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(Key.idFromFifths(-7, Mode.values()[0]))
        .withUpperBound(Key.idFromFifths(7, Mode.values()[Mode.values().size - 1]))
        .excludingIf { id -> Key.modeFromId(id) == Mode.N_MINOR }
        .build()

    private val randomForError = RandomIntegerGeneratorBuilder.generator()
        .withLowerBound(0)
        .withUpperBound(QuestionError.values().size - 1)
        .build()

     */

    private val relPitchOrdinalList = relPitchOrdinalRange(STAFF_RANGE).toRangeList()

    private val randomForPitch = RandomPitchGenerator().apply {
        setStaffBounds(-6, 14)
    }

    private val keyIdConstraint = buildConstraint {
        val lowerBound = Key.idFromFifths(-7, Mode.values()[0])
        val upperBound = Key.idFromFifths(7, Mode.values()[Mode.values().size - 1])
        withRange(lowerBound..upperBound)
        excludingIf { id -> Key.modeFromId(id) == Mode.NatMinor }
    }

    private val errorConstraint = buildConstraint {
        withRange(0 until QuestionError.values().size)
    }

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val relStepRandom = Random1(
            REL_STEP_LIST,
            autoReset = true
        )
        val keyIdRandom = Random1(
            KEY_ID_LIST,
            autoReset = true
        )
        val clefRandom = Random1(
            CLEF_TYPES,
            autoReset = true
        )

        val questions = List(parentQuestionCount) { parentIndex ->
            val pitch: DiatonicPitch
            val key: Key
            var clefType: Clef.Type
            run {
                val relStep = relStepRandom.generateAndExclude()
                val keyId = keyIdRandom.generateAndExclude()

                clefType = clefRandom.generateAndExclude()
                key = Key(keyId)
                val absStep = clefType.baseAbsStep() + relStep

                //val relPitchFifths = pitchFifthsValue(Music.letterFromAbsStep(absStep), key.startPitch().id())
                val letter = Music.letterFromAbsStep(absStep)
                val octave = Music.octaveFromAbsStep(absStep)

                pitch = DiatonicPitch(key, letter, octave)
            }
            //val (clefType, pitch) = pitchRandom.generateAndExclude()
            val isWrong: Boolean = random.nextBoolean()
            //val keyId = keyIdRandom.generateAndExclude()
            //val key = Key(keyId)
            //var clefType = if (random.nextBoolean()) Clef.Type.Bass else Clef.Type.Treble
            //val pitch = randomForPitch.nextPitch(clefType, key)

            var finalPitch: AbsolutePitch? = pitch
            var technicalName = pitch.technicalName()
            if (isWrong) {
                val error = using(errorConstraint) { errorRandom ->
                    if (!nameMixingIsPossible(technicalName)) {
                        errorRandom.exclude(QuestionError.NAME_MIXING.ordinal)
                    }
                    if (!naturalPitchIsPossible(pitch)) {
                        errorRandom.exclude(QuestionError.NATURAL_PITCH.ordinal)
                    }
                    QuestionError.values()[errorRandom.generate()]
                }

                when (error) {
                    QuestionError.NAME_MIXING -> {
                        technicalName = wrongTechnicalName(technicalName, random)
                    }
                    QuestionError.NATURAL_PITCH -> {
                        pitch.naturalize()
                    }
                    QuestionError.WRONG_CLEF -> {
                        val wrongClefType = if (clefType == Clef.Type.Bass) Clef.Type.Treble else Clef.Type.Bass
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
                    Key(0, Mode.Major),
                    null,
                    1, arrayOf(Clef(clefType))
                )
            )
            measure.addNote(Note.pitchedNote(finalPitch, 4, Note.Type.WHOLE, 1))

            ParentQuestion(
                number = parentIndex + 1,
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
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    TruthQuestion(
                        number = childIndex + 1,
                        answer = TruthQuestion.Answer(null, !isWrong)
                    )
                }
            )
        }


        return QuestionGroup(
            name = getGroupName(context.bundle),
            number = groupNumber,
            parentQuestions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("general_truth_group_desc")
                )
            )
        )
    }

}

private enum class QuestionError { NAME_MIXING, NATURAL_PITCH, WRONG_CLEF }

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