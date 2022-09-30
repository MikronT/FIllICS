package com.mikront.util.debug;


@SuppressWarnings("unused")
public class Log {
    public static final int
            LEVEL_VERBOSE = 2, LEVEL_DEBUG = 3,
            LEVEL_INFO = 4, LEVEL_WARN = 5, LEVEL_ERROR = 6,
            LEVEL_ASSERT = 7, LEVEL_WTF = 8;

    private static boolean LOGGING = false;
    private static int LEVEL = LEVEL_INFO;


    public static void v(Object msg, Object... args) {
        println(LEVEL_VERBOSE, msg, args);
    }
    public static void d(Object msg, Object... args) {
        println(LEVEL_DEBUG, msg, args);
    }
    public static void i(Object msg, Object... args) {
        println(LEVEL_INFO, msg, args);
    }
    public static void w(Object msg, Object... args) {
        println(LEVEL_WARN, msg, args);
    }
    public static <T extends Exception> void w(Object msg, T e, Object... args) {
        w(msg + e.getMessage(), args);
    }
    public static void e(Object msg, Object... args) {
        println(LEVEL_ERROR, msg, args);
    }
    public static <T extends Exception> void e(Object msg, T e, Object... args) {
        e(msg + e.getMessage(), args);
    }
    public static void a(Object msg, Object... args) {
        println(LEVEL_ASSERT, msg, args);
    }
    public static void wtf(Object msg, Object... args) {
        println(LEVEL_WTF, msg, args);
    }

    private static void println(int level, Object msg, Object... args) {
        if (!LOGGING || level < LEVEL)
            return;

        System.out.printf(msg.toString() + System.lineSeparator(), args);
    }


    public static void ging(boolean newState) {
        LOGGING = newState;
    }

    public static boolean ging() {
        return LOGGING;
    }

    public static void level(int newState) {
        if (LEVEL_VERBOSE <= newState && newState <= LEVEL_WTF)
            LEVEL = newState;
    }

    public static int level() {
        return LEVEL;
    }
}