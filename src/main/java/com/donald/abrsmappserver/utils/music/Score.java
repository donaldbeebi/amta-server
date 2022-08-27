package com.donald.abrsmappserver.utils.music;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Score
{
	public final ArrayList<Part> parts;
	private int currentPart;
	private int currentMeasure;
	private int currentStaff;
	private Measure.Attributes currentAttributes;
	private final int[] keyAlters;
	private final HashMap<Integer, Integer> measureAlters;

	public Score()
	{
		currentStaff = 1;
		parts = new ArrayList<>();
		keyAlters = new int[Letter.NO_OF_LETTERS];
		measureAlters = new HashMap<>();
	}

	public void addPart(Part part)
	{
		parts.add(part);
	}

	public Part newPart(String id)
	{
		Part part = new Part(id);
		parts.add(part);
		return part;
	}

	public Part newPart()
	{
		return newPart("P" + (parts.size() + 1));
	}

	public Part switchPart(int index)
	{
		currentPart = index;
		return parts.get(currentPart);
	}

	public Measure newMeasure()
	{
		return newMeasure(null);
	}

	public Measure newMeasure(Measure.Attributes attributes)
	{
		return newMeasure(attributes, null);
	}

	public Measure newMeasure(Measure.Attributes attributes, Measure.Barline barline)
	{
		Part part = parts.get(currentPart);
		Measure measure;
		if(attributes == null)
		{
			if(part.numberOfMeasures() == 0)
			{
				throw new IllegalStateException("First measure has null attributes.");
			}
			measure = new Measure();
		}
		else
		{
			currentAttributes = attributes;
			setAlterMemory(attributes.key(), keyAlters);
			measure = new Measure();
			measure.setAttributes(attributes);
		}
		measure.setBarline(barline);

		parts.get(currentPart).addMeasure(measure);
		currentMeasure = parts.size() - 1;
		return measure;
	}

	/*
	public void switchMeasure(int index)
	{
		if(index > parts.size() - 1) throw new IllegalArgumentException("Index out of bound.");
		currentMeasure = index;
		Part part = parts.get(currentPart);
		Measure measure = part.getMeasure(currentMeasure);
			currentAttributes = null;
		for(int i = index; i >= 0; i--)
		{
			Measure.Attributes attributes = part.getMeasure(i).attributes();
			if(attributes != null)
			{
				currentAttributes = attributes;
				setAlterMemory(currentAttributes.key(), keyAlters);
				break;
			}
		}
		ArrayList<Note> notes = measure.notes();
		Arrays.fill(measureAlters, 0);
		for(int i = 0; i < notes.size(); i++)
		{
			AbsolutePitch pitch = notes.get(i).pitch();
			measureAlters[pitch.letter().step()] = pitch.alter();
		}
	}

	 */

	/*
	public void setAttributes(Measure.Attributes attributes)
	{
		parts.get(currentPart).getMeasure(currentMeasure).setAttributes(attributes);
		currentAttributes = attributes;
	}

	 */

	/*
	public void setStaff(int staff)
	{
		if(staff > currentAttributes.staves() || staff < 1)
			throw new IllegalArgumentException("Staff index is out of bound.");
		currentStaff = staff;
	}

	 */

	public Note addPitchedNote(AbsolutePitch pitch, int duration, Note.Type type)
	{
		return addPitchedNote(pitch, duration, type, false, true);
	}

	public Note addPitchedNote(AbsolutePitch pitch, int duration, Note.Type type, Accidental accidental)
	{
		return addPitchedNote(pitch, duration, type, false, accidental, true);
	}

	public Note addPitchedNote(AbsolutePitch pitch, int duration, Note.Type type,
							   boolean chord, boolean printObject)
	{
		Integer measureAlter = measureAlters.get(pitch.absStep());
		int signatureAlter = keyAlters[pitch.letter().ordinal()];
		if(!((measureAlter != null && pitch.alter() == measureAlter) ||
			(signatureAlter == pitch.alter() && measureAlter == null)))
		{
			measureAlters.put(pitch.absStep(), pitch.alter());
			return addPitchedNote(
				pitch, duration, type, chord, Accidental.fromAlter(pitch.alter()), printObject
			);
		}
		return addPitchedNote(pitch, duration, type, chord, null, printObject);
	}

	public Note addPitchedNote(AbsolutePitch pitch, int duration, Note.Type type,
							   boolean chord, Accidental accidental, boolean printObject)
	{
		Note note = Note.pitchedNote(
			pitch, duration, type, chord, accidental, currentStaff, printObject
		);
		parts.get(currentPart).measure(currentMeasure).addNote(note);
		return note;
	}

	public Note addRestNote(int duration, Note.Type type)
	{
		Note note =	Note.restNote(duration, type, currentStaff);
		parts.get(currentPart).measure(currentMeasure).addNote(note);
		return note;
	}

	public Document toDocument()
	{
		Document score = DocumentHelper.createDocument();
		Element scorePartwise = score.addElement("score-partwise").addAttribute("version", "4.0");
		for(var part : parts)
		{
			part.addToXML(scorePartwise);
		}
		return score;
	}

	private static void setAlterMemory(Key key, int[] accidentalsMemory)
	{
		int signature = key.fifths();
		Arrays.fill(accidentalsMemory, 0);
		if(signature < 0)
		{
			for(int i = -1; i >= signature; i--)
			{
				accidentalsMemory[Math.floorMod(i, Letter.NO_OF_LETTERS)]--;
			}
		}
		else if(signature > 0)
		{
			for(int i = 0; i < signature; i++)
			{
				accidentalsMemory[Math.floorMod(i, Letter.NO_OF_LETTERS)]++;
			}
		}
	}
}
