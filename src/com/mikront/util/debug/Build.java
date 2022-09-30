package com.mikront.util.debug;

public class Build {
    private static boolean DEBUG = false;


    public static void debug(boolean newState) {
        DEBUG = newState;
    }

    public static boolean debug() {
        return DEBUG;
    }
}