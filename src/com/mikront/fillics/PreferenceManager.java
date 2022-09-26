package com.mikront.fillics;

import com.mikront.util.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class PreferenceManager {
    private static final File FILE_PREFERENCES = new File("preferences.ini");
    private static final String SEPARATOR = "=";
    private static final String
            KEY_TEACHER = "teacher",
            KEY_GROUP = "group",
            KEY_EXCLUDE = "exclude",
            KEY_INCLUDE_OPTIONAL = "include_optional";

    private static final boolean DEFAULT_SHOULD_INCLUDE_OPTIONAL = true;
    private static final String DEFAULT_TEACHER = "", DEFAULT_GROUP = "";
    private static final List<String> DEFAULT_EXCLUSIONS = new ArrayList<>();

    private boolean shouldIncludeOptional = DEFAULT_SHOULD_INCLUDE_OPTIONAL;
    private String teacher = DEFAULT_TEACHER, group = DEFAULT_GROUP;
    private final List<String> exclusions = DEFAULT_EXCLUSIONS;


    public PreferenceManager() {
        read();
    }


    private void read() {
        FileInputStream stream;
        try {
            stream = new FileInputStream(FILE_PREFERENCES);
        } catch (FileNotFoundException e) {
            Log.e("PreferenceManager::read: unable to open the input steam");
            Log.e("PreferenceManager::read:   = catching: ", e);
            return;
        }

        var scanner = new Scanner(stream);
        while (scanner.hasNextLine()) {
            var line = scanner.nextLine().split(SEPARATOR);
            if (line.length != 2)
                continue;

            var key = line[0];
            var value = line[1];
            switch (key) {
                case KEY_TEACHER -> teacher = value;
                case KEY_GROUP -> group = value;
                case KEY_EXCLUDE -> exclusions.add(value);
                case KEY_INCLUDE_OPTIONAL -> shouldIncludeOptional = Boolean.parseBoolean(value);
            }
        }

        try {
            stream.close();
        } catch (IOException e) {
            Log.e("PreferenceManager::read: unable to close the input stream");
            Log.e("PreferenceManager::read:   = catching: ", e);
        }
    }

    private void commit() {
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(FILE_PREFERENCES, false);
        } catch (FileNotFoundException e) {
            Log.e("PreferenceManager::commit: unable to open file output stream");
            Log.e("PreferenceManager::commit:   - file = " + FILE_PREFERENCES);
            Log.e("PreferenceManager::commit:   = catching: ", e);
            return;
        }

        var printWriter = new PrintWriter(stream);

        if (!DEFAULT_TEACHER.equals(teacher))
            printWriter.println(KEY_TEACHER + SEPARATOR + teacher);

        if (!DEFAULT_GROUP.equals(group))
            printWriter.println(KEY_GROUP + SEPARATOR + group);

        if (!DEFAULT_EXCLUSIONS.equals(exclusions))
            exclusions.forEach(s -> printWriter.println(KEY_EXCLUDE + SEPARATOR + s));

        if (DEFAULT_SHOULD_INCLUDE_OPTIONAL != shouldIncludeOptional)
            printWriter.println(KEY_INCLUDE_OPTIONAL + SEPARATOR + shouldIncludeOptional);

        printWriter.close();
    }


    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String value) {
        teacher = value;
        commit();
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String value) {
        group = value;
        commit();
    }

    public List<String> getExclusions() {
        return exclusions;
    }

    public void setExclusions(List<String> list) {
        exclusions.clear();
        exclusions.addAll(list);
        commit();
    }

    public boolean getShouldIncludeOptional() {
        return shouldIncludeOptional;
    }

    public void setShouldIncludeOptional(boolean value) {
        shouldIncludeOptional = value;
        commit();
    }
}