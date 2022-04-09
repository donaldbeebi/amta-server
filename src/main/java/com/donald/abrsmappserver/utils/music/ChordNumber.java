package com.donald.abrsmappserver.utils.music;

public enum ChordNumber
{
	I("I"), II("II"), IV("IV"), V("V");

	private final String string;

	ChordNumber(String string)
	{
		this.string = string;
	}

	public String string()
	{
		return string;
	}

	public static ChordNumber fromString(String string)
	{
		for(ChordNumber number : values())
		{
			if(number.string.equals(string)) return number;
		}
		return null;
	}
}
