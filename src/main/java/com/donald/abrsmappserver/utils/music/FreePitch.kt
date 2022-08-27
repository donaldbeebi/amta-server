package com.donald.abrsmappserver.utils.music

import com.donald.abrsmappserver.utils.music.AbsolutePitch
import com.donald.abrsmappserver.utils.music.FreePitch
import com.donald.abrsmappserver.utils.music.Music
import java.lang.IllegalArgumentException

class FreePitch : AbsolutePitch, Comparable<FreePitch> {
    var isConstrained = false
        private set
    var constrainStartPitchId = 0

    constructor(id: Int) : super(id) {}
    constructor(that: FreePitch) : super(that) {
        isConstrained = that.isConstrained
        constrainStartPitchId = that.constrainStartPitchId
    }

    constructor(letter: Letter?, alter: Int, octave: Int) : super(letter, alter, octave) {}

    @Deprecated("")
    constructor(letter: Letter?, accidental: Accidental?, octave: Int) : super(letter, accidental, octave) {
    }

    constructor(noteString: String?) : super(noteString) {}

    /*
     * GETTERS
     */
    fun enharmonic(): Array<FreePitch?> {
        val currentNote = FreePitch(this)
        currentNote.removeConstraint()
        var canTranslate = currentNote.translateDown(Interval.DIM_2)
        while (canTranslate) {
            canTranslate = currentNote.translateDown(Interval.DIM_2)
        }

        // calculating array size
        val currentNoteRelativeID = relativeIdFromAbsoluteId(currentNote.id())
        val fifths = Math.abs(Interval.DIM_2.fifths())
        val arraySize = (currentNoteRelativeID + fifths) / fifths
        val notes = arrayOfNulls<FreePitch>(arraySize)
        notes[0] = FreePitch(currentNote)
        var i = 1
        while (currentNote.translateUp(Interval.DIM_2)) {
            notes[i] = FreePitch(currentNote)
            i++
        }
        return notes
    }

    fun enharmonicExc(): Array<FreePitch?> {
        val trueAbsoluteID = id
        val currentNote = FreePitch(this)
        currentNote.removeConstraint()
        var canTranslate = currentNote.translateDown(Interval.DIM_2)
        while (canTranslate) {
            canTranslate = currentNote.translateDown(Interval.DIM_2)
        }

        // calculating array size
        val currentNoteRelativeID = Music.relIdFromAbsId(currentNote.id())
        val fifths = Math.abs(Interval.DIM_2.fifths())
        val arraySize = (currentNoteRelativeID + fifths) / fifths - 1
        val notes = arrayOfNulls<FreePitch>(arraySize)
        run {
            var i = 0
            do {
                if (trueAbsoluteID != currentNote.id()) {
                    notes[i] = FreePitch(currentNote)
                    i++
                }
            } while (currentNote.translateUp(Interval.DIM_2))
        }
        return notes
    }

    /*
     * SETTERS
     */
    fun translateUp(interval: Interval): Boolean {
        val oldRelId = Music.relIdFromAbsId(id)
        val newRelId: Int
        newRelId = if (!isConstrained) {
            oldRelId + interval.fifths()
        } else {
            Math.floorMod(
                oldRelId - constrainStartPitchId + interval.fifths(),
                Music.CHROMATIC_SCALE_LENGTH
            ) + constrainStartPitchId
        }
        if (Music.isValidRelId(newRelId)) {
            val letterDisplacement = Music.letterFromId(newRelId).step() - Music.letterFromId(oldRelId).step()
            var octave = octave()
            if (letterDisplacement < 0) octave++
            octave += interval.octaves()
            id = newRelId + Music.PITCHES_PER_OCT * octave
            return true
        }
        return false
    }

    fun translateDown(interval: Interval): Boolean {
        val oldRelId = Music.relIdFromAbsId(id)
        val newRelId: Int
        newRelId = if (!isConstrained) {
            oldRelId - interval.fifths()
        } else {
            Math.floorMod(
                oldRelId - constrainStartPitchId + interval.fifths() * -1,
                Music.CHROMATIC_SCALE_LENGTH
            ) + constrainStartPitchId
        }
        if (Music.isValidRelId(newRelId)) {
            val letterDisplacement = Music.letterFromId(newRelId).step() - Music.letterFromId(oldRelId).step()
            var octave = octave()
            if (letterDisplacement > 0) octave--
            octave -= interval.octaves()
            id = newRelId + Music.PITCHES_PER_OCT * octave
            return true
        }
        return false
    }

    fun constrain(startPitchId: Int) {
        require(!(startPitchId < 0 || startPitchId > Music.HIGHEST_REL_ID)) { "$startPitchId is not a valid start pitch id." }
        val oldRelId = Music.relIdFromAbsId(id)
        val newRelId = Math.floorMod(oldRelId - startPitchId, Music.CHROMATIC_SCALE_LENGTH) +
                startPitchId
        val letterDisplacement = Music.letterFromId(newRelId).step() - letter().step()
        val accidentalDisplacement = Music.alterFromId(newRelId) - alter()
        var octave = octave()
        // when accidental displacement is negative, it is translating up
        // e.g. A## (2) -> Cb (-1) = -3
        if (accidentalDisplacement < 0 && letterDisplacement < 0) {
            octave++
        } else if (accidentalDisplacement > 0 && letterDisplacement > 0) {
            octave--
        }
        id = newRelId + octave * Music.PITCHES_PER_OCT
        isConstrained = true
        constrainStartPitchId = startPitchId
    }

    fun removeConstraint() {
        isConstrained = false
    }

    fun equals(that: FreePitch): Boolean {
        return id == that.id
    }

    override fun compareTo(other: FreePitch): Int {
        return ordinal() - other.ordinal()
    }
}