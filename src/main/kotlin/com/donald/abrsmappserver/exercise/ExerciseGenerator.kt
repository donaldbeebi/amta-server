package com.donald.abrsmappserver.exercise

import com.donald.abrsmappserver.generator.groupgenerator.GroupGenerator
import com.donald.abrsmappserver.generator.groupgenerator.*
import com.donald.abrsmappserver.generator.sectiongenerator.*
import com.donald.abrsmappserver.question.QuestionSection
import com.donald.abrsmappserver.utils.*
import org.json.JSONObject
import java.sql.Connection
import java.util.*

const val NO_OF_SECTION_7_VARIATIONS = 29

/*
 * Image naming scheme
 * [question/answer]_[question name]_[variation]
 * variation: indicates the index of the variation and sometimes the question to which the answer belongs
 */
class ExerciseGenerator(database: Connection) {

    /*
    private val groupGenerators = arrayOf(
        TimeSignature(database),
        SimpleCompound(database),
        Rhythm(database),
        NoteGrouping(database),
        Rests(database),
        NoteNaming(database),
        EnharmonicEquivalent(database),
        Transposition(database),
        ClefComparison(database),
        KeySignature(database),
        MelodyKey(database),
        ScaleNote(database),
        ScaleClef(database),
        ChromaticScale(database),
        TechnicalName(database),
        IntervalNaming(database),
        IntervalQuality(database),
        IntervalDrawing(database),
        CadenceChord(database),
        CadenceNaming(database),
        ChordNaming(database),
        InstrumentFact(database),
        MusicalTerm(database),
        OrnamentNaming(database),
        OctaveComparison(database)
    )

    private val groupMap = HashMap<String, GroupGenerator>().apply {
        groupGenerators.forEach { generator ->
            put(generator.identifier, generator)
        }
    }

     */

    private val sectionGenerators = listOf(
        RhythmSection(database),
        PitchSection(database),
        KeysScalesSection(database),
        IntervalsSection(database),
        ChordsSection(database),
        TermsSignsInstrumentsSection(database),
        MusicInContextSection(database)
    )

    private val sectionMap = HashMap<String, SectionGenerator>().apply {
        sectionGenerators.forEach { generator ->
            put(generator.identifier, generator)
        }
    }

    fun generateTest(bundle: ResourceBundle): Exercise {
        val sections = ArrayList<QuestionSection>()
        var currentGroupNumber = 1
        sectionGenerators.forEachIndexed { index, sectionGenerator ->
            val section = sectionGenerator.generateSectionTest(index + 1, currentGroupNumber, bundle)
            currentGroupNumber += sectionGenerator.groupCount
            sections.add(section)
        }
        return Exercise(Exercise.Type.TEST, "Test", Date(), sections)
    }

    fun generatePractice(bundle: ResourceBundle, options: JSONObject): Exercise? {
        // TODO: CHECK AND DEBUG SANITIZE REQUESTS
        val sectionOptions = options.getJSONArrayOrNull("sections") ?: return null
        val sections = ArrayList<QuestionSection>(sectionOptions.length())
        //val groups = ArrayList<QuestionGroup>()
        var currentStartGroupNumber = 1

        for (i in 0 until sectionOptions.length()) {
            val sectionOption = sectionOptions.getJSONObjectOrNull(i) ?: return null
            val identifier = sectionOption.getStringOrNull("identifier") ?: return null
            val generator = sectionMap[identifier]?: return null
            val groupOptions = sectionOption.getJSONArrayOrNull("groups") ?: return null
            if (groupOptions.length() == 0) return null

            sections += generator.generateSectionPractice(i + 1, currentStartGroupNumber, bundle, groupOptions) ?: return null
            currentStartGroupNumber += groupOptions.length()
        }

        if (sections.size == 0) return null

        return Exercise(
            type = Exercise.Type.PRACTICE,
            title = bundle.getString("exercise_title_practice"),
            date = Date(),
            sections = sections
        )
    }

    /*
    private fun loadConfig(): List<SectionGenerator> {
        val config = JSONObject(JSONTokener(FileInputStream(File("src/main/resources/config.json"))))

        val sectionGenerators = ArrayList<SectionGenerator>()
        val sections = config.getJSONArray("sections")

        for (sectionIndex in 0 until sections.length()) {
            val section = sections.getJSONObject(sectionIndex)
            val identifier = section.getString("identifier").replace("\\s,", "").lowercase()

            val groupGenerators = ArrayList<GroupGenerator>()
            val groups = section.getJSONArray("groups")
            for (groupIndex in 0 until groups.length()) {
                // adding groups
                val group = groups.getJSONObject(groupIndex)
                groupMap[group.getString("identifier")]?.let { groupGenerators += it }
                    ?: throw IllegalStateException("Question with name ${group.getString("identifier")} not found")
            }

            // handling section descriptions
            val descriptionsOptions = section.getJSONArray("descriptions")

            val descriptionBuilders = List(descriptionsOptions.length()) { descriptionOptionIndex ->
                val descriptionOption = descriptionsOptions.getJSONObject(descriptionOptionIndex)
                val descriptionType = descriptionOption.getDescriptionType()
                val descriptionContentTemplate = descriptionOption.getString("content")
                check(descriptionContentTemplate.count { it == '#' } < 2)
                // TODO: NO NEED TO USE BUNDLE HERE AFTER WE EMBED THE TEXT KEYS
                DescriptionBuilder { variation, bundle ->
                    val content = if (descriptionType.isText) {
                        bundle.getString(descriptionContentTemplate.replace("#", variation.toString()))
                    } else {
                        descriptionContentTemplate.replace("#", variation.toString())
                    }
                    Description(
                        descriptionType,
                        content
                    )
                }
            }

            sectionGenerators.add(SectionGenerator(identifier, descriptionBuilders, groupGenerators.toList()))
        }
        return sectionGenerators.toList()
    }

     */

}