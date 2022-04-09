package com.donald.abrsmappserver.utils.music;

import org.dom4j.Element;
import org.json.JSONObject;

public class Notations
{
	/*
	public enum Type
	{
		START("start"), STOP("stop"), SINGLE("single");
		private String string;
		Type(String string) { this.string = string; }
		public String string() { return this.string; }
	}

	public enum Placement
	{
		ABOVE("above"), BELOW("below");
		private String string;
		Placement(String string) { this.string = string; }
		public String string() { return this.string; }
	}

	 */

	public static class NoteArrow
	{
		private final String label;

		public NoteArrow(String label)
		{
			this.label = label;
		}

		public void addToXML(Element notations)
		{
			notations.addElement("other-notation")
				.addAttribute("notation-name", "note-arrow")
				.addText(label);
		}

		public JSONObject toJson()
		{
			JSONObject object = new JSONObject();
			object.put("label", label);
			return object;
		}
	}

	private NoteArrow noteArrow;

	public void setNoteArrow(NoteArrow noteArrow)
	{
		this.noteArrow = noteArrow;
	}

	public void addToXML(Element note)
	{
		Element notations = note.addElement("notations");
		if(noteArrow != null) noteArrow.addToXML(notations);
	}

	public JSONObject toJson()
	{
		JSONObject object = new JSONObject();
		if(noteArrow != null) object.put("note_arrow", noteArrow.toJson());
		return object;
	}
}
