package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.RandomPitchGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Measure.Barline
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.question.Description
import java.sql.Connection
import java.util.*

class NoteNaming(database: Connection) : GroupGenerator("note_naming", database) {

    private val random = Random()

    private val randomForPitch = RandomPitchGenerator().apply {
        setStaffBounds(-6, 14)
    }

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val questions = List(NO_OF_QUESTIONS) { index ->

            val clefType = Clef.Type.values()[random.nextInt(Clef.Type.values().size)]
            val targetPitch = randomForPitch.nextPitch(clefType)
            val score = Score()
            val part = score.newPart("P1")
            val measure = part.newMeasure()
            measure.setAttributes(
                Measure.Attributes(
                    1,
                    Key(Letter.C, 0, Mode.MAJOR),
                    null,
                    1, arrayOf(Clef(clefType))
                )
            )
            score.addPitchedNote(targetPitch, 4, Note.Type.WHOLE)
            measure.setBarline(Barline(Barline.BarStyle.LIGHT_LIGHT))

            val options = ArrayList<String>(NO_OF_OPTIONS)
            options += targetPitch.string()
            for (wrongClef in Clef.Type.values()) {
                if (wrongClef == clefType) continue
                val wrongLetter = Letter.fromAbsoluteStep(
                    targetPitch.absStep() - clefType.baseAbsStep() + wrongClef.baseAbsStep()
                )
                val wrongPitch = RelativePitch(
                    wrongLetter,
                    targetPitch.alter()
                )
                options += wrongPitch.string()
            }
            val dispositions = options.shuffle()

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Score,
                        score.toDocument().asXML()
                    )
                ),
                optionType = MultipleChoiceQuestion.OptionType.Text,
                options = options,
                answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
            )
        }

        return QuestionGroup(
            name = "Note Naming",
            number = groupNumber,
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("note_naming_question_desc")
                )
            )
        )
    }

    companion object {

        private const val NO_OF_QUESTIONS = 1
        private val NO_OF_OPTIONS = Clef.Type.values().size

    }

}