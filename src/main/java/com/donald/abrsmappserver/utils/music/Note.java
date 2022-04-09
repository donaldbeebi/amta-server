package com.donald.abrsmappserver.utils.music;

import org.dom4j.Element;
import org.json.JSONObject;


// TODO: ADD A CLASS CALLED CHORD OR SOMETHING
public class Note
{
	public enum Type
	{
		BREVE("breve"), WHOLE("whole"), HALF("half"), QUARTER("quarter"), EIGHTH("eighth"),
		SIXTEENTH("16th"), THIRTY_SECOND("32nd"), SIXTY_FOURTH("64th");
		private final String string;
		Type(String string) { this.string = string; }
		public String string() { return this.string; }
	}

	private AbsolutePitch pitch;
	private int duration;
	private Type type;
	private boolean chord;
	private Accidental accidental;
	private int staff;
	private Notations notations;

	private boolean printObject;

	public static Note pitchedNote(AbsolutePitch pitch, int duration,
								   Type type, int staff)
	{
		return new Note(pitch, duration, type, false, null, staff, true);
	}

	public static Note pitchedNote(AbsolutePitch pitch, int duration,
								   Type type, Accidental accidental, int staff)
	{
		return new Note(pitch, duration, type, false, accidental, staff, true);
	}

	public static Note pitchedNote(AbsolutePitch pitch, int duration, Type type,
								   boolean chord, Accidental accidental,
								   int staff, boolean printObject)
	{
		return new Note(pitch, duration, type, chord, accidental, staff, printObject);
	}

	public static Note restNote(int duration, Type type, int staff)
	{
		return new Note(null, duration, type, false, null, staff, true);
	}

	public static Note nullNote(int duration, int staff)
	{
		return new Note(null, duration, null, false, null, staff, true);
	}

	private Note(AbsolutePitch pitch, int duration, Type type, boolean chord,
				 Accidental accidental, int staff, boolean printObject)
	{
		this.pitch = pitch;
		this.duration = duration;
		this.type = type;
		this.accidental = accidental;
		this.chord = chord;
		this.staff = staff;
		this.printObject = printObject;
	}

	public AbsolutePitch pitch()
	{
		return pitch;
	}

	public void setNotations(Notations notations)
	{
		this.notations = notations;
	}

	public void addToXML(Element measure)
	{
		Element note = measure.addElement("note");
		if(pitch != null) pitch.addToXML(note);
		else if(type != null) note.addElement("rest");
		note.addElement("duration").addText(String.valueOf(duration));
		if(type != null) note.addElement("type").addText(type.string());
		if(chord) note.addElement("chord");
		if(accidental != null) note.addElement("accidental").addText(accidental.string());
		note.addElement("staff").addText(String.valueOf(staff));
		note.addAttribute("print-object", printObject ? "yes" : "no");
		if(notations != null) notations.addToXML(note);
	}

	public JSONObject toJson()
	{
		JSONObject object = new JSONObject();
		object.put("print-object", printObject);
		object.put("pitch", pitch.toJson());
		object.put("duration", duration);
		if(type != null) object.put("type", type.string());
		if(accidental != null) object.put("accidental", accidental.string());
		object.put("staff", staff);
		if(notations != null) object.put("notations", notations.toJson());
		return object;
	}
}