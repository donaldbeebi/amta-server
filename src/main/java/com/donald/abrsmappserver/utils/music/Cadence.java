package com.donald.abrsmappserver.utils.music;

import java.util.ResourceBundle;

public enum Cadence
{
	PERFECT("perfect"),
	PLAGAL("plagal"),
	IMPERFECT("imperfect");

	private final String string;

	Cadence(String string)
	{
		this.string = string;
	}

	public String string(ResourceBundle bundle)
	{
		return bundle.getString(string + "_cadence_string");
	}

	@Deprecated
	public String stringKey()
	{
		return string + "_cadence_string";
	}

	public static Cadence fromString(String string)
	{
		for(Cadence cadence : values())
		{
			if(cadence.string.equals(string)) return cadence;
		}
		return null;
	}
}
