package com.donald.abrsmappserver.utils.music;

import com.google.common.collect.ImmutableList;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public enum Mode
{
    HarMinor("harmonic minor", "minor_mode_string", "h_minor_mode_string"),
    NatMinor("minor", "minor_mode_string", "n_minor_mode_string"),
    MelMinor("melodic minor", "minor_mode_string", "m_minor_mode_string"),
    Major("major", "major_mode_string", "major_mode_string");

    @Deprecated
    public static final int NO_OF_MODES = 4;
    public static final int NO_OF_PITCHES_PER_SCALE = 7;
    public static final ImmutableList<Mode> modes = ImmutableList.copyOf(values());

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
            if(mode == Mode.NatMinor) map.put("natural minor", mode);
            map.put(mode.string(), mode);
        }
        m_Map = Collections.unmodifiableMap(map);
    }

    public static Mode fromString(String scaleString)
    {
        return m_Map.get(scaleString);
    }
}
