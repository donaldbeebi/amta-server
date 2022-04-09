package com.donald.abrsmappserver.utils;

public class EnglishNumberConverter
{
    private static final String[] NUMBERS =
    { "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten" };

    private EnglishNumberConverter() {}

    public static String convert(int number)
    {
        if(number < 0 || number > 10)
            throw new IllegalArgumentException("Number must be within 1-10.");

        return NUMBERS[number];
    }
}
