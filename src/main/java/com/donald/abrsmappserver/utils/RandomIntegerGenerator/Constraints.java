package com.donald.abrsmappserver.utils.RandomIntegerGenerator;

import java.util.ArrayList;
import java.util.TreeSet;

public class Constraints
{
    private final int m_LowerBound;
    private final int m_UpperBound;
    private final TreeSet<Integer> m_HardExcludedIntegers;
    private final TreeSet<Integer> m_AllExcludedIntegers;
    // TODO: NO NEED TO CACHE ALL EXCLUDED INTEGERS FROM EXCLUDER

    protected Constraints(int lowerBound, int upperBound,
                          TreeSet<Integer> hardExcludedIntegers,
                          ArrayList<RandomIntegerGenerator.IntegerExcluder> excluders)
    {
        if(upperBound <= lowerBound)
            throw new IllegalStateException("Inclusive lower bound is not lower than exclusive upper bound.");
        m_LowerBound = lowerBound;
        m_UpperBound = upperBound;
        m_HardExcludedIntegers = hardExcludedIntegers;
        for(RandomIntegerGenerator.IntegerExcluder excluder : excluders) excludeIf(excluder, m_HardExcludedIntegers);
        m_AllExcludedIntegers = new TreeSet<>(m_HardExcludedIntegers);
    }

    private void exclude(int integer, TreeSet<Integer> treeSet)
    {
        if(integer < m_LowerBound)
            throw new IllegalArgumentException("Integer to exclude is beyond the lower bound.");

        if(integer >= m_UpperBound)
            throw new IllegalArgumentException("Integer to exclude is beyond the upper bound.");

        treeSet.add(integer);
    }

    private void exclude(int[] integers, TreeSet<Integer> treeSet)
    {
        for(int integer : integers) exclude(integer, treeSet);
    }

    private void excludeIf(RandomIntegerGenerator.IntegerExcluder excluder, TreeSet<Integer> treeSet)
    {
        // iterating through all possible values
        for(int n = m_LowerBound; n < m_UpperBound; n++)
        {
            if(excluder.excludes(n)) treeSet.add(n);
        }
    }

    public void exclude(int integer) { exclude(integer, m_AllExcludedIntegers); }

    public void exclude(int[] integers) { exclude(integers, m_AllExcludedIntegers); }

    public void excludeIf(RandomIntegerGenerator.IntegerExcluder excluder) { excludeIf(excluder, m_AllExcludedIntegers); };

    public void clearAllExcludedIntegers()
    {
        m_AllExcludedIntegers.clear();
        m_AllExcludedIntegers.addAll(m_HardExcludedIntegers);
    }

    public int lowerBound() { return m_LowerBound; }

    public int upperBound() { return m_UpperBound; }

    public int numberOfExcludedIntegers()
    {
        return m_AllExcludedIntegers.size();
    }

    public TreeSet<Integer> excludedIntegers()
    {
        return new TreeSet<>(m_AllExcludedIntegers);
    }

    public int numberOfPossibleIntegers()
    {
        return m_UpperBound - m_LowerBound - m_AllExcludedIntegers.size();
    }

    public int[] possibleIntegers()
    {
        int[] integers = new int[numberOfPossibleIntegers()];
        int i = 0;
        int currentInteger = m_LowerBound;
        while(currentInteger < m_UpperBound)
        {
            if(!m_AllExcludedIntegers.contains(currentInteger))
            {
                integers[i] = currentInteger;
                i++;
            }
            currentInteger++;
        }
        return integers;
    }
}
