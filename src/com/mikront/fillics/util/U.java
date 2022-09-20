package com.mikront.fillics.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;


public class U {
    public static final String NL = System.lineSeparator();


    public static StringBuilder NL(StringBuilder builder) {
        if (!builder.isEmpty())
            builder.append(NL);
        return builder;
    }


    public static ZonedDateTime toGMT(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("GMT")); //Greenwich Mean Time
    }


    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }
}