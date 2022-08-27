package com.donald.abrsmappserver.utils.music;

import com.google.common.collect.ImmutableList;
import org.dom4j.Element;
import org.json.JSONObject;

import java.util.*;

public class Clef
{
	public enum Sign
	{
		F("F", Letter.F.step() + 3 * Letter.NO_OF_LETTERS),
		C("C", Letter.C.step() + 4 * Letter.NO_OF_LETTERS),
		G("G", Letter.G.step() + 4 * Letter.NO_OF_LETTERS);

		private final String string;
		private final int refAbsoluteStep;

		Sign(String string, int refAbsoluteStep)
		{
			this.string = string;
			this.refAbsoluteStep = refAbsoluteStep;
		}

		public String string() { return string; }
		public int refAbsStep() { return refAbsoluteStep; }
	}

	public enum Type
	{
		Bass  ("bass", Sign.F, 4),
		Tenor ("tenor", Sign.C, 4),
		Alto  ("alto", Sign.C, 3),
		Treble("treble", Sign.G, 2);

		private static final Map<String, Type> stringMap;
		static
		{
			HashMap<String, Type> map = new HashMap<>();
			for(Type clef : values())
			{
				map.put(clef.string(), clef);
			}
			stringMap = Collections.unmodifiableMap(map);
		}

		public static final ImmutableList<Type> types;
		static {
			types = ImmutableList.<Type>builder()
				.add(Bass)
				.add(Tenor)
				.add(Alto)
				.add(Treble)
				.build();
		}

		private final String string;
		private final Sign sign;
		private final int line;
		private boolean printObject;

		Type(String string, Sign sign, int line)
		{
			this.string = string;
			this.sign = sign;
			this.line = line;
			printObject = true;
		}

		public void setPrintObject(boolean printObject)
		{
			this.printObject = printObject;
		}

		public Sign sign()
		{
			return this.sign;
		}

		public int line()
		{
			return this.line;
		}

		public String string()
		{
			return string;
		}

		public String string(ResourceBundle bundle)
		{
			return bundle.getString(string + "_clef_string");
		}

		public String stringKey()
		{
			return string + "_clef_string";
		}

		public int baseAbsStep()
		{
			return sign.refAbsStep() - (line - 1) * 2;
		}

		public static Type fromString(String clefString)
		{
			return stringMap.get(clefString);
		}

		public void addToXML(Element clef)
		{
			clef.addElement("sign").addText(this.sign.string());
			clef.addElement("line").addText(String.valueOf(this.line));
		}
	}

	private final Type type;
	private boolean printObject;

	public Clef(Type type)
	{
		this.type = type;
		printObject = true;
	}

	public void setPrintObject(boolean printObject)
	{
		this.printObject = printObject;
	}

	public void addToXml(Element attributes)
	{
		Element clef = attributes.addElement("clef")
			.addAttribute("print-object", printObject ? "yes" : "no");
		type.addToXML(clef);
	}

	public JSONObject toJson()
	{
		JSONObject object = new JSONObject();
		object.put("sign", type.sign().string());
		object.put("line", type.line());
		object.put("print_object", printObject);
		return object;
	}
}
