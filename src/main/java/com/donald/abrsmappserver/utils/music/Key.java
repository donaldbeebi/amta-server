package com.donald.abrsmappserver.utils.music;

import org.dom4j.Element;
import org.json.JSONObject;

import java.util.ResourceBundle;

import static java.lang.Math.floorMod;

// TODO: ADD SUPPORT FOR ALTERNATIVE NAMES
/*
 * each key has a diatonic frame denoted by a starting note (not the tonic note) and
 * a scale type
 *
 * essentially, the starting note is the signature (it tells us how many accidentals there are)
 */
// TODO: SEPARATE BETWEEN FIFTHS AND SCALE?
public class Key
{
    // TODO: PERHAPS USE CHROMATIC RELATIVE NOTE??
    // key = starting note * number of modes (4) + mode
    // Eb h minor (lower bound) =  8 * 4 + 0 = 32
    // F# major   (upper bound) = 19 * 4 + 3 = 111

    // TODO: MAYBE SUPPORT F# MAJOR? (START NOTE = 20)
    // TODO: CHECK IF THE TONIC NOTE IS NATURAL
    /*
    public static final int LOWEST_START_NOTE = 8;   //  8 is Cb -> Gb maj and Eb min
    public static final int HIGHEST_START_NOTE = 19; // 19 is  E ->  B maj and  D min
     */
    public static final int LOWEST_START_NOTE = 0;   // Fbb
    public static final int HIGHEST_START_NOTE = 28; // F##
    public static final int VALID_START_NOTE_RANGE = 12;

    public static final int LOWEST_ID = 0; // 32
    public static final int HIGHEST_ID = 1155; // 79
    public static final int VALID_ID_RANGE = HIGHEST_ID - LOWEST_ID;

    // White-key scale = F C G D A E B, C major starts at 1, A minor starts at 4
    public static final int MAJOR_FIRST_NOTE_OFFSET = 1;
    public static final int MINOR_FIRST_NOTE_OFFSET = 4;

    private int id;

    public Key(int id)
    {
        throwErrorIfInvalidID(id);
        this.id = id;
    }

    public Key(Letter tonicNoteLetter, int tonicNoteAlter, Mode mode)
    {
        this.id = idFromTonicNote(Music.relIdFromLetter(tonicNoteLetter, tonicNoteAlter), mode);
    }

    @Deprecated
    public Key(Letter tonicNoteLetter, Accidental tonicNoteAccidental, Mode mode)
    {
        this.id = idFromTonicNote(
            Pitch.relativeIDFromLetter(tonicNoteLetter, tonicNoteAccidental), mode);
    }

    public Key(RelativePitch tonicNote, Mode mode)
    {
        this.id = idFromTonicNote(tonicNote.id(), mode);
    }

    public Key(int fifths, Mode mode)
    {
        this.id = idFromFifths(fifths, mode);
    }

    public Key(String keyString)
    {
        String[] args = keyString.split(" ", 2);
        int tonicNoteId = Music.relIdFromString(args[0]);
        //int tonicNoteID = RelativePitch.relativeIdFromString(args[0]);
        Mode mode = Mode.fromString(args[1]);
        this.id = idFromTonicNote(tonicNoteId, mode);
    }

    public Key(Key that)
    {
        this(that.id);
    }

    public int id() { return this.id; }

    public Mode mode()
    {
        return modeFromId(id);
    }

    public RelativePitch startPitch()
    {
        return new RelativePitch(this.id / Mode.NO_OF_MODES);
    }

    public RelativePitch tonicPitch()
    {
        int offset;
        if(mode() == Mode.MAJOR) offset = MAJOR_FIRST_NOTE_OFFSET;
        else offset = MINOR_FIRST_NOTE_OFFSET;
        return new RelativePitch(this.id / Mode.NO_OF_MODES + offset);
    }

