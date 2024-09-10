package com.mikront.util;

import com.mikront.fillics.resource.Translations;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.util.Locale;


public class Utils {
    private static final Logger log = LogManager.getLogger();

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
            log.fatal("Unable to create a form instance from class '{}'", instanceClass, e);
            throw new RuntimeException(e);
        }
    }
}