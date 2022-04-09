package com.donald.abrsmappserver.utils.music;

public enum ChordInversion
{
	A("a"), B("b"), C("c");

	private final String string;

	ChordInversion(String string)
	{
		this.string = string;
	}

	public String string()
	{
		return string;
	}

	public static ChordInversion fromString(String string)
	{
		for(ChordInversion inversion : values())
		{
			if(inversion.string.equals(string)) return inversion;
		}
		return null;
	}
}
