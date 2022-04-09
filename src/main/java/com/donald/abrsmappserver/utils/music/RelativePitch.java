package com.donald.abrsmappserver.utils.music;

// MAYBE MAKE IT INTO ENUMS????
public class RelativePitch extends Pitch
{
    /*       |   F  |   C  |   G  |   D  |   A  |   E  |   B
     *   bb  |   0  |   1  |   2  |   3  |   4  |   5  |   6
     *    b  |   7  |   8  |   9  |  10  |  11  |  12  |  13
     *    0  |  14  |  15  |  16  |  17  |  18  |  19  |  20
     *    #  |  21  |  22  |  23  |  24  |  25  |  26  |  27
     *   ##  |  28  |  29  |  30  |  31  |  32  |  33  |  34
     *    n  |  35  |  36  |  37  |  38  |  39  |  40  |  41
     *
     *  a continuous row of 12 notes will cover the entire chromatic scale, forming a chromatic frame
     *  a continuous row of 7 notes will cover a major / minor scale, forming a diatonic frame
     *   0 - 34 are called the real notes
     *  35 - 41 are called the natural notes
     */
    /*
    public static final int REAL_NOTES_PER_OCT = 35;
    public static final int NATURAL_NOTES_PER_OCT = 7;
    public static final int ALL_NOTES_PER_OCT = REAL_NOTES_PER_OCT + NATURAL_NOTES_PER_OCT;

    public static final int NOTES_PER_ACCIDENTAL = 7;

    public static final int NAT_SCALE_START_NOTE = 14;

    public static final int LOWEST_ID = 0;
    public static final int HIGHEST_ID = 34;

    public static final String LETTER_NAME_PATTERN = "^[A-Z]";
    public static final String ACCIDENTAL_PATTERN = "(?<=^[A-Z])b{1,2}|n|#{1,2}";
    public static final String OCTAVE_PATTERN = "\\d+(?=$|\\s)";

     */

    //private int m_ID;
    // TODO: PERHAPS MOVE ALL STATIC METHODS TO NOTE

    public RelativePitch(int id)
    {
        super(id);
        if(!Music.isValidRelId(id))
        {
            throw new IllegalArgumentException
                ("Relative note instantiated with an invalid note ID: " + id);
        }
    }

    public RelativePitch(RelativePitch that)
    {
        super(that);
    }

    public RelativePitch(Letter letter, int alter)
    {
        super(Music.relIdFromLetter(letter, alter));
    }

    @Deprecated
    public RelativePitch(Letter letter, Accidental accidental)
    {
        super(relativeIDFromLetter(letter, accidental));
    }

    public RelativePitch(String noteString)
    {
        super(Music.relIdFromString(noteString));
    }

    // this will NOT produce natural notes
    public RelativePitch[] enharmonicNotes()
    {
        int currentID = id;
        // going to the lowest enharmonic note
        currentID += ((Music.HIGHEST_REL_ID - currentID) / Interval.DIM_2.fifths()) * Interval.DIM_2.fifths();
        // round-up division
        int fifths = Math.abs(Interval.DIM_2.fifths());
        int arraySize = (currentID + fifths) / fifths;
        RelativePitch[] notes = new RelativePitch[arraySize];

        for(int i = 0; i < arraySize; i++)
        {
            notes[i] = new RelativePitch(currentID);
            currentID += Interval.DIM_2.fifths();
        }

        return notes;
    }

    public RelativePitch[] enharmonicNotesExclusive()
    {
        int trueID = id;
        int currentID = trueID;
        // going to the lowest enharmonic note
        currentID += ((Music.HIGHEST_REL_ID - currentID) / Interval.DIM_2.fifths()) * Interval.DIM_2.fifths();
        // round-up division
        int fifths = Math.abs(Interval.DIM_2.fifths());
        int arraySize = (currentID + fifths) / fifths - 1;
        RelativePitch[] notes = new RelativePitch[arraySize];

        {
            int i = 0;
            while(i < arraySize)
            {
                if(currentID != trueID)
                {
                    notes[i] = new RelativePitch(currentID);
                    i++;
                }
                currentID += Interval.DIM_2.fifths();
            }
        }

        return notes;
    }

    // remove
    // @Deprecated
    public void translate(int fifths)
    {
        //if(alter() == 0) id -= Letter.NO_OF_LETTERS * 3;
        int id = this.id + fifths;
        if(Music.isValidRelId(id)) this.id = id;
        else throw new IllegalArgumentException("Note " + this.id + " is translated to " + id +
            ", which is beyond bounds.");
    }

    public boolean translateUp(Interval interval)
    {
        int newRelId = id + interval.fifths();
        if(Music.isValidRelId(newRelId))
        {
            id = newRelId;
            return true;
        }
        return false;
    }

    public boolean translateDown(Interval interval)
    {
        int newRelId = id - interval.fifths();
        if(Music.isValidRelId(newRelId))
        {
            id = newRelId;
            return true;
        }
        return false;
    }

    public boolean equals(RelativePitch that)
    {
        return this.id == that.id;
    }
}
