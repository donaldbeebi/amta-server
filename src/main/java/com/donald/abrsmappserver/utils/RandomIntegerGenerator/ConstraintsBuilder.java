package com.donald.abrsmappserver.utils.RandomIntegerGenerator;

import java.util.ArrayList;
import java.util.TreeSet;

public class ConstraintsBuilder
{
    private Integer m_LowerBound = null; // inclusive
    private Integer m_UpperBound = null; // exclusive
    private final TreeSet<Integer> m_HardExcludedIntegers = new TreeSet<>();
    private final ArrayList<RandomIntegerGenerator.IntegerExcluder> m_IntegerExcluders = new ArrayList<>();

    protected void throwError(String field)
    {
        throw new AssertionError("Field '" + field + "' not initialized.");
    }

    public static ConstraintsBuilder constraints() { return new ConstraintsBuilder(); }

    public ConstraintsBuilder withBounds(int lowerBound, int upperBound)
    {
        if(lowerBound > upperBound)
        {
            m_UpperBound = lowerBound + 1;
            m_LowerBound = upperBound;
        }
        else
        {
            m_UpperBound = upperBound + 1;
            m_LowerBound = lowerBound;
        }
        return this;
    }

    public ConstraintsBuilder withLowerBound(int lowerBound)
    {
        m_LowerBound = lowerBound;
        return this;
    }

    public ConstraintsBuilder withUpperBound(int upperBound)
    {
        m_UpperBound = upperBound;
        return this;
    }

    public ConstraintsBuilder excluding(int excludedInteger)
    {
        m_HardExcludedIntegers.add(excludedInteger);
        return this;
    }

    public ConstraintsBuilder excluding(int... excludedIntegers)
    {
        for(int integer : excludedIntegers) excluding(integer);
        return this;
    }

    public ConstraintsBuilder excludingIf(RandomIntegerGenerator.IntegerExcluder excluder)
    {
        m_IntegerExcluders.add(excluder);
        return this;
    }

    public Constraints build()
    {
        if(m_LowerBound == null) throwError("LowerBound");
        if(m_UpperBound == null) throwError("UpperBound");
        return new Constraints
            (m_LowerBound, m_UpperBound, m_HardExcludedIntegers, m_IntegerExcluders);
    }
}
