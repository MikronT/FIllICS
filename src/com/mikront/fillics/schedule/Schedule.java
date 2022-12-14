package com.mikront.fillics.schedule;

import com.mikront.util.Utils;
import com.mikront.util.debug.Build;
import com.mikront.util.debug.Log;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Schedule {
    private static final List<String> DEBUG_TEACHERS = Collections.emptyList();
    private static final List<String> DEBUG_GROUPS = Collections.singletonList("ІП-20-3");
    private static final File DEBUG_SCHEDULE = new File("schedule.html");

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String URL = "https://dekanat.nung.edu.ua/cgi-bin/timetable.cgi";


    public static List<String> getTeachers() {
        if (Build.debug())
            return DEBUG_TEACHERS;

        List<String> out = new ArrayList<>();

        Document doc;
        try {
            doc = Jsoup.connect(URL + "?n=701&lev=141").get();
        } catch (IOException e) {
            Log.e("Schedule::getTeachers: unable to get teachers list");
            Log.e("Schedule::getTeachers:   = catching: ", e);
            return out;
        }

        JSONObject root = new JSONObject(doc.wholeText());
        return root.getJSONArray("suggestions")
                .toList()
                .stream()
                .map(Object::toString)
                .filter(s -> !s.contains("!")) //Get rid of fired teachers
                .filter(s -> !s.contains("Вакансія")) //Get rid of vacancies
                .map(s -> s.replace("*", "")) //Get rid of asterisks
                .sorted(Utils.COLLATOR::compare)
                .toList();
    }

    public static List<String> getGroups() {
        if (Build.debug())
            return DEBUG_GROUPS;

        List<String> out = new ArrayList<>();

        Document doc;
        try {
            doc = Jsoup.connect(URL + "?n=701&lev=142").get();
        } catch (IOException e) {
            Log.e("Schedule::getGroups: unable to get groups list");
            Log.e("Schedule::getGroups:   = catching: ", e);
            return out;
        }

        JSONObject root = new JSONObject(doc.wholeText());
        return root.getJSONArray("suggestions")
                .toList()
                .stream()
                .map(Object::toString)
                .sorted(Utils.COLLATOR::compare)
                .toList();
    }

    @Nullable
    public static Document getSchedule(String teacher, String group, LocalDate from, LocalDate to) {
        if (Build.debug())
            try {
                return Jsoup.parse(DEBUG_SCHEDULE, StandardCharsets.UTF_8.name());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        var connection = Jsoup.connect(URL + "?n=700");

        if (Utils.notEmpty(teacher))
            connection = connection.data("teacher", teacher);
        if (Utils.notEmpty(group))
            connection = connection.data("group", group);
        if (from != null)
            connection = connection.data("sdate", from.format(FORMATTER));
        if (to != null)
            connection = connection.data("edate", to.format(FORMATTER));

        try {
            return connection
                    .postDataCharset("windows-1251")
                    .post();
        } catch (IOException e) {
            Log.e("Schedule::getSchedule: unable to get schedule");
            Log.e("Schedule::getSchedule:   = catching: ", e);
        }
        return null;
    }
}