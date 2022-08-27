package com.donald.abrsmappserver.utils.RandomIntegerGenerator;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;

// TODO: SEPARATE RANDOM FROM CONSTRAINTS
// TODO: ALLOW CONSTRAINTS TO BE CHANGED

public class RandomIntegerGenerator implements Closeable
{
    public interface IntegerExcluder { boolean excludes(int value); }

    private int m_InclusiveLowerBound;
    private int m_ExclusiveUpperBound;
    private final Random m_Random;
    private TreeSet<Integer> m_HardExcludedIntegers;
    private TreeSet<Integer> m_AllExcludedIntegers;

    public RandomIntegerGenerator()
    {
        m_Random = new Random();
    }

    public RandomIntegerGenerator(Random random)
    {
        m_Random = random;
    }

    @Deprecated
    protected RandomIntegerGenerator(int inclusiveLowerBound, int exclusiveUpperBound,
                                     TreeSet<Integer> hardExcludedIntegers,
                                     ArrayList<IntegerExcluder> excluders)
    {
        if(exclusiveUpperBound <= inclusiveLowerBound)
            throw new IllegalStateException("Inclusive lower bound is not lower than exclusive upper bound.");
        m_InclusiveLowerBound = inclusiveLowerBound;
        m_ExclusiveUpperBound = exclusiveUpperBound;
        m_Random = new Random();
        m_HardExcludedIntegers = hardExcludedIntegers;
        for(IntegerExcluder excluder : excluders) excludeIf(excluder, m_HardExcludedIntegers);
        m_AllExcludedIntegers = new TreeSet<>(m_HardExcludedIntegers);
    }

    /*
    public int nextInt(Constraints constraints)
    {
        if((constraints.upperBound() - constraints.lowerBound()) - constraints.numberOfPossibleIntegers() <= 0)
            throw new IllegalStateException("All of the possible integers have been excluded.");

        // 1. generate an integer within the bound of the number of possible values
        // e.g. Possible values: 0, 1, 2, 3, 4; excluded: 2; new possible values: 0, 1, 2, 3
        int randomInteger = m_Random.nextInt(
            constraints.upperBound() - constraints.numberOfExcludedIntegers() - constraints.lowerBound());
        randomInteger += constraints.lowerBound();

        // 2. adjusting the numbers to match the exclusion
        // e.g. generated: 2; after adjustment: 3;
        // very similar to shifting the frame
        // 0, 1, 2, 3 --> 0, 1, _, 3, 4
        //                        +1 +1
        // for every integer excluded
        for(int excludedInteger : constraints.excludedIntegers())
        {
            // if the generated integer is less than the current excluded integer, then it is fine
            if(randomInteger < excludedInteger) { break; }
            // if the generated integer is higher than / equal to the current excluded integer
            // then increment it
            else randomInteger++;
        }
        return randomInteger;
    }

     */

    public int nextInt()
    {
        if((m_ExclusiveUpperBound - m_InclusiveLowerBound) - m_AllExcludedIntegers.size() <= 0)
            throw new IllegalStateException("All of the possible integers have been excluded.");

        // 1. generate an integer within the bound of the number of possible values
        // e.g. Possible values: 0, 1, 2, 3, 4; excluded: 2; new possible values: 0, 1, 2, 3
        int randomInteger = m_Random.nextInt(m_ExclusiveUpperBound - m_AllExcludedIntegers.size() - m_InclusiveLowerBound);
        randomInteger += m_InclusiveLowerBound;

        // 2. adjusting the numbers to match the exclusion
        // e.g. generated: 2; after adjustment: 3;
        // very similar to shifting the frame
        // 0, 1, 2, 3 --> 0, 1, _, 3, 4
        //                        +1 +1
        // for every integer excluded
        for(int excludedInteger : m_AllExcludedIntegers)
        {
            // if the generated integer is less than the current excluded integer, then it is fine
            if(randomInteger < excludedInteger) { break; }
            // if the generated integer is higher than / equal to the current excluded integer
            // then increment it
            else randomInteger++;
        }
        return randomInteger;
    }

    public int nextIntAndExclude() {
        var value = nextInt();
        exclude(value);
        return value;
    }

    private void exclude(int integer, TreeSet<Integer> treeSet)
    {
        if(integer < m_InclusiveLowerBound)
            throw new IllegalArgumentException("Integer to exclude is beyond the lower bound.");

        if(integer >= m_ExclusiveUpperBound)
            throw new IllegalArgumentException("Integer to exclude is beyond the upper bound.");

        treeSet.add(integer);
    }

    private void exclude(int[] integers, TreeSet<Integer> treeSet)
    {
        for(int integer : integers) exclude(integer, treeSet);
    }

    private void excludeIf(IntegerExcluder excluder, TreeSet<Integer> treeSet)
    {
        // iterating through all possible values
        for(int n = m_InclusiveLowerBound; n < m_ExclusiveUpperBound; n++)
        {
            if(excluder.excludes(n)) treeSet.add(n);
        }
    }

    public void exclude(int integer) { exclude(integer, m_AllExcludedIntegers); }

    public void exclude(int[] integers) { exclude(integers, m_AllExcludedIntegers); }

    public void excludeIf(IntegerExcluder excluder) { excludeIf(excluder, m_AllExcludedIntegers); };

    public void clearAllExcludedIntegers()
    {
        m_AllExcludedIntegers.clear();
        m_AllExcludedIntegers.addAll(m_HardExcludedIntegers);
    }

    public int lowerBound() { return m_InclusiveLowerBound; }

    public int upperBound() { return m_ExclusiveUpperBound; }

    public int numberOfPossibleIntegers()
    {
        return m_ExclusiveUpperBound - m_InclusiveLowerBound - m_AllExcludedIntegers.size();
    }

    public TreeSet<Integer> allExcludedIntegers()
    {
        return new TreeSet<>(m_AllExcludedIntegers);
    }

    public int[] allPossibleIntegers()
    {
        int[] integers = new int[numberOfPossibleIntegers()];
        int i = 0;
        int currentInteger = m_InclusiveLowerBound;
        while(currentInteger < m_ExclusiveUpperBound)
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

    @Override
    public void close() {
        clearAllExcludedIntegers();
    }
}