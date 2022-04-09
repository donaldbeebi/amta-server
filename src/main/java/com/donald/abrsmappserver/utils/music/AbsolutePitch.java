package com.donald.abrsmappserver.utils.music;

import org.dom4j.Element;
import org.json.JSONObject;

public abstract class AbsolutePitch extends Pitch
{
    public AbsolutePitch(int id)
    {
        super(id);
    }

    public AbsolutePitch(AbsolutePitch that)
    {
        super(that);
    }

    public AbsolutePitch(Letter letter, int alter, int octave)
    {
        super(Music.absIdFromLetter(letter, alter, octave));
    }

    @Deprecated
    public AbsolutePitch(Letter letter, Accidental accidental, int octave)
    {
        super(absoluteIdFromLetter(letter, accidental, octave));
    }

    public AbsolutePitch(String noteString)
    {
        super(absoluteIDFromString(noteString));
    }

    /*
     * GETTERS
     */
    public int octave()
    {
        return Music.octaveFromId(id);
    }

    public int pitchValue()
    {
        int id = trueId(id());
        return pitchWithinOctave() + octaveFromId(id) * Consts.PITCHES_PER_OCT;
    }

    public int absStep()
    {
        return letter().step() + octave() * Letter.NO_OF_LETTERS;
    }

    // TODO: RENAME
    public int pitchWithinOctave()
    {
        int id = this.id;
        id = trueId(RelativePitch.relativeIdFromAbsoluteId(id));
        return Math.floorMod(((id - 15) * Letter.NO_OF_LETTERS), Consts.PITCHES_PER_OCT);
    }

    public String stringWithOctave()
    {
        return octave() + letter().string() + accidental().symbol();
    }

    public String stringForImage()
    {
        return octave() + letter().stringForImage() + accidental().symbolForImage();
    }

    public void addToXML(Element note)
    {
        Element pitch = note.addElement("pitch");
        pitch.addElement("step").addText(String.valueOf(letter().string()));
        if(alter() != 0) pitch.addElement("alter").addText(String.valueOf(alter()));
        pitch.addElement("octave").addText(String.valueOf(octave()));
    }

    public JSONObject toJson()
    {
        JSONObject object = new JSONObject();
        object.put("step", letter().string());
        object.put("alter", accidental().alter());
        object.put("octave", octave());
        return object;
    }
}
