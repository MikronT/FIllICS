package com.mikront.util;

import java.text.Collator;
import java.util.Locale;


public class Utils {
    public static final Collator COLLATOR = Collator.getInstance(new Locale("uk", "UA"));


    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }
}