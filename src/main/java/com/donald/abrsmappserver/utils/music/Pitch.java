package com.donald.abrsmappserver.utils.music;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: RENAME ID TO FIFTHS
// TODO: TAKE INTO ACCOUNT NEGATIVE OCTAVES (not allowed)

public abstract class Pitch
{
    protected int id;

    protected Pitch(int id)
    {
        this.id = id;
    }

    protected Pitch(Pitch that)
    {
        this.id = that.id;
    }

    public int id()
    {
        return id;
    }

    public int ordinal()
    {
        return Music.ordinalFromId(id);
    }

    public Letter letter()
    {
        return Music.letterFromId(id);
    }

    @Deprecated
    public Accidental accidental()
    {
        return accidentalFromID(id);
    }

    public int alter() { return Music.alterFromId(id); }

    // TODO: SHARPENING A FLAT NOTE MAKES IT NATURAL
    public boolean sharpen()
    {
        if(alter() < 2)
        {
            id += Letter.NO_OF_LETTERS;
            return true;
        }
        return false;
    }

    public boolean flatten()
    {
        if(alter() > 2)
        {
            id -= Letter.NO_OF_LETTERS;
            return true;
        }
        return false;
    }

    public boolean naturalize()
    {
        if(alter() != 0)
        {
            _changeAlter(0);
            return true;
        }
        return false;
    }

    public boolean setAlter(int alter)
    {
        if(alter() != alter)
        {
            _changeAlter(alter);
            return true;
        }
        return false;
    }

    @Deprecated
    public boolean removeAccidental()
    {
        /*
        if(accidental() != Accidental.NONE)
        {
            _changeAccidental(Accidental.NONE);
            return true;
        }

         */
        return false;
    }

    @Deprecated
    public boolean setAccidental(Accidental newAccidental)
    {
        if(accidental() != newAccidental)
        {
            _changeAccidental(newAccidental);
            return true;
        }
        return false;
    }

    // without checking validity
    private void _changeAccidental(Accidental newAccidental)
    {
        int relativeID = relativeIdFromAbsoluteId(id);
        int octave = octaveFromId(id);
        relativeID = relativeID % Letter.NO_OF_LETTERS +
            Letter.NO_OF_LETTERS * newAccidental.ordinal();
        id = relativeID + octave * Music.PITCHES_PER_OCT;
    }

    private void _changeAlter(int newAlter)
    {
        int relativeId = Music.relIdFromAbsId(id);
        int octave = Music.octaveFromId(id);
        relativeId = relativeId % Music.FIFTH_TABLE_COLUMN_COUNT +
            Letter.NO_OF_LETTERS * (newAlter + 2);
        id = relativeId + octave * Music.PITCHES_PER_OCT;
    }

    // find a cleaner implementation
    public boolean offsetLetter(int offset)
    {
        // implement
        return false;
    }

    public String string()
    {
        return letter().string() + accidental().symbol();
    }

    // helper methods
    public static Letter letterFromId(int id)
    {
        // TODO: NO NEED FOR FLOOR MOD?
        return Letter.values()[Math.floorMod(id, Letter.NO_OF_LETTERS)];
    }

    public static Accidental accidentalFromID(int id)
    {
        return Accidental.values()[Math.floorMod(id, Music.PITCHES_PER_OCT) / Letter.NO_OF_LETTERS];
    }

    @Deprecated
    protected static int trueId(int id)
    {
        if (accidentalFromID(id) == Accidental.NATURAL)
        {
            id -= Letter.NO_OF_LETTERS * 3;
        }
        return id;
    }

    public static int relativeIdFromAbsoluteId(int id)
    {
        return id % Music.PITCHES_PER_OCT;
    }

    public static int relativeIDFromLetter(Letter letter, Accidental accidental)
    {
        return letter.ordinal() + accidental.ordinal() * Letter.NO_OF_LETTERS;
    }

    public static int relativeIdFromString(String noteString)
    {
        Matcher matcher;
        Letter letterName = null;
        int alter = 0;

        // 1. parsing letter name
        matcher = Pattern.compile(Music.LETTER_PATTERN).matcher(noteString);
        if(matcher.find()) letterName = Letter.fromString(matcher.group(0));
        assert letterName != null;

        // 2. parsing accidental
        matcher = Pattern.compile(Music.ACCIDENTAL_PATTERN).matcher(noteString);
        if(matcher.find()) alter = Accidental.fromSymbol(matcher.group(0)).alter();

        return Music.relIdFromLetter(letterName, alter);
    }

	public static boolean isValidRelativeID(int id)
    {
        return id >= 0 && id <= Music.HIGHEST_REL_ID;
    }

    public static int octaveFromId(int id)
    {
        return id / Music.PITCHES_PER_OCT;
    }

    public static int absoluteIdFromLetter(Letter letter, Accidental accidental, int octave)
    {
        return RelativePitch.relativeIDFromLetter(letter, accidental) + octave * Music.PITCHES_PER_OCT;
    }

    public static int ordinalFromId(int id)
    {
        return ((((id % 7) * 24) + 18) % 42) + (id % 42) / 7 + (id / 42) * 42;
    }

    public static int idFromOrdinal(int ordinal)
    {
        return ((ordinal / 6 * 2) + 1) % 7 + ordinal % 6 * 7 + (ordinal / 42) * 42;
    }

    public static int absoluteIDFromString(String noteString)
    {
        int octave = 0;
        Matcher matcher;
        matcher = Pattern.compile(Music.OCTAVE_PATTERN).matcher(noteString);
        if(matcher.find()) octave = Integer.parseInt(matcher.group(0));

        int relativeID = RelativePitch.relativeIdFromString(noteString);
        return relativeID + Music.PITCHES_PER_OCT * octave;
    }

    protected static int translatedOctave(int absoluteID, Interval interval, int factor)
    {
        absoluteID = trueId(absoluteID);
        int octave = octaveFromId(absoluteID);
        int offset = factor >= 0 ? 0 : -6;
        Letter letter = letterFromId(absoluteID);
        octave += (letter.step() + (interval.number() * factor + offset)) /
            Letter.NO_OF_LETTERS;
        return octave;
    }
}
