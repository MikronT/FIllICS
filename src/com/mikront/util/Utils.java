package com.mikront.util;

import java.text.Collator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;


public class Utils {
    public static final Collator COLLATOR = Collator.getInstance(new Locale("uk", "UA"));


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