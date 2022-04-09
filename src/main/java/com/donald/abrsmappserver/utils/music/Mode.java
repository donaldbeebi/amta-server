package com.donald.abrsmappserver.utils.music;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public enum Mode
{
    H_MINOR("harmonic minor", "minor_mode_string", "h_minor_mode_string"),
    N_MINOR("minor", "minor_mode_string", "n_minor_mode_string"),
    M_MINOR("melodic minor", "minor_mode_string", "m_minor_mode_string"),
    MAJOR("major", "major_mode_string", "major_mode_string");

    public static final int NO_OF_MODES = 4;
    public static final int NO_OF_PITCHES_PER_SCALE = 7;

    private final String string;
    private final String simpleStringKey;
    private final String stringKey;

    Mode(String string, String simpleStringKey, String stringKey)
    {
        this.string = string;
        this.simpleStringKey = simpleStringKey;
        this.stringKey = stringKey;
    }

    public static int getScale(int key)
    {
        Key.throwErrorIfInvalidID(key);
        return key % 4;
    }

    public String string()
    {
        return string;
    }

    public String simpleString(ResourceBundle bundle) {
        return bundle.getString(simpleStringKey);
    }

    public String string(ResourceBundle bundle)
    {
        return bundle.getString(stringKey);
    }

    // MAPPING
    private static final Map<String, Mode> m_Map;
    static
    {
        HashMap<String, Mode> map = new HashMap<>();
        for(Mode mode : values())
        {
            if(mode == Mode.N_MINOR) map.put("natural minor", mode);
            map.put(mode.string(), mode);
        }
        m_Map = Collections.unmodifiableMap(map);
    }

    public static Mode fromString(String scaleString)
    {
        return m_Map.get(scaleString);
    }
}