    public String string()
    {
        String tonic = getNthPitch(0).string();
        return tonic + " " + mode().string();
    }

    public String simpleString(ResourceBundle bundle)
    {
        String tonic = getNthPitch(0).string();
        return tonic + " " + mode().simpleString(bundle);
    }

    public String string(ResourceBundle bundle)
    {
        String tonic = getNthPitch(0).string();
        return tonic + " " + mode().string(bundle);
    }

    public int fifths()
    {
        return startPitch().id() - Music.NAT_SCALE_START_NOTE;
    }

    public Key relativeKey()
    {
        Key clone = new Key(this);
        if(mode() == Mode.MAJOR) clone.changeMode(Mode.N_MINOR);
        else clone.changeMode(Mode.MAJOR);
        return clone;
    }

    public Key parallelKey()
    {
        // translating a scale by a major 6
        int factor = mode() == Mode.MAJOR ? -1 : 1;
        int cloneID = translatedID(this.id, Interval.MAJ_6, factor);
        Key clone = null;
        if(isValidID(cloneID))
        {
            clone = new Key(cloneID);
            clone.changeMode(mode() == Mode.MAJOR ? Mode.N_MINOR : Mode.MAJOR);
        }
        else
        {
            System.out.println(this.id + " " + string() + " does not have a parallel key.");
            System.out.println("clone id " + cloneID);
        }
        return clone;
    }

    public Key adjacentKey(int fifths)
    {
        int cloneID = translatedID(this.id, Interval.PER_5, fifths);
        Key clone = null;
        if(isValidID(cloneID))
        {
            clone = new Key(cloneID);
        }
        return clone;
    }

    // TODO: NEGATIVE NTH?
    public RelativePitch getNthPitch(int n)
    {
        RelativePitch startPitch = startPitch();
        boolean sharpen = false;
        int offset;

        // conditionals
        switch (mode())
        {
            case H_MINOR: {
                if (n % Consts.PITCHES_PER_SCALE == 6) sharpen = true;
                offset = MINOR_FIRST_NOTE_OFFSET;
                break;
            }
            case N_MINOR: {
                offset = MINOR_FIRST_NOTE_OFFSET;
                break;
            }
            case M_MINOR: {
                if (n % Consts.PITCHES_PER_SCALE == 5 || n % Consts.PITCHES_PER_SCALE == 6) sharpen = true;
                offset = MINOR_FIRST_NOTE_OFFSET;
                break;
            }
            default: {
                offset = MAJOR_FIRST_NOTE_OFFSET;
                break;
            }
        }

        // TODO: USE DIATONIC NOTE
        startPitch.translate(
            ((n * Interval.MAJ_2.fifths()) + offset) % Consts.PITCHES_PER_SCALE);
        if(sharpen) startPitch.sharpen();

        return startPitch;
    }

    public void changeMode(Mode newMode)
    {
        this.id = idWithNewMode(this.id, newMode);
    }

    protected static int idWithNewMode(int id, Mode newMode)
    {
        return id = (id / 4) * 4 + newMode.ordinal();
    }

    public static int idFromTonicNote(RelativePitch tonicNote, Mode mode)
    {
        return idFromTonicNote(tonicNote.id(), mode);
    }

    public static int idFromTonicNote(int tonicNoteID, Mode mode)
    {
        int translation;
        if(mode == Mode.MAJOR) translation = -MAJOR_FIRST_NOTE_OFFSET;
        else translation = -MINOR_FIRST_NOTE_OFFSET;
        int startingNoteID = tonicNoteID;
        startingNoteID += translation;
        return startingNoteID * Mode.NO_OF_MODES + mode.ordinal();
    }

    public static int idFromFifths(int accidentals, Mode mode)
    {
        return startingNoteIDFromAccidentals(accidentals) * Mode.NO_OF_MODES + mode.ordinal();
    }

