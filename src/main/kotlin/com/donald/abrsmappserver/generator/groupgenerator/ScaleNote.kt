package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Measure.Barline
import com.donald.abrsmappserver.utils.music.Notations.NoteArrow
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomIntegerGeneratorBuilder
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.question.Description
import java.sql.Connection
import java.util.*

class ScaleNote(database: Connection) : GroupGenerator("scale_note", database) {

    private val random = Random()
    private val randomForSignature = RandomIntegerGeneratorBuilder.generator()
       .withLowerBound(-7)
       .withUpperBound(7)
       .build()
    private val currentMissingNotes = IntArray(NO_OF_MISSING_NOTES)

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val signatureFifths = randomForSignature.nextInt()
        val mode = Mode.values()[random.nextInt(Mode.NO_OF_MODES)]
        val key = Key(signatureFifths, mode)
        val clefType = Clef.Type.values()[random.nextInt(Clef.Type.values().size)]
        val ascending: Boolean = random.nextBoolean()
        val firstNoteAbsoluteStep = getRandomFirstNoteAbsStep(key, clefType, ascending, random)
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
                Key(0, Mode.MAJOR),
                null,
                1, arrayOf(Clef(clefType))
            ),
            Barline(Barline.BarStyle.LIGHT_LIGHT)
        )

        // TODO: OPTIMIZE THIS SHIT
        var currentMissingNote = 0
        for (i in 0 until NO_OF_NOTES) {
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

        val questions = List(NO_OF_QUESTIONS) { index ->
            val options = ArrayList<String>(NO_OF_OPTIONS)
            val notePosition = currentMissingNotes[index]
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

            if (key.mode() != Mode.MAJOR) {
                numberOfOptionsLeft = NO_OF_OPTIONS - 1
                makeWrongNotesForMajor(correctNote, notePosition, wrongNotes)
            } else {
                numberOfOptionsLeft = NO_OF_OPTIONS - 3
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
                number = index + 1,
                descriptions = emptyList(),
                optionType = MultipleChoiceQuestion.OptionType.Text,
                options = options,
                answer = MultipleChoiceQuestion.Answer(null, dispositions[0]),
                inputHint = context.getString("scale_note_input_hint", (index + 'X'.code).toChar())
            )
        }



        return QuestionGroup(
            number = groupNumber,
            name = getGroupName(context.bundle),
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("scale_note_group_desc", key.string(context.bundle))
                ),
                Description(
                    Description.Type.Score,
                    score.toDocument().asXML()
                )
            )
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
            Clef.Type.TREBLE, Clef.Type.BASS -> {
                val wrongClef = if (clefType == Clef.Type.TREBLE) Clef.Type.BASS else Clef.Type.TREBLE
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
                val firstLetterDisplacement = -(clefType.baseAbsStep() - Clef.Type.TREBLE.baseAbsStep())
                val firstWrongNote = RelativePitch(
                    Letter.valuesBySteps()[Math.floorMod(correctNote.letter().step() + firstLetterDisplacement, Letter.NO_OF_LETTERS)],
                    correctNote.alter()
                )
                val secondLetterDisplacement = -(clefType.baseAbsStep() - Clef.Type.BASS.baseAbsStep())
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

    companion object {

        private const val NO_OF_OPTIONS = 4
        private const val NO_OF_QUESTIONS = 2
        private const val STAFF_LOWER_BOUND = -5
        private const val STAFF_UPPER_BOUND = 13
        private const val NO_OF_NOTES = 8
        private const val NO_OF_MISSING_NOTES = 2

        // TODO: ADD MORE PATTERNS
        private val MISSING_PATTERNS = listOf(
            listOf(5, 6),
            listOf(2, 6),
            listOf(2, 5)
        )

        private fun getRandomFirstNoteAbsStep(key: Key, clefType: Clef.Type, ascending: Boolean, random: Random): Int {
            val firstTonicLetter = key.tonicPitch().letter()
            val lowestAbsoluteStep: Int = if (ascending) {
                clefType.baseAbsStep() + STAFF_LOWER_BOUND
            } else {
                clefType.baseAbsStep() + STAFF_LOWER_BOUND - 1 + NO_OF_NOTES
            }
            val lowestLetter = Letter.fromAbsoluteStep(lowestAbsoluteStep)
            val tonicStepHeight =  // distance between the lowest absolute step and the lowest possible tonic absolute step
                Math.floorMod(firstTonicLetter.step() - lowestLetter.step(), Letter.NO_OF_LETTERS)
            // for example, a C major scale might start at C4 or C5 with a treble clef
            val numberOfPossiblePositions = (STAFF_UPPER_BOUND - STAFF_LOWER_BOUND + 1 - (NO_OF_NOTES - 1) - tonicStepHeight +
                    Letter.NO_OF_LETTERS - 1) /
                    Letter.NO_OF_LETTERS
            val lowestPossibleTonicStep = lowestAbsoluteStep + tonicStepHeight
            return lowestPossibleTonicStep +
                    random.nextInt(numberOfPossiblePositions) * Consts.PITCHES_PER_SCALE
        }

    }

}