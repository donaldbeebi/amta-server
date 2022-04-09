package com.donald.abrsmappserver.utils.music;

import java.util.ArrayList;
import java.util.ResourceBundle;

public enum Interval
{
    /*
    DIM_U  ( -7, 0),                  PER_U  (  0, 0),                  AUG_U  (  7, 0),
    DIM_2  (-12, 0), MIN_2  ( -5, 0),                  MAJ_2  (  2, 0), AUG_2  (  9, 0),
    DIM_3  (-10, 0), MIN_3  ( -3, 0),                  MAJ_3  (  4, 0), AUG_3  ( 11, 0),
    DIM_4  ( -8, 0),                  PER_4  ( -1, 0),                  AUG_4  (  6, 0),
    DIM_5  ( -6, 0),                  PER_5  (  1, 0),                  AUG_5  (  8, 0),
    DIM_6  (-11, 0), MIN_6  ( -4, 0),                  MAJ_6  (  3, 0), AUG_6  ( 10, 0),
    DIM_7  ( -9, 0), MIN_7  ( -2, 0),                  MAJ_7  (  5, 0), AUG_7  ( 12, 0),
     */

    /*
    DIM_O  ( -7, 1),                  PER_O  (  0, 1),                  AUG_O  (  7, 1),
    DIM_9  (-12, 1), MIN_9  ( -5, 1),                  MAJ_9  (  2, 1), AUG_9  (  9, 1),
    DIM_10 (-10, 1), MIN_10 ( -3, 1),                  MAJ_10 (  4, 1), AUG_10 ( 11, 1),
    DIM_11 ( -8, 1),                  PER_11 ( -1, 1),                  AUG_11 (  6, 1),
    DIM_12 ( -6, 1),                  PER_12 (  1, 1),                  AUG_12 (  8, 1),
    DIM_13 (-11, 1), MIN_13 ( -4, 1),                  MAJ_13 (  3, 1), AUG_13 ( 10, 1),
    DIM_14 ( -9, 1), MIN_14 ( -2, 1),                  MAJ_14 (  5, 1), AUG_14 ( 12, 1);
     */

    /*  0 */ DIM_2, DIM_6, DIM_3, DIM_7, DIM_4, DIM_U, DIM_5,
    /*  7 */ MIN_2, MIN_6, MIN_3, MIN_7, PER_4, PER_U, PER_5,
    /* 14 */ MAJ_2, MAJ_6, MAJ_3, MAJ_7, AUG_4, AUG_U, AUG_5,
    /* 21 */ AUG_2, AUG_6, AUG_3, AUG_7,

    /* 25 */ DIM_9, DIM_13, DIM_10, DIM_14, DIM_11, DIM_O, DIM_12,
    /* 32 */ MIN_9, MIN_13, MIN_10, MIN_14, PER_11, PER_O, PER_12,
    /* 39 */ MAJ_9, MAJ_13, MAJ_10, MAJ_14, AUG_11, AUG_O, AUG_12,
    /* 46 */ AUG_9, AUG_13, AUG_10, AUG_14;

    public static final int[] NUMBER_MAP = { 5, 0, 2, 4, 6, 1, 3 };
    private static final String[] NUMBER_STRING_KEYS =
        {
            "unison_number_string", "2nd_number_string", "3rd_number_string",
            "4th_number_string", "5th_number_string", "6th_number_string",
            "7th_number_string", "octave_number_string", "9th_number_string",
            "10th_number_string", "11th_number_string", "12th_number_string",
            "13th_number_string", "14th_number_string"
        };


    private static final int NO_OF_NUMBERS_SIMPLE = 7;
    private static final int NO_OF_NUMBERS_ALL = 14;
    private static final int NO_OF_SECTIONS = 2;
    private static final int UPPER_BOUND = values().length / NO_OF_SECTIONS - 1;
    private static final int SECTION_LENGTH = values().length / NO_OF_SECTIONS;


    public enum Quality
    {
        DIM("diminished"), MIN("minor"), PER("perfect"), MAJ("major"), AUG("augmented");

        // TODO: FIND A MORE MATHEMATICAL RELATION
        private final String string;
        Quality(String string) { this.string = string; }
        public String string() { return string; }
        public String string(ResourceBundle bundle)
        {
            return bundle.getString(string + "_quality_string");
        }
        public static Quality fromInterval(Interval interval)
        {
            int fifths = Math.abs(interval.fifths());
            boolean negative = interval.fifths() < 0;
            if(fifths <= 1) return PER;
            else if(fifths <= 5) return negative ? MIN : MAJ;
            else if(fifths <= 12) return negative ? DIM : AUG;
            else throw new IllegalArgumentException(
                "No matching quality for interval " + interval.name()
                );
        }
    }

