package com.donald.abrsmappserver.utils.music;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Letter
{
    F(5, "F"), C(0, "C"), G(7, "G"), D(2, "D"), A(9, "A"), E(4, "E"), B(11, "B");

    public static final int NO_OF_LETTERS = values().length;

    private final int value;
    private final String string;
    private final String stringForImage;

    Letter(int value, String string)
    {
        this.value = value;
        this.string = string;
        this.stringForImage = string.toLowerCase();
    }

    public int value() { return this.value; }

    public int step() { return Math.floorMod((ordinal() - 1) * DiatonicInterval.FIFTH.letterDisplacement(), NO_OF_LETTERS); }

    public String string() { return this.string; }

    public String stringForImage() { return this.stringForImage; }

    // MAPPING
    private static final Map<String, Letter> STRING_MAP;
    static
    {
        HashMap<String, Letter> map = new HashMap<>();
        for(Letter letterName : values())
        {
            map.put(letterName.string(), letterName);
        }
        STRING_MAP = Collections.unmodifiableMap(map);
    }

    public static Letter fromAbsoluteStep(int absoluteStep)
    {
        return valuesBySteps()[Math.floorMod(absoluteStep, NO_OF_LETTERS)];
    }

    public static Letter fromString(String letterName)
    {
        return STRING_MAP.get(letterName);
    }

    private static final Letter[] LETTERS_BY_STEP = { C, D, E, F, G, A, B };
    // TODO: MAKE IT IMMUTABLE
    public static Letter[] valuesBySteps() { return LETTERS_BY_STEP; }
}
