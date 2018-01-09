package com.schneeloch.bostonbusmap_library.util;

/**
 * Created by schneg on 1/8/18.
 */

public class Now {
    public static long fakeTimeMillis;

    public static boolean useFakeTime = false;

    public static long getMillis() {
        if (useFakeTime) {
            return fakeTimeMillis;
        } else {
            return Now.getMillis();
        }
    }
}
