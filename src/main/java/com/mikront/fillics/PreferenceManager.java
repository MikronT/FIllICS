package com.mikront.fillics;

import com.mikront.util.debug.Log;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class PreferenceManager {
    private static final File FILE_PREFERENCES = new File("preferences.ini");
    private static final String SEPARATOR = "=";
    private static final String
            KEY_TEACHER = "teacher",
            KEY_GROUP = "group",
            KEY_FILTER_TYPES = "filter_type",
            KEY_FILTER_SUBJECTS = "filter_subject",
            KEY_FILTER_GROUPS = "filter_group";

    private static final String DEFAULT_TEACHER, DEFAULT_GROUP;

    private String teacher = DEFAULT_TEACHER;
    private String group = DEFAULT_GROUP;
    private final Set<String> filter_types = new HashSet<>();
    private final Set<String> filter_subjects = new HashSet<>();
    private final Set<String> filter_groups = new HashSet<>();

    static {
        DEFAULT_TEACHER = DEFAULT_GROUP = "";
    }

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
                case KEY_FILTER_TYPES -> filter_types.add(value);
                case KEY_FILTER_SUBJECTS -> filter_subjects.add(value);
                case KEY_FILTER_GROUPS -> filter_groups.add(value);
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

        if (!filter_types.isEmpty())
            for (String s : filter_types)
                printWriter.println(KEY_FILTER_TYPES + SEPARATOR + s);

        if (!filter_subjects.isEmpty())
            for (String s : filter_subjects)
                printWriter.println(KEY_FILTER_SUBJECTS + SEPARATOR + s);

        if (!filter_groups.isEmpty())
            for (String s : filter_groups)
                printWriter.println(KEY_FILTER_GROUPS + SEPARATOR + s);

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

    public Set<String> getFilterTypes() {
        return filter_types;
    }

    public void addFilterType(String value) {
        filter_types.add(value);
        commit();
    }

    public void removeFilterType(String value) {
        filter_types.remove(value);
        commit();
    }

    public Set<String> getFilterSubjects() {
        return filter_subjects;
    }

    public void addFilterSubject(String value) {
        filter_subjects.add(value);
        commit();
    }

    public void removeFilterSubject(String value) {
        filter_subjects.remove(value);
        commit();
    }

    public Set<String> getFilterGroups() {
        return filter_groups;
    }

    public void addFilterGroup(String value) {
        filter_groups.add(value);
        commit();
    }

    public void removeFilterGroup(String value) {
        filter_groups.remove(value);
        commit();
    }
}