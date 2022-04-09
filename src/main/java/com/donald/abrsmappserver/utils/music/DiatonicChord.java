package com.donald.abrsmappserver.utils.music;

public class DiatonicChord
{
	private final int id;

	public DiatonicChord(String string)
	{
		String numberString = string.substring(0, string.length() - 1);
		String inversionString = string.substring(string.length() - 1);

		ChordNumber number = ChordNumber.fromString(numberString);
		if(number == null)
		{
			throw new IllegalArgumentException(
				"Number string " + numberString + " not recognized."
			);
		}

		ChordInversion inversion = ChordInversion.fromString(inversionString);
		if(inversion == null)
		{
			throw new IllegalArgumentException(
				"Inversion string " + inversionString + " not recognized."
			);
		}

		id = Music.idFromChord(number, inversion);
	}

	public DiatonicChord(ChordNumber number, ChordInversion inversion)
	{
		id = Music.idFromChord(number, inversion);
	}

	public DiatonicChord(int id)
	{
		this.id = id;
	}

	public int id()
	{
		return id;
	}

	public ChordNumber number()
	{
		return Music.numberFromId(id);
	}

	public ChordInversion inversion()
	{
		return Music.inversionFromId(id);
	}

	public String string()
	{
		return number().string() + inversion().string();
	}
}
