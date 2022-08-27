package com.donald.abrsmappserver.utils.music;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum Accidental
{
    FLAT_FLAT   (-2, "bb", "bb", "flat-flat"),
    FLAT        (-1, "b",  "b",  "flat"),
    NATURAL     (0,  "",   "n",  "natural"),
    SHARP       (1,  "#",  "s",  "sharp"),
    SHARP_SHARP (2,  "x", "ss", "sharp-sharp");

    private final int alter;
    private final String symbol;
    private final String symbolForImage;
    private final String string;

    Accidental(int semitones, String symbol, String symbolForImage, String string)
    {
        this.alter = semitones;
        this.symbol = symbol;
        this.symbolForImage = symbolForImage;
        this.string = string;
    }

    public int alter()
    {
        return this.alter;
    }

    public String symbol()
    {
        return this.symbol;
    }

    public String symbolForImage()
    {
        return this.symbolForImage;
    }

    public String string()
    {
        return this.string;
    }

    // MAPPING
    private static final Map<String, Accidental> m_Map;
    static
    {
        HashMap<String, Accidental> map = new HashMap<>();
        for(Accidental accidental : values())
        {
            // TODO: UNIFY THEM? SINCE IMAGES ARE NO LONGER STORED ON THE PHONES
            if(accidental == NATURAL) map.put(accidental.symbolForImage(), accidental);
            else map.put(accidental.symbol(), accidental);
        }
        m_Map = Collections.unmodifiableMap(map);
    }

    public static Accidental fromSymbol(String symbol)
    {
        for(Accidental accidental : values())
        {
            // this never returns Accidental.NONE
            if(accidental.symbol.equals(symbol))
            {
                return accidental;
            }
        }
        return null;
    }

    public static Accidental fromAlter(int alter)
    {
        return values()[alter + Music.ALTER_RANGE / 2];
    }
}
