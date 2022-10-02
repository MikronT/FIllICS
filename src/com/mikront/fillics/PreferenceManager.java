package com.mikront.fillics;

import com.mikront.util.debug.Log;

import java.io.*;
import java.util.Scanner;


public class PreferenceManager {
    private static final File FILE_PREFERENCES = new File("preferences.ini");
    private static final String SEPARATOR = "=";
    private static final String
            KEY_TEACHER = "teacher",
            KEY_GROUP = "group";

    private static final String DEFAULT_TEACHER = "", DEFAULT_GROUP = "";

    private String teacher = DEFAULT_TEACHER, group = DEFAULT_GROUP;


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
}