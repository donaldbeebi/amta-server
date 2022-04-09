package com.donald.abrsmappserver.utils.music;

public enum ChromaticInterval
{
	PER_U   (  0, 0),
	MIN_2   ( -5, 0), MAJ_2   (  2, 0),
	MIN_3   ( -3, 0), MAJ_3   (  4, 0),
	PER_4   ( -1, 0),
	TRITONE (  6, 0),
	PER_5   (  1, 0),
	MIN_6   ( -4, 0), MAJ_6   (  3, 0),
	MIN_7   ( -2, 0), MAJ_7   (  5, 0),
	PER_O   (  0, 1);
	// implement 9ths and stuff

	// each interval is dependent on its letter name displacement and interval type
	//private final int m_Semitones;
	private final int fifths;
	private final int octaves;

	ChromaticInterval(int fifths, int octaves)
	{
		this.fifths = fifths;
		this.octaves = octaves;
	}

	public int fifths()
	{
		return fifths;
	}

	public int letterDisplacement() { return Math.floorMod(fifths * 4, 7) + (octaves * 7); }
	// detecting letter displacements allows for detecting octave change
}
