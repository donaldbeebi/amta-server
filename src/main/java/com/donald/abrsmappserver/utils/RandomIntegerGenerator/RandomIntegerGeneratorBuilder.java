package com.donald.abrsmappserver.utils.RandomIntegerGenerator;

import java.util.ArrayList;
import java.util.TreeSet;

public class RandomIntegerGeneratorBuilder
{
    private Integer m_InclusiveLowerBound = null; // inclusive
    private Integer m_ExclusiveUpperBound = null; // exclusive
    private final TreeSet<Integer> m_HardExcludedIntegers = new TreeSet<>();
    private final ArrayList<RandomIntegerGenerator.IntegerExcluder> m_IntegerExcluders = new ArrayList<>();

    protected void throwError(String field) { throw new AssertionError("Field '" + field + "' not initialized."); }

    public static RandomIntegerGeneratorBuilder generator()
    {
        return new RandomIntegerGeneratorBuilder();
    }

    public RandomIntegerGeneratorBuilder withBounds(int firstInclusiveBound, int secondInclusiveBound)
    {
        if(firstInclusiveBound > secondInclusiveBound)
        {
            m_ExclusiveUpperBound = firstInclusiveBound + 1;
            m_InclusiveLowerBound = secondInclusiveBound;
        }
        else
        {
            m_ExclusiveUpperBound = secondInclusiveBound + 1;
            m_InclusiveLowerBound = firstInclusiveBound;
        }
        return this;
    }

    public RandomIntegerGeneratorBuilder withLowerBound(int inclusiveLowerBound)
    {
        m_InclusiveLowerBound = inclusiveLowerBound;
        return this;
    }

    public RandomIntegerGeneratorBuilder withUpperBound(int inclusiveUpperBound)
    {
        m_ExclusiveUpperBound = inclusiveUpperBound + 1;
        return this;
    }

    public RandomIntegerGeneratorBuilder excluding(int excludedInteger)
    {
        m_HardExcludedIntegers.add(excludedInteger);
        return this;
    }

    public RandomIntegerGeneratorBuilder excluding(int... excludedIntegers)
    {
        for(int integer : excludedIntegers) excluding(integer);
        return this;
    }

    public RandomIntegerGeneratorBuilder excludingIf(RandomIntegerGenerator.IntegerExcluder excluder)
    {
        m_IntegerExcluders.add(excluder);
        return this;
    }

    public RandomIntegerGenerator build()
    {
        if(m_InclusiveLowerBound == null) throwError("LowerBound");
        if(m_ExclusiveUpperBound == null) throwError("UpperBound");
        return new RandomIntegerGenerator
            (m_InclusiveLowerBound, m_ExclusiveUpperBound, m_HardExcludedIntegers, m_IntegerExcluders);
    }
}