    public static String stringOfNumber(int number, ResourceBundle bundle)
    {
        return bundle.getString(NUMBER_STRING_KEYS[number]);
    }
    // each interval is dependent on its letter name displacement and interval type

    //private final int m_Semitones;
    //private final int fifths;
    //private final int octaves;

    Interval()
    {
        //this.fifths = fifths;
        //this.octaves = octaves;
    }

    public int fifths() { return (ordinal() % (values().length / 2)) - (values().length / 2) / 2; }

    public int octaves() { return ordinal() / (values().length / 2) ; }

    // TODO: NUMBER IS WRONG, IT STARTS AT 0
    @Deprecated
    public int number() { return Math.floorMod(fifths() * 4, 7) + (octaves() * 7); }

    public int numberN() { return (Math.floorMod(fifths() * 4, 7) + (octaves() * 7)) + 1; }

    public int compoundNumber() { return number() % NO_OF_NUMBERS_SIMPLE; }

    public Quality quality() { return Quality.fromInterval(this); }

    public boolean isCompound()
    {
        return octaves() > 0 && number() != 7;
    }

    public String string()
    {
        return quality().string() + " " + NUMBER_STRING_KEYS[number()];
    }

    public String string(ResourceBundle bundle)
    {
        return quality().string(bundle) + " " + bundle.getString(NUMBER_STRING_KEYS[number()]);
    }

    public String compoundString()
    {
        String numberString;
        if(number() == 7) numberString = NUMBER_STRING_KEYS[number()];
        else numberString = NUMBER_STRING_KEYS[number() % 7];
        return (number() > 7 ? "compound " : "") + quality().string() + " " + numberString;
    }

    public String compoundString(ResourceBundle bundle)
    {
        String numberString;
        if(number() == 7) numberString = bundle.getString(NUMBER_STRING_KEYS[number()]);
        else numberString = bundle.getString(NUMBER_STRING_KEYS[number() % 7]);
        return (number() > 7 ? bundle.getString("compound_interval_string") + " " : "")
            + quality().string(bundle) + " " + numberString;
    }

    public Interval[] enharmonic()
    {
        ArrayList<Interval> intervals = new ArrayList<>();
        int currentSection = ordinal() / SECTION_LENGTH;
        int currentRelOrdinal = ordinal() % SECTION_LENGTH;
        while(true)
        {
            if(currentRelOrdinal + 12 <= UPPER_BOUND)
            {
                if(values()[currentRelOrdinal + 12 + currentSection * SECTION_LENGTH].number() >
                    values()[currentRelOrdinal + currentSection * SECTION_LENGTH].number())
                {
                    if(currentSection - 1 >= 0) currentSection--;
                    else break;
                }
                currentRelOrdinal += 12;
            }
            else break;
        }
        while(true)
        {
            intervals.add(values()[currentRelOrdinal + currentSection * SECTION_LENGTH]);
            if(currentRelOrdinal - 12 >= 0)
            {
                if(values()[currentRelOrdinal - 12 + currentSection * SECTION_LENGTH].number() <
                    values()[currentRelOrdinal + currentSection * SECTION_LENGTH].number())
                {
                    if(currentSection + 1 < NO_OF_SECTIONS) currentSection++;
                    else break;
                }
                currentRelOrdinal -= 12;
            }
            else break;
        }
        Interval[] array = new Interval[intervals.size()];
        array = intervals.toArray(array);
        return array;
    }

    public Interval[] enharmonicExc()
    {
        ArrayList<Interval> intervals = new ArrayList<>();
        int currentSection = ordinal() / SECTION_LENGTH;
        int currentRelOrdinal = ordinal() % SECTION_LENGTH;
        while(true)
        {
            if(currentRelOrdinal + 12 <= UPPER_BOUND)
            {
                if(values()[currentRelOrdinal + 12 + currentSection * SECTION_LENGTH].number() >
                    values()[currentRelOrdinal + currentSection * SECTION_LENGTH].number())
                {
                    if(currentSection - 1 >= 0) currentSection--;
                    else break;
                }
                currentRelOrdinal += 12;
            }
            else break;
        }
        while(true)
        {
            if(currentRelOrdinal + currentSection * SECTION_LENGTH != ordinal())
                intervals.add(values()[currentRelOrdinal + currentSection * SECTION_LENGTH]);
            if(currentRelOrdinal - 12 >= 0)
            {
                if(values()[currentRelOrdinal - 12 + currentSection * SECTION_LENGTH].number() <
                    values()[currentRelOrdinal + currentSection * SECTION_LENGTH].number())
                {
                    if(currentSection + 1 < NO_OF_SECTIONS) currentSection++;
                    else break;
                }
                currentRelOrdinal -= 12;
            }
            else break;
        }
        Interval[] array = new Interval[intervals.size()];
        array = intervals.toArray(array);
        return array;
    }

    public static Interval[] intervalsOfNumber(int number)
    {
        if(number < 0 || number > NO_OF_NUMBERS_ALL)
        {
            throw new IllegalArgumentException(number + " is not a valid number");
        }
        int section = number / NO_OF_NUMBERS_SIMPLE;
        int lowestRelOrdinal = NUMBER_MAP[number % NO_OF_NUMBERS_SIMPLE];
        return values()[lowestRelOrdinal + section * SECTION_LENGTH].sameNumber();
    }

    public Interval[] sameNumber()
    {
        int section = ordinal() / SECTION_LENGTH;
        int lowestRelOrdinal = (ordinal() % SECTION_LENGTH) % NO_OF_NUMBERS_SIMPLE;
        int arraySize = (SECTION_LENGTH - lowestRelOrdinal + NO_OF_NUMBERS_SIMPLE - 1) / NO_OF_NUMBERS_SIMPLE;
        Interval[] array = new Interval[arraySize];
        int currentOrdinal = lowestRelOrdinal + section * SECTION_LENGTH;
        for(int i = 0; i < array.length; i++)
        {
            array[i] = values()[currentOrdinal];
            currentOrdinal += NO_OF_NUMBERS_SIMPLE;
        }
        return array;
    }

    public Interval[] sameNumberExc()
    {
        int section = ordinal() / SECTION_LENGTH;
        int lowestRelOrdinal = (ordinal() % SECTION_LENGTH) % NO_OF_NUMBERS_SIMPLE;
        int arraySize = (SECTION_LENGTH - lowestRelOrdinal + NO_OF_NUMBERS_SIMPLE - 1) / NO_OF_NUMBERS_SIMPLE - 1;
        Interval[] array = new Interval[arraySize];
        int currentOrdinal = lowestRelOrdinal + section * SECTION_LENGTH;
        for(int i = 0; i < array.length; i++)
        {
            if(currentOrdinal == this.ordinal()) currentOrdinal += NO_OF_NUMBERS_SIMPLE;
            array[i] = values()[currentOrdinal];
            currentOrdinal += NO_OF_NUMBERS_SIMPLE;
        }
        return array;
    }

    public Interval[] octaveEquivalent()
    {
        int relOrdinal = ordinal() % SECTION_LENGTH;
        Interval[] array = new Interval[NO_OF_SECTIONS];
        for(int i = 0; i < NO_OF_SECTIONS; i++)
        {
            array[i] = values()[relOrdinal + i * SECTION_LENGTH];
        }
        return array;
    }

    public Interval[] octaveEquivalentExc()
    {
        int relOrdinal = ordinal() % SECTION_LENGTH;
        Interval[] array = new Interval[NO_OF_SECTIONS - 1];
        int currentIndex = 0;
        for(int currentSection = 0; currentSection < NO_OF_SECTIONS; currentSection++)
        {
            int currentOrdinal = relOrdinal + currentSection * SECTION_LENGTH;
            if(ordinal() != currentOrdinal)
            {
                array[currentIndex] = values()[currentOrdinal];
                currentIndex++;
            }
        }
        return array;
    }

    public Interval inverse()
    {
        int section = ordinal() / SECTION_LENGTH;
        int relOrdinal = ordinal() % SECTION_LENGTH;

        relOrdinal = -(relOrdinal - PER_U.ordinal()) + PER_U.ordinal();
        return values()[relOrdinal + section * SECTION_LENGTH];
    }

    public static Interval between(AbsolutePitch first, AbsolutePitch second)
    {
         int fifths = Music.relIdFromAbsId(second.id()) - Music.relIdFromAbsId(first.id());
         int octaves = second.octave() - first.octave();
         octaves = Math.min(Math.max(octaves, 0), NO_OF_SECTIONS - 1);
         int ordinal = fifths + 12 + octaves * SECTION_LENGTH;
         return values()[ordinal];
    }
}
