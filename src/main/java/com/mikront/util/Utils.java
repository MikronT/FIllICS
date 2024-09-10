package com.mikront.util;

import com.mikront.fillics.resource.Translations;
import com.mikront.util.debug.Log;

import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.util.Locale;


public class Utils {
    public static final Collator COLLATOR = Collator.getInstance(Locale.forLanguageTag(Translations.UK));


    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean notEmpty(String s) {
        return s != null && !s.isEmpty();
    }


    public static <T> T getNewInstanceOrThrow(Class<T> instanceClass) {
        try {
            return instanceClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            Log.a("Utils::getNewInstanceOrThrow: unable to create a form instance");
            Log.a("Utils::getNewInstanceOrThrow:   - class = " + instanceClass);
            Log.a("Utils::getNewInstanceOrThrow:   = catching: ", e);
            throw new RuntimeException(e);
        }
    }
}