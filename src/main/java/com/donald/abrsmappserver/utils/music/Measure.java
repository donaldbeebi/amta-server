package com.donald.abrsmappserver.utils.music;

import org.dom4j.Element;

import java.util.ArrayList;

public class Measure
{
	public static class Attributes
	{
		private final int divisions;
		private final Key key;
		private final Time time;
		private final int staves;
		private final Clef[] clefs;

		public Attributes(int divisions, Key key, Time time, int staves, Clef[] clefs)
		{
			this.divisions = divisions;
			this.key = key;
			this.time = time;
			this.staves = staves;
			this.clefs = clefs;
		}

		public int divisions()
		{
			return divisions;
		}

		public Key key()
		{
			return key;
		}

		public Time time()
		{
			return time;
		}

		public int staves()
		{
			return staves;
		}

		public Clef clef(int index)
		{
			return clefs[index];
		}

		public void addToXML(Element measure)
		{
			Element attributes = measure.addElement("attributes");

			attributes.addElement("divisions").addText(String.valueOf(this.divisions));
			key.addToXML(attributes);
			if(time != null) time.addToXML(attributes);
			attributes.addElement("staves").addText(String.valueOf(this.staves));
			for(var clef : clefs)
			{
				clef.addToXml(attributes);
			}
		}
	}

	public static class Barline
	{
		public enum BarStyle
		{
			REGULAR("regular"), LIGHT_HEAVY("light-heavy"), LIGHT_LIGHT("light-light");

			private final String barStyle;

			BarStyle(String barStyle)
			{
				this.barStyle = barStyle;
			}

			public void addToXML(Element barline)
			{
				barline.addElement("bar-style").addText(barStyle);
			}
		}

		private final BarStyle barStyle;

		public Barline(BarStyle barStyle)
		{
			this.barStyle = barStyle;
		}

		public void addToXML(Element measure)
		{
			Element barline = measure.addElement("barline");
			barStyle.addToXML(barline);
		}
	}


	private Attributes attributes;
	private final ArrayList<Note> notes;
	private Barline barline;

	@Deprecated
	public Measure()
	{
		notes = new ArrayList<>();
	}

	public Measure(Attributes attributes, Barline barline)
	{
		this.attributes = attributes;
		this.barline = barline;
		notes = new ArrayList<>();
	}

	public Attributes attributes()
	{
		return attributes;
	}

	public void setAttributes(Attributes attributes)
	{
		this.attributes = attributes;
	}

	public void addNote(Note note)
	{
		notes.add(note);
	}

	public void clearNotes()
	{
		notes.clear();
	}

	public ArrayList<Note> notes()
	{
		return notes;
	}

	public void setBarline(Barline barline)
	{
		this.barline = barline;
	}

	public void addToXML(Element part)
	{
		Element measure = part.addElement("measure");
		if(attributes != null) attributes.addToXML(measure);
		for(var note : notes)
		{
			note.addToXML(measure);
		}
		if(barline != null) barline.addToXML(measure);
	}
}
