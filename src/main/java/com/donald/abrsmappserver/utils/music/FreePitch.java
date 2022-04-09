package com.donald.abrsmappserver.utils.music;

public class FreePitch extends AbsolutePitch
{
    private boolean isConstrained;
    private int constrainStartPitchId;

    public FreePitch(int id)
    {
        super(id);
    }

    public FreePitch(FreePitch that)
    {
        super(that);
        this.isConstrained = that.isConstrained;
        this.constrainStartPitchId = that.constrainStartPitchId;
    }

    public FreePitch(Letter letter, int alter, int octave)
    {
        super(letter, alter, octave);
    }

    @Deprecated
    public FreePitch(Letter letter, Accidental accidental, int octave)
    {
        super(letter, accidental, octave);
    }

    public FreePitch(String noteString)
    {
        super(noteString);
    }

    /*
     * GETTERS
     */
    public FreePitch[] enharmonic()
    {
        var currentNote = new FreePitch(this);
        currentNote.removeConstraint();

        boolean canTranslate = currentNote.translateDown(Interval.DIM_2);
        while(canTranslate)
        {
            canTranslate = currentNote.translateDown(Interval.DIM_2);
        }

        // calculating array size
        int currentNoteRelativeID = relativeIdFromAbsoluteId(currentNote.id());
        int fifths = Math.abs(Interval.DIM_2.fifths());
        int arraySize = (currentNoteRelativeID + fifths) / fifths;
        var notes = new FreePitch[arraySize];

        notes[0] = (new FreePitch(currentNote));

        for(int i = 1; currentNote.translateUp(Interval.DIM_2); i++)
        {
            notes[i] = new FreePitch(currentNote);
        }

        return notes;
    }

    public FreePitch[] enharmonicExc()
    {
        int trueAbsoluteID = id;
        var currentNote = new FreePitch(this);
        currentNote.removeConstraint();

        boolean canTranslate = currentNote.translateDown(Interval.DIM_2);
        while(canTranslate)
        {
            canTranslate = currentNote.translateDown(Interval.DIM_2);
        }

        // calculating array size
        int currentNoteRelativeID = Music.relIdFromAbsId(currentNote.id());
        int fifths = Math.abs(Interval.DIM_2.fifths());
        int arraySize = (currentNoteRelativeID + fifths) / fifths - 1;
        var notes = new FreePitch[arraySize];

        {
            int i = 0;
            do
            {
                if(trueAbsoluteID != currentNote.id())
                {
                    notes[i] = new FreePitch(currentNote);
                    i++;
                }
            } while(currentNote.translateUp(Interval.DIM_2));
        }

        return notes;
    }

    public boolean isConstrained()
    {
        return isConstrained;
    }

    /*
     * SETTERS
     */
    public boolean translateUp(Interval interval)
    {
        int oldRelId = Music.relIdFromAbsId(id);
        int newRelId;
        if(!isConstrained)
        {
            newRelId = oldRelId + interval.fifths();
        }
        else
        {
            newRelId = Math.floorMod(
                oldRelId - constrainStartPitchId + interval.fifths(),
                Music.CHROMATIC_SCALE_LENGTH
            ) + constrainStartPitchId;
        }
        if(Music.isValidRelId(newRelId))
        {
            int letterDisplacement =
                Music.letterFromId(newRelId).step() - Music.letterFromId(oldRelId).step();
            int octave = octave();
            if(letterDisplacement < 0) octave++;
            octave += interval.octaves();
            id = newRelId + Music.PITCHES_PER_OCT * octave;
            return true;
        }
        return false;
    }

    public boolean translateDown(Interval interval)
    {
        int oldRelId = Music.relIdFromAbsId(id);
        int newRelId;
        if(!isConstrained)
        {
            newRelId = oldRelId - interval.fifths();
        }
        else
        {
            newRelId = Math.floorMod(
                oldRelId - constrainStartPitchId + interval.fifths() * -1,
                Music.CHROMATIC_SCALE_LENGTH
            ) + constrainStartPitchId;
        }
        if(Music.isValidRelId(newRelId))
        {
            int letterDisplacement =
                Music.letterFromId(newRelId).step() - Music.letterFromId(oldRelId).step();
            int octave = octave();
            if(letterDisplacement > 0) octave--;
            octave -= interval.octaves();
            id = newRelId + Music.PITCHES_PER_OCT * octave;
            return true;
        }
        return false;
    }

    public void constrain(int startPitchId)
    {
        if(startPitchId < 0 || startPitchId > Music.HIGHEST_REL_ID)
            throw new IllegalArgumentException(startPitchId + " is not a valid start pitch id.");
        int oldRelId = Music.relIdFromAbsId(id);
        int newRelId = Math.floorMod(oldRelId - startPitchId, Music.CHROMATIC_SCALE_LENGTH) +
            startPitchId;

        int letterDisplacement = Music.letterFromId(newRelId).step() - letter().step();
        int accidentalDisplacement = Music.alterFromId(newRelId) - alter();
        int octave = octave();
        // when accidental displacement is negative, it is translating up
        // e.g. A## (2) -> Cb (-1) = -3
        if(accidentalDisplacement < 0 && letterDisplacement < 0)
        {
            octave++;
        }
        // when accidental displacement is positive, it is translating down
        else if(accidentalDisplacement > 0 && letterDisplacement > 0)
        {
            octave--;
        }
        id = newRelId + octave * Music.PITCHES_PER_OCT;

        isConstrained = true;
        constrainStartPitchId = startPitchId;
    }

    public void removeConstraint()
    {
        isConstrained = false;
    }

    public boolean equals(FreePitch that)
    {
        return this.id == that.id;
    }
}
