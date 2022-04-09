package com.donald.abrsmappserver.utils;

public class SnakeCaseConverter
{
    public static String convert(String string)
    {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < string.length(); i++)
        {
            if(Character.isAlphabetic(string.charAt(i)))
                builder.append(Character.toLowerCase(string.charAt(i)));
            else if(string.charAt(i) == ' ')
                builder.append('_');
            //else throw new IllegalArgumentException("Illegal character detected");
            else builder.append(string.charAt(i));
        }

        return builder.toString();
    }
}
