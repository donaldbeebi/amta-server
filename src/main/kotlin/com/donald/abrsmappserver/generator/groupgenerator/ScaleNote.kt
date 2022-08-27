package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Measure.Barline
import com.donald.abrsmappserver.utils.music.Notations.NoteArrow
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.multirandom.Random3
import java.sql.Connection
import java.util.*

private const val OPTION_COUNT = 4
private const val PARENT_QUESTION_COUNT = 1
private const val CHILD_QUESTION_COUNT = 2
private const val STAFF_LOWER_BOUND = -5
private const val STAFF_UPPER_BOUND = 13
private const val NOTE_COUNT = 8
private const val MISSING_NOTE_COUNT = 2

// TODO: ADD MORE PATTERNS
private val MISSING_PATTERNS = listOf(
    listOf(5, 6),
    listOf(2, 6),
    listOf(2, 5)
)

class ScaleNote(database: Connection) : GroupGenerator(
    "scale_note",
    PARENT_QUESTION_COUNT,
    database
) {

    private val random = Random()

    private val keyFifthsList = (-7..7).toList()
    /*
    private val signatureConstraint = buildConstraint {
        withRange(-7..7)
    }

     */

    //private val currentMissingNotes = IntArray(MISSING_NOTE_COUNT)

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val variationRandom = Random3(
            keyFifthsList,
            Mode.modes,
            Clef.Type.types,
            autoReset = true
        )

        val questions = List(parentQuestionCount) { parentIndex ->
            val (keyFifths, mode, clefType) = variationRandom.generateAndExclude()

            //val signatureFifths = signatureRandom.generateAndExclude()
            //val mode = Mode.values()[random.nextInt(Mode.NO_OF_MODES)]
            val key = Key(keyFifths, mode)
            //val clefType = Clef.Type.values()[random.nextInt(Clef.Type.values().size)]
            val ascending: Boolean = random.nextBoolean()
            val firstNoteAbsoluteStep = getRandomFirstNoteAbsStep(key, clefType, ascending)
            val missingNotes = MISSING_PATTERNS[random.nextInt(MISSING_PATTERNS.size)]
            val pitch = DiatonicPitch(
                key,
                Music.letterFromAbsStep(firstNoteAbsoluteStep),
                Music.octaveFromAbsStep(firstNoteAbsoluteStep)
            )
            val score = Score()
            score.newPart("P1")
            score.newMeasure(
                Measure.Attributes(
                    1,
                    Key(0, Mode.Major),
                    null,
                    1, arrayOf(Clef(clefType))
                ),
                Barline(Barline.BarStyle.LIGHT_LIGHT)
            )

            // TODO: OPTIMIZE THIS SHIT
            var currentMissingNote = 0
            val currentMissingNotes = MutableList<Int?>(MISSING_NOTE_COUNT) { null }
            for (i in 0 until NOTE_COUNT) {
                var isMissing = false
                for (missingNote in missingNotes) {
                    if (pitch.degree() == missingNote) {
                        isMissing = true
                        break
                    }
                }
                if (isMissing) {
                    val note = score.addPitchedNote(pitch, 4, Note.Type.WHOLE, false, false)
                    val notations = Notations()
                    notations.setNoteArrow(
                        NoteArrow((currentMissingNote + 'X'.code).toChar().toString())
                    )
                    note.setNotations(notations)
                    currentMissingNotes[currentMissingNote] = pitch.degree()
                    currentMissingNote++
                } else score.addPitchedNote(DiatonicPitch(pitch), 4, Note.Type.WHOLE)
                if (ascending) pitch.translateUp(DiatonicInterval.SECOND) else pitch.translateDown(DiatonicInterval.SECOND)
            }

            val childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                val options = ArrayList<String>(OPTION_COUNT)
                val notePosition = currentMissingNotes[childIndex]!!
                val correctNote = key.getNthPitch(notePosition)
                options += correctNote.string()

                /*
                 * if special kind of minor
                 * | 6th and 7th -> different types of accidentals (2-3 options)
                 * other scales
                 * | one letter off (abandon)
                 * | different clef (provide the same clef option in both answers and correct accidental) (0-2 options)
                 * | one accidental off (same type of accidental and continuous) (0 - 2 options)
                 * | enharmonic equivalent (0 - 1 option)
                 */

                /*
                 * 1. if special kind of minor
                 *    | choose between 2 / 3 options of different types of accidentals
                 * 2. fill up the rest with different clef, one accidental off, and enharmonic
                 * 3. remember the wrong clefs
                 */
                val wrongNotes = ArrayList<RelativePitch>()
                val numberOfOptionsLeft: Int

                if (key.mode() != Mode.Major) {
                    numberOfOptionsLeft = OPTION_COUNT - 1
                    makeWrongNotesForMajor(correctNote, notePosition, wrongNotes)
                } else {
                    numberOfOptionsLeft = OPTION_COUNT - 3
                    makeWrongNotesForMinor(correctNote, notePosition, clefType, wrongNotes, options)
                }
                val random = RandomIntegerGeneratorBuilder.generator()
                    .withLowerBound(0)
                    .withUpperBound(wrongNotes.size - 1)
                    .build()

                for (i in 0 until numberOfOptionsLeft) {
                    val chosenIndex = random.nextInt()
                    random.exclude(chosenIndex)
                    options += wrongNotes[chosenIndex].string()
                }

                val dispositions = options.shuffle()

                MultipleChoiceQuestion(
                    number = childIndex + 1,
                    optionType = MultipleChoiceQuestion.OptionType.Text,
                    options = options,
                    answer = MultipleChoiceQuestion.Answer(null, dispositions[0]),
                    inputHint = context.getString("scale_note_input_hint", (childIndex + 'X'.code).toChar())
                )
            }

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Text,
                        context.getString("scale_note_group_desc", key.string(context.bundle))
                    ),
                    Description(
                        Description.Type.Score,
                        score.toDocument().asXML()
                    )
                ),
                childQuestions = childQuestions
            )
        }


        return QuestionGroup(
            number = groupNumber,
            name = getGroupName(context.bundle),
            parentQuestions = questions
        )
    }

    private fun makeWrongNotesForMajor(correctNote: RelativePitch, notePosition: Int, wrongNotes: ArrayList<RelativePitch>) {
        for (alter in -2..2) {
            if (alter != correctNote.alter() && alter != 0) {
                val wrongNote = RelativePitch(correctNote)
                wrongNote.setAlter(alter)
                wrongNotes.add(wrongNote)
            }
        }
        if (notePosition == 6) {
            wrongNotes.addAll(correctNote.enharmonicNotesExclusive().toList())
        }
    }

    private fun makeWrongNotesForMinor(correctNote: RelativePitch, notePosition: Int, clefType: Clef.Type, wrongNotes: ArrayList<RelativePitch>, options: ArrayList<String>) {
        when (clefType) {
            Clef.Type.Treble, Clef.Type.Bass -> {
                val wrongClef = if (clefType == Clef.Type.Treble) Clef.Type.Bass else Clef.Type.Treble
                val letterDisplacement = -(clefType.baseAbsStep() - wrongClef.baseAbsStep())
                val firstWrongNote = RelativePitch(
                    Letter.valuesBySteps()[Math.floorMod(correctNote.letter().step() + letterDisplacement, Letter.NO_OF_LETTERS)],
                    correctNote.alter()
                )
                val secondWrongNote = RelativePitch(firstWrongNote)
                var alteredSuccessfully = false
                if (random.nextBoolean()) alteredSuccessfully = secondWrongNote.sharpen()
                if (!alteredSuccessfully) secondWrongNote.flatten()
                options += firstWrongNote.string()
                options += secondWrongNote.string()
            }
            else -> {
                val firstLetterDisplacement = -(clefType.baseAbsStep() - Clef.Type.Treble.baseAbsStep())
                val firstWrongNote = RelativePitch(
                    Letter.valuesBySteps()[Math.floorMod(correctNote.letter().step() + firstLetterDisplacement, Letter.NO_OF_LETTERS)],
                    correctNote.alter()
                )
                val secondLetterDisplacement = -(clefType.baseAbsStep() - Clef.Type.Bass.baseAbsStep())
                val secondWrongNote = RelativePitch(
                    Letter.valuesBySteps()[Math.floorMod(correctNote.letter().step() + secondLetterDisplacement, Letter.NO_OF_LETTERS)],
                    correctNote.alter()
                )
                options += firstWrongNote.string()
                options += secondWrongNote.string()
            }
        }
        for (alter in -2..2) {
            if (alter != correctNote.alter() && alter != 0) {
                val wrongNote = RelativePitch(correctNote)
                wrongNote.setAlter(0)
                wrongNotes.add(wrongNote)
            }
        }
        if (notePosition == 6) {
            wrongNotes.addAll(correctNote.enharmonicNotesExclusive().toList())
        }
    }

    private fun getRandomFirstNoteAbsStep(key: Key, clefType: Clef.Type, ascending: Boolean): Int {
        val firstTonicLetter = key.tonicPitch().letter()
        val lowestAbsoluteStep: Int = if (ascending) {
            clefType.baseAbsStep() + STAFF_LOWER_BOUND
        } else {
            clefType.baseAbsStep() + STAFF_LOWER_BOUND - 1 + NOTE_COUNT
        }
        val lowestLetter = Letter.fromAbsoluteStep(lowestAbsoluteStep)
        val tonicStepHeight =  // distance between the lowest absolute step and the lowest possible tonic absolute step
            Math.floorMod(firstTonicLetter.step() - lowestLetter.step(), Letter.NO_OF_LETTERS)
        // for example, a C major scale might start at C4 or C5 with a treble clef
        val numberOfPossiblePositions = (STAFF_UPPER_BOUND - STAFF_LOWER_BOUND + 1 - (NOTE_COUNT - 1) - tonicStepHeight +
                Letter.NO_OF_LETTERS - 1) /
                Letter.NO_OF_LETTERS
        val lowestPossibleTonicStep = lowestAbsoluteStep + tonicStepHeight
        return lowestPossibleTonicStep +
                random.nextInt(numberOfPossiblePositions) * Consts.PITCHES_PER_SCALE
    }

}