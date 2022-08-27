package com.donald.abrsmappserver.utils.music;

import java.util.ResourceBundle;

public enum TechnicalName
{
	TONIC("tonic"),
	SUPERTONIC("supertonic"),
	MEDIANT("mediant"),
	SUBDOMINANT("subdominant"),
	DOMINANT("dominant"),
	SUBMEDIANT("submediant"),
	SUBTONIC("subtonic"),
	LEADING_TONE("leading_tone");

	private final String string;

	TechnicalName(String string)
	{
		this.string = string;
	}

	public static TechnicalName get(Mode mode, int degree)
	{
		// todo: not correct when pitch is altered manually
		// todo: degree 1 - 7, not 0 - 6
		if(degree < 0 || degree > 6)
			throw new IllegalArgumentException(degree + " is not a valid degree.");

		if(degree == 6 && mode != Mode.NatMinor)
		{
			return values()[degree + 1];
		}
		else
		{
			return values()[degree];
		}
	}

	public String string()
	{
		return string;
	}

	public String string(ResourceBundle bundle)
	{
		return bundle.getString(string + "_tech_name_string");
	}
}
