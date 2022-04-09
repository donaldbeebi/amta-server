package com.donald.abrsmappserver.generator.sectiongenerator

import com.donald.abrsmappserver.exercise.Context
import com.donald.abrsmappserver.generator.groupgenerator.*
import com.donald.abrsmappserver.question.Description
import com.donald.abrsmappserver.utils.AndOrStringBuilder
import java.sql.Connection

class RhythmSection(
    database: Connection
) : SectionGenerator(
    identifier = "rhythm",
    generators = listOf(TimeSignature(database), SimpleCompound(database), Rhythm(database), NoteGrouping(database), Rests(database)),
    database = database
)

class PitchSection(
    database: Connection
) : SectionGenerator(
    identifier = "pitch",
    generators = listOf(NoteNaming(database), EnharmonicEquivalent(database), Transposition(database), ClefComparison(database)),
    database = database
)

class KeysScalesSection(
    database: Connection
) : SectionGenerator(
    identifier = "keys_and_scales",
    generators = listOf(KeySignature(database), MelodyKey(database), ScaleNote(database), ScaleClef(database), ChromaticScale(database), TechnicalName(database)),
    database = database
)

class IntervalsSection(
    database: Connection
) : SectionGenerator(
    identifier = "intervals",
    generators = listOf(IntervalNaming(database), IntervalQuality(database), IntervalDrawing(database)),
    database = database
)

class ChordsSection(
    database: Connection
) : SectionGenerator(
    identifier = "chords",
    generators = listOf(CadenceChord(database), CadenceNaming(database), ChordNaming(database)),
    database = database
)

class TermsSignsInstrumentsSection(
    database: Connection
) : SectionGenerator(
    identifier = "terms_signs_and_instruments",
    generators = listOf(InstrumentFact(database), MusicalTerm(database), OrnamentNaming(database)),
    database = database
)

class MusicInContextSection(
    database: Connection
) : SectionGenerator(
    identifier = "music_in_context",
    generators = listOf(OctaveComparison(database)),
    variationCount = 29,
    database = database
) {

    override fun generateDescriptions(context: Context): List<Description> {
        val result = database.prepareStatement("""
            SELECT instrument_1_string_key, instrument_2_string_key
            FROM questions_section_7
            WHERE variation = ?
            LIMIT 1;
        """.trimIndent()).apply {
            setInt(1, context.sectionVariation)
        }.executeQuery()
        result.next()

        val instrument1String = result.getString("instrument_1_string_key")?.let { context.getString(it) }
        val instrument2String = result.getString("instrument_2_string_key")?.let { context.getString(it) }

        val firstDescription = if (instrument1String == null) {
            Description(Description.Type.Text, context.getString("section_7_desc_no_instruments"))
        } else {
            checkNotNull(instrument2String)
            val builder = AndOrStringBuilder(context.getString("and_or_string_builder_and"))
            val arg = builder.append(instrument1String).append(instrument2String).build()
            Description(Description.Type.Text, context.getString("section_7_desc", arg))
        }

        return listOf(
            firstDescription,
            Description(Description.Type.Image, "q_section_7_score_${context.sectionVariation}")
        )
    }

}