    public static int idFromString(String keyString)
    {
        String[] args = keyString.split(" ");
        int tonicNoteID = RelativePitch.relativeIdFromString(args[0]);
        Mode mode = Mode.fromString(args[1]);
        return idFromTonicNote(tonicNoteID, mode);
    }

    /*
     * VALIDATION FUNCTIONS
     */
    public static boolean isValidID(int key) { return key >= LOWEST_ID && key <= HIGHEST_ID; }

    public static void throwErrorIfInvalidID(int key)
    {
        if(!isValidID(key))
            throw new IllegalArgumentException("Key " + key + " is not valid.");
    }

    public static boolean isValidStartingNote(int note)
    {
        return note >= LOWEST_START_NOTE && note <= HIGHEST_START_NOTE;
    }

    public static void throwErrorIfInvalidStartingNote(int note)
    {
        if(!isValidStartingNote(note))
            throw new IllegalArgumentException("Starting note " + note + " is not valid.");
    }

    public void translate(int fifths)
    {
        int id = LOWEST_ID + (this.id - LOWEST_ID + fifths * Mode.NO_OF_MODES);
        if(isValidID(id)) this.id = id;
        else throw new IllegalArgumentException("Key " + this.id + " is translated to " + id +
            ", which is beyond bounds.");
    }

    public boolean translateUp(Interval interval)
    {
        int id = translatedID(this.id, interval, 1);
        if(isValidID(id))
        {
            this.id = id;
            return true;
        }
        return false;
    }

    public boolean translateDown(Interval interval)
    {
        int id = translatedID(this.id, interval, -1);
        if(isValidID(id))
        {
            this.id = id;
            return true;
        }
        return false;
    }

    private static int translatedID(int id, Interval interval, int factor)
    {
        return id + interval.fifths() * Mode.NO_OF_MODES * factor;
    }

    private static RelativePitch startingNoteFromAccidentals(int accidentals)
    {
        return new RelativePitch(accidentals + Music.NAT_SCALE_START_NOTE);
    }

    private static int startingNoteIDFromAccidentals(int accidentals)
    {
        return accidentals + Music.NAT_SCALE_START_NOTE;
    }

    private static int accidentalsFromStartingNoteID(int startNoteID)
    {
        return startNoteID - Music.NAT_SCALE_START_NOTE;
    }

    private static int startNoteID(int keyID)
    {
        return keyID / Mode.NO_OF_MODES;
    }

    private static int keyID(int startNoteID, int modeOrdinal)
    {
        return startNoteID * Mode.NO_OF_MODES + modeOrdinal;
    }

    protected static int startPitchIdFromKeyId(int keyId)
    {
        return keyId / Mode.NO_OF_MODES;
    }

    // TODO: USE THIS IN tonic()
    protected static int tonicPitchIdFromKeyId(int keyId)
    {
        int offset;
        Mode mode = Mode.values()[floorMod(keyId, Mode.NO_OF_MODES)];
        if(mode == Mode.MAJOR) offset = MAJOR_FIRST_NOTE_OFFSET;
        else offset = MINOR_FIRST_NOTE_OFFSET;
        return keyId / Mode.NO_OF_MODES + offset;
    }

    public static Mode modeFromId(int keyId)
    {
        return Mode.values()[floorMod(keyId, Mode.NO_OF_MODES)];
    }

    public void addToXML(Element attributes)
    {
        Element key = attributes.addElement("key");
        key.addElement("fifths")
            .addText(String.valueOf(startNoteID(this.id) - Music.NAT_SCALE_START_NOTE));
        key.addElement("mode").addText(mode() == Mode.MAJOR ? Mode.MAJOR.string() : Mode.N_MINOR.string());
    }

    public JSONObject toJson()
    {
        JSONObject object = new JSONObject();
        object.put("fifths", fifths());
        object.put("mode", mode() == Mode.MAJOR ? Mode.MAJOR.string() : Mode.N_MINOR.string());
        return object;
    }
}