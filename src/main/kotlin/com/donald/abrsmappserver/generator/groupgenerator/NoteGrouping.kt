package com.donald.abrsmappserver.generator.groupgenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.question.QuestionGroup
import com.donald.abrsmappserver.question.MultipleChoiceQuestion
import com.donald.abrsmappserver.utils.ArrayShuffler
import com.donald.abrsmappserver.question.Description
import java.sql.Connection
import java.util.*

class NoteGrouping(database: Connection) : GroupGenerator("note_grouping", database) {

    private val random = Random()

    override fun generateGroup(groupNumber: Int, context: Context): QuestionGroup {
        val questions = List(NO_OF_QUESTIONS) { index ->
            val variation: Int = random.nextInt(NO_OF_VARIATIONS)
            val images = Array(NO_OF_OPTIONS) { i ->
                if (i == 0) {
                    "a_note_grouping_" + (variation + 1) + "_t"
                } else {
                    "a_note_grouping_" + (variation + 1) + "_f" + i
                }
            }
            val dispositions = ArrayShuffler.shuffle(images)
            val descriptions = ArrayList<Description>(NO_OF_OPTIONS * 2)
            for (i in 0 until NO_OF_OPTIONS) {
                descriptions += Description(
                    Description.Type.Text,
                    context.getString("note_grouping_index", i + 1)
                )
                descriptions += Description(
                    Description.Type.Image,
                    images[i]
                )
            }

            val options = List(NO_OF_OPTIONS) { i ->
                context.getString("note_grouping_index", i + 1)
            }

            MultipleChoiceQuestion(
                number = index + 1,
                descriptions = descriptions,
                optionType = MultipleChoiceQuestion.OptionType.Text,
                options = options,
                answer = MultipleChoiceQuestion.Answer(null, dispositions[0])
            )
        }

        return QuestionGroup(
            number = groupNumber,
            name = getGroupName(context.bundle),
            questions = questions,
            descriptions = listOf(
                Description(
                    Description.Type.Text,
                    context.getString("note_grouping_question_desc")
                )
            )
        )
    }

    companion object {

        private const val NO_OF_VARIATIONS = 10
        private const val NO_OF_QUESTIONS = 1
        private const val NO_OF_OPTIONS = 4

    }
}