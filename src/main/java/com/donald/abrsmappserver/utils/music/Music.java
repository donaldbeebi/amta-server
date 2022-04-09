package com.donald.abrsmappserver.utils.music;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Music
{
	public static final int STAFF_LOWER_BOUND = -7;
	public static final int STAFF_UPPER_BOUND = 15;
	public static final int ALTER_RANGE = 5;

	public static final int ID_TABLE_ROW_COUNT = 5;
	public static final int FIFTH_TABLE_COLUMN_COUNT = 7;

	public static final int DIATONIC_SCALE_LENGTH = 7;
	public static final int CHROMATIC_SCALE_LENGTH = 12;
	public static final int NO_OF_STEPS_PER_OCT = 7;

	public static final int TRUE_IDS_PER_OCT = 35;
	//public static final int NATURAL_IDS_PER_OCT = 7;
	public static final int PITCHES_PER_OCT = TRUE_IDS_PER_OCT;

	public static final int NAT_SCALE_START_NOTE = 14;

	public static final int LOWEST_REL_ID = 0;
	public static final int HIGHEST_REL_ID = 34;

	public static final int LOWEST_INTERVAL_NUMBER = 0;
	public static final int HIGHEST_INTERVAL_NUMBER = 13;

	public static final String LETTER_PATTERN = "^[A-Z]";
	public static final String ACCIDENTAL_PATTERN = "(?<=^[A-Z])b{1,2}|n|#{1,2}";
	public static final String OCTAVE_PATTERN = "\\d+(?=$|\\s)";

	private Music() {}

	// helper methods
	public static Letter letterFromId(int id)
	{
		// TODO: NO NEED FOR FLOOR MOD?
		return Letter.values()[Math.floorMod(id, Letter.NO_OF_LETTERS)];
	}

	@Deprecated
	public static Accidental accidentalFromId(int id)
	{
		return Accidental.values()[Math.floorMod(id, PITCHES_PER_OCT) / Letter.NO_OF_LETTERS];
	}

	@Deprecated
	public static int trueId(int id)
	{
		if (accidentalFromId(id) == Accidental.NATURAL)
		{
			id -= Letter.NO_OF_LETTERS * 3;
		}
		return id;
	}

	public static int relIdFromAbsId(int id)
	{
		return id % PITCHES_PER_OCT;
	}

	public static int relIdFromLetter(Letter letter, int alter)
	{
		return letter.ordinal() + (alter + ALTER_RANGE / 2) * Letter.NO_OF_LETTERS;
	}

	@Deprecated
	public static int relIdFromLetter(Letter letter, Accidental accidental)
	{
		return letter.ordinal() + accidental.ordinal() * Letter.NO_OF_LETTERS;
	}

	public static int relIdFromString(String noteString)
	{
		Matcher matcher;
		Letter letter = null;
		int alter = 0;

		// 1. parsing letter name
		matcher = Pattern.compile(LETTER_PATTERN).matcher(noteString);
		if(matcher.find()) letter = Letter.fromString(matcher.group(0));
		assert letter != null;

		// 2. parsing accidental
		matcher = Pattern.compile(ACCIDENTAL_PATTERN).matcher(noteString);
		if(matcher.find())
		{
			Accidental accidental = Accidental.fromSymbol(matcher.group(0));
			assert accidental != null;
			alter = accidental.alter();
		}
		return relIdFromLetter(letter, alter);
	}

	public static boolean isValidRelId(int id)
	{
		return id >= LOWEST_REL_ID && id <= HIGHEST_REL_ID;
	}

	public static int octaveFromId(int id)
	{
		return id / PITCHES_PER_OCT;
	}

	public static int absIdFromLetter(Letter letter, int alter, int octave)
	{
		return relIdFromLetter(letter, alter) + octave * PITCHES_PER_OCT;
	}

	@Deprecated
	public static int absIdFromLetter(Letter letter, Accidental accidental, int octave)
	{
		return RelativePitch.relativeIDFromLetter(letter, accidental) + octave * PITCHES_PER_OCT;
	}


	// TODO: PROPERLY REIMPLEMENT THIS
	public static int ordinalFromId(int id)
	{
		return letterFromId(id).step() * ALTER_RANGE +
			alterFromId(id) + ALTER_RANGE / 2 +
			octaveFromId(id) * PITCHES_PER_OCT;
		//return ((((id % 7) * 24) + 18) % 42) + (id % 42) / 7 + (id / 42) * 42;
	}

	public static int idFromOrdinal(int ordinal)
	{
		/*
		return ((ordinal / 6 * 2) + 1) % 7
			+ ordinal % 6 * 7 + (ordinal / 42) * 42;

		 */
		return ((ordinal / ALTER_RANGE * 2) + 1) % Letter.NO_OF_LETTERS +
			ordinal % ALTER_RANGE * Letter.NO_OF_LETTERS +
			(ordinal / PITCHES_PER_OCT) * PITCHES_PER_OCT;
	}

	public static int absoluteIDFromString(String noteString)
	{
		int octave = 0;
		Matcher matcher;
		matcher = Pattern.compile(OCTAVE_PATTERN).matcher(noteString);
		if(matcher.find()) octave = Integer.parseInt(matcher.group(0));

		int relativeID = RelativePitch.relativeIdFromString(noteString);
		return relativeID + PITCHES_PER_OCT * octave;
	}

	public static int translatedOctave(int absoluteID, Interval interval, int factor)
	{
		int octave = octaveFromId(absoluteID);
		int offset = factor >= 0 ? 0 : -6;
		Letter letter = letterFromId(absoluteID);
		octave += (letter.step() + (interval.number() * factor + offset)) /
			Letter.NO_OF_LETTERS;
		return octave;
	}

	@Deprecated
	public static int pitchOrdinalFromAbsStep(int absStep, Accidental accidental)
	{
		return
			Pitch.ordinalFromId(
				Pitch.absoluteIdFromLetter(
					Letter.fromAbsoluteStep(absStep), accidental, absStep / Letter.NO_OF_LETTERS
				)
			);
	}

	public static int pitchOrdinalFromAbsStep(int absStep, int alter)
	{
		return
			ordinalFromId(
				absIdFromLetter(
					Letter.fromAbsoluteStep(absStep),
					alter,
					absStep / Letter.NO_OF_LETTERS
				)
			);
	}

	public static Letter letterFromAbsStep(int absStep)
	{
		return Letter.valuesBySteps()[absStep % Letter.NO_OF_LETTERS];
	}

	public static int octaveFromAbsStep(int absStep)
	{
		return absStep / NO_OF_STEPS_PER_OCT;
	}

	public static int stepFromId(int id)
	{
		return letterFromId(id).step() + octaveFromId(id) * NO_OF_STEPS_PER_OCT;
	}

	public static int alterFromId(int id)
	{
		return Math.floorMod(id, PITCHES_PER_OCT) / Letter.NO_OF_LETTERS - ALTER_RANGE / 2;
	}

	public static int alterOfLetterInKey(Key key, Letter letter)
	{
		int fifths = key.fifths();
		if(fifths > 0)
		{
			return
				(fifths - letter.ordinal() + NO_OF_STEPS_PER_OCT - 1)
					/ NO_OF_STEPS_PER_OCT;
		}
		else
		{
			return
				(fifths - letter.ordinal()) / NO_OF_STEPS_PER_OCT;
		}
	}

	public static int idFromChord(ChordNumber number, ChordInversion inversion)
	{
		return number.ordinal() * ChordInversion.values().length + inversion.ordinal();
	}

	public static ChordNumber numberFromId(int id)
	{
		return ChordNumber.values()[id / ChordInversion.values().length];
	}

	public static ChordInversion inversionFromId(int id)
	{
		return ChordInversion.values()[id % ChordInversion.values().length];
	}
}
