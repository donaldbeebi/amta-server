package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.abstractgroupgenerator.GroupGenerator
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.music.Measure.Barline
import com.donald.abrsmappserver.utils.music.*
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.question.ParentQuestion
import com.donald.abrsmappserver.utils.RandomIntegerGenerator.PitchRandom
import java.sql.Connection
import java.util.*

private const val PARENT_QUESTION_COUNT = 1
private const val CHILD_QUESTION_COUNT = 1
private val OPTION_COUNT = Clef.Type.values().size
private val STAFF_RANGE = -6..14

class NoteNaming(database: Connection) : GroupGenerator(
    "note_naming",
    PARENT_QUESTION_COUNT,
    database
) {

    override fun generateGroup(groupNumber: Int, parentQuestionCount: Int, context: Context): QuestionGroup {
        val pitchRandom = PitchRandom(
            staffRange = STAFF_RANGE,
            clefTypes = Clef.Type.types,
            autoReset = true
        )

        val questions = List(parentQuestionCount) { parentIndex ->
            //val clefType = Clef.Type.values()[random.nextInt(Clef.Type.values().size)]
            val (clefType, targetPitch) = pitchRandom.generateAndExclude()
            val score = Score()
            val part = score.newPart("P1")
            val measure = part.newMeasure()
            measure.setAttributes(
                Measure.Attributes(
                    1,
                    Key(Letter.C, 0, Mode.Major),
                    null,
                    1, arrayOf(Clef(clefType))
                )
            )
            score.addPitchedNote(targetPitch, 4, Note.Type.WHOLE)
            measure.setBarline(Barline(Barline.BarStyle.LIGHT_LIGHT))

            val options = ArrayList<String>(OPTION_COUNT)
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
            val (shuffledOptions, dispositions) = options.shuffled()
            //val dispositions = options.shuffle()

            ParentQuestion(
                number = parentIndex + 1,
                descriptions = listOf(
                    Description(
                        Description.Type.Score,
                        score.toDocument().asXML()
                    )
                ),
                childQuestions = List(CHILD_QUESTION_COUNT) { childIndex ->
                    MultipleChoiceQuestion(
                        number = childIndex + 1,
                        optionType = MultipleChoiceQuestion.OptionType.Text,
                        options = shuffledOptions,
                        answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
                    )
                }
            )
        }

        return QuestionGroup(
            name = "Note Naming",
            number = groupNumber,
            parentQuestions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("note_naming_question_desc")
                )
            )
        )
    }

}