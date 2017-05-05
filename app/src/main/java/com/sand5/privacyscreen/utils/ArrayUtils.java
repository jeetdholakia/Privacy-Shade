package com.sand5.privacyscreen.utils;

/**
 * Created by jeetdholakia on 5/3/17.
 */

public class ArrayUtils {

    public static int[] combineIntArrays(int[] a, int[] b) {
        int length = a.length + b.length;
        int[] result = new int[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
