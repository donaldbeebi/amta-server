package com.donald.abrsmappserver.utils.music;

import org.dom4j.Element;

public class Time
{
	private final int beats;
	private final int beatType;

	public Time(int beats, int beatType)
	{
		this.beats = beats;
		this.beatType = beatType;
	}

	public void addToXML(Element attributes)
	{
		Element time = attributes.addElement("time");
		time.addElement("beats").addText(String.valueOf(this.beats));
		time.addElement("beat-type").addText(String.valueOf(this.beatType));
	}
}
