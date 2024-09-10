package com.mikront.gui;

import com.mikront.fillics.resource.Strings;
import com.mikront.fillics.resource.Translations;

import java.util.Locale;


public class Context {
    private String language;


    public Context() {
        setLanguage(Locale.getDefault());
    }

    public void setLanguage(Locale locale) {
        language = locale.getLanguage();
    }


    public String getString(Strings id) {
        return Translations.getTranslation(language, id);
    }
}