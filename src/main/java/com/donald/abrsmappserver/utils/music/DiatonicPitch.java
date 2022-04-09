package com.donald.abrsmappserver.utils.music;

public class DiatonicPitch extends AbsolutePitch
{
	private final Key key;

	public DiatonicPitch(Key key, int degree, int octave)
	{
		super(relIdFromNthNote(key.id(), degree) + octave * Music.PITCHES_PER_OCT);
		this.key = key;
	}

	public DiatonicPitch(Key key, Letter letter, int octave)
	{
		super(relIdFromLetter(key.id(), letter) + octave * Music.PITCHES_PER_OCT);
		this.key = key;
	}

	public DiatonicPitch(DiatonicPitch that)
	{
		super(that);
		this.key = that.key;
	}

	public Key key()
	{
		return key;
	}

	public int degree()
	{
		Letter tonicLetter = Pitch.letterFromId(Key.tonicPitchIdFromKeyId(key.id()));
		return Math.floorMod(letter().step() - tonicLetter.step(), Letter.NO_OF_LETTERS);
	}

	public void translateUp(DiatonicInterval interval)
	{
		int rId = relativeIdFromAbsoluteId(id);
		rId = (rId - Key.startPitchIdFromKeyId(key.id()) + interval.fifths()) % Consts.PITCHES_PER_SCALE +
			key.startPitch().id();
		int octave = DiatonicPitch.translatedOctave(id, interval, 1);
		id = rId + Music.PITCHES_PER_OCT * octave;
		id = alterForMode(key.id(), id);
	}

	public void translateDown(DiatonicInterval interval)
	{
		int rId = relativeIdFromAbsoluteId(id);
		rId = Math.floorMod(rId -  Key.startPitchIdFromKeyId(key.id()) - interval.fifths(), Consts.PITCHES_PER_SCALE) +
			key.startPitch().id();
		int octave = DiatonicPitch.translatedOctave(id, interval, -1);
		id = rId + Music.PITCHES_PER_OCT * octave;
		id = alterForMode(key.id(), id);
	}

	public TechnicalName technicalName()
	{
		return TechnicalName.get(key.mode(), degree());
	}

	protected static int alterForMode(int keyId, int noteId)
	{
		Mode mode = Key.modeFromId(keyId);
		int tonicNoteId = Key.tonicPitchIdFromKeyId(keyId);
		Letter tonicNoteLetter = Music.letterFromId(tonicNoteId);
		Letter noteLetter = Music.letterFromId(noteId);
		int degree = Math.floorMod(noteLetter.step() - tonicNoteLetter.step(), Letter.NO_OF_LETTERS);
		if(mode == Mode.H_MINOR && degree == 6)
		{
			// TODO: CHECK IF RELATIVE ID IS VALID
			noteId += Letter.NO_OF_LETTERS;
		}
		else if(mode == Mode.M_MINOR && (degree == 5 || degree == 6))
		{
			noteId += Letter.NO_OF_LETTERS;
		}
		return noteId;
	}

	// TODO: MOVE TO PARENT CLASS?
	protected static int relIdFromNthNote(int keyId, int nthNote)
	{
		int tonicNoteId = Key.tonicPitchIdFromKeyId(keyId);
		Letter tonicLetter = letterFromId(tonicNoteId);
		Letter letter =
			Letter.valuesBySteps()[Math.floorMod(nthNote + tonicLetter.step(), Letter.NO_OF_LETTERS)];
		return relIdFromLetter(keyId, letter);
	}

	protected static int relIdFromLetter(int keyId, Letter letter)
	{
		int startNoteId = Key.startPitchIdFromKeyId(keyId);
		return Math.floorMod(letter.ordinal() - startNoteId, Letter.NO_OF_LETTERS) +
			startNoteId;
	}

	protected static int translatedOctave(int absoluteId, DiatonicInterval interval, int factor)
	{
		int octave = octaveFromId(absoluteId);
		int offset = factor >= 0 ? 0 : -6;
		Letter letter = letterFromId(absoluteId);
		octave += (letter.step() + (interval.letterDisplacement() * factor + offset)) /
			Letter.NO_OF_LETTERS;
		return octave;
	}
}
