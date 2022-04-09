package com.donald.abrsmappserver.utils.music;

import org.dom4j.Element;

import java.util.ArrayList;

public class Part
{
	private final String id;
	private final ArrayList<Measure> measures;

	public Part(String id)
	{
		this.id = id;
		measures = new ArrayList<>();
	}

	public void addMeasure(Measure measure)
	{
		measures.add(measure);
	}

	public Measure newMeasure()
	{
		Measure measure = new Measure();
		measures.add(measure);
		return measure;
	}

	public Measure measure(int index)
	{
		return measures.get(index);
	}

	public int numberOfMeasures()
	{
		return measures.size();
	}

	public void addToXML(Element scorePartwise)
	{
		Element part = scorePartwise.addElement("part");
		part.addAttribute("id", String.valueOf(id));
		for(var measure : measures)
		{
			measure.addToXML(part);
		}
	}
}
