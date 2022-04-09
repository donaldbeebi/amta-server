package com.donald.abrsmappserver.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArrayShuffler
{
    private ArrayShuffler() {}

    public static <T> int[] shuffle(T[] array)
    {
        int swapIndex = array.length - 1;
        Random random = new Random();

        int[] dispositions = new int[array.length];
        // an array of indices to keep track of the original positions of the elements
        int[] indices = new int[array.length];
        for(int i = 0; i < indices.length; i++) indices[i] = i;

        while(swapIndex != 0)
        {
            int selectIndex = random.nextInt(swapIndex + 1); //inclusive
            T tempElement = array[swapIndex];
            array[swapIndex] = array[selectIndex];
            array[selectIndex] = tempElement;

            // how far the randomly selected element for swapping got moved
            int selectedElementDisposition = swapIndex - selectIndex;
            dispositions[indices[selectIndex]] += selectedElementDisposition;

            // since it is a swap, the swapping element is moved the opposite direction
            // for the same amount of distance
            dispositions[indices[swapIndex]] -= selectedElementDisposition;

            // updating the array of indices
            int tempIndex = indices[swapIndex];
            indices[swapIndex] = indices[selectIndex];
            indices[selectIndex] = tempIndex;

            swapIndex--;
        }

        return dispositions;
    }

    // implement dispositions
    @Deprecated
    public static <T> void shuffle(ArrayList<T> array)
    {
        int swapIndex = array.size() - 1;
        Random random = new Random();

        while(swapIndex != 0)
        {
            int randomIndex = random.nextInt(swapIndex + 1); //inclusive
            T temp = array.get(swapIndex);
            array.set(swapIndex, array.get(randomIndex));
            array.set(randomIndex, temp);

            swapIndex--;
        }
    }

    public static <T> int[] shuffle(List<T> array)
    {
        int swapIndex = array.size() - 1;
        Random random = new Random();

        int[] dispositions = new int[array.size()];
        // an array of indices to keep track of the original positions of the elements
        int[] indices = new int[array.size()];
        for(int i = 0; i < indices.length; i++) indices[i] = i;

        while(swapIndex != 0)
        {
            int selectIndex = random.nextInt(swapIndex + 1); //inclusive
            T tempElement = array.get(swapIndex);
            array.set(swapIndex, array.get(selectIndex));
            array.set(selectIndex, tempElement);

            // how far the randomly selected element for swapping got moved
            int selectedElementDisposition = swapIndex - selectIndex;
            dispositions[indices[selectIndex]] += selectedElementDisposition;

            // since it is a swap, the swapping element is moved the opposite direction
            // for the same amount of distance
            dispositions[indices[swapIndex]] -= selectedElementDisposition;

            // updating the array of indices
            int tempIndex = indices[swapIndex];
            indices[swapIndex] = indices[selectIndex];
            indices[selectIndex] = tempIndex;

            swapIndex--;
        }

        return dispositions;
    }
}
