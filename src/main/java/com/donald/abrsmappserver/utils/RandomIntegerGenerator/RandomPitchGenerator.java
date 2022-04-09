package com.donald.abrsmappserver.utils.RandomIntegerGenerator;

import com.donald.abrsmappserver.utils.music.*;

import java.util.Random;

public class RandomPitchGenerator
{
	private final Random random;
	private int STAFF_LOWER_BOUND;
	private int STAFF_UPPER_BOUND;

	public RandomPitchGenerator()
	{
		random = new Random();
	}

	@Deprecated
	public RandomPitchGenerator(int staffLowerBound, int staffUpperBound)
	{
		random = new Random();
		STAFF_LOWER_BOUND = staffLowerBound;
		STAFF_UPPER_BOUND = staffUpperBound;
	}

	public void setStaffBounds(int staffLowerBound, int staffUpperBound)
	{
		STAFF_LOWER_BOUND = staffLowerBound;
		STAFF_UPPER_BOUND = staffUpperBound;
	}

	private int calculatePitchOrdinalLowerBound(Clef.Type clefType)
	{
		int lowestAbsoluteStep = clefType.baseAbsStep() + STAFF_LOWER_BOUND;
		return Music.pitchOrdinalFromAbsStep(lowestAbsoluteStep, -Music.ALTER_RANGE / 2);
	}

	private int calculatePitchOrdinalUpperBound(Clef.Type clefType)
	{
		int highestAbsoluteStep = clefType.baseAbsStep() + STAFF_UPPER_BOUND;
		return Music.pitchOrdinalFromAbsStep(highestAbsoluteStep, Music.ALTER_RANGE / 2);
	}

	public FreePitch nextPitch(Clef.Type clefType)
	{
		int lowerBound = calculatePitchOrdinalLowerBound(clefType);
		int upperBound = calculatePitchOrdinalUpperBound(clefType);
		int ordinal = random.nextInt(upperBound - lowerBound + 1) + lowerBound;
		return new FreePitch(Music.idFromOrdinal(ordinal));
	}

	public DiatonicPitch nextPitch(Clef.Type clefType, Key key)
	{
		int lowerBound = clefType.baseAbsStep() + STAFF_LOWER_BOUND;
		int upperBound = clefType.baseAbsStep() + STAFF_UPPER_BOUND;
		int absStep = random.nextInt(upperBound - lowerBound + 1) + lowerBound;
		return new DiatonicPitch(
			key,
			Music.letterFromAbsStep(absStep),
			Music.octaveFromAbsStep(absStep)
		);
	}
}
