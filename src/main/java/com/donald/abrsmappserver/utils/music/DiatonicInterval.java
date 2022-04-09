package com.donald.abrsmappserver.utils.music;

public enum DiatonicInterval
{
    UNI     ( 0, 0),
    SECOND  ( 2, 0),
    THIRD   ( 4, 0),
    FOURTH  (-1, 0),
    FIFTH   ( 1, 0),
    SIXTH   ( 3, 0),
    SEVENTH ( 5, 0),
    OCT     ( 0, 1);
    // implement 9ths and stuff

    // each interval is dependent on its letter name displacement and interval type

    //private final int m_Semitones;
    private final int fifths;
    private final int octaves;


    DiatonicInterval(int fifths, int octaves)
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
