package com.mikront.fillics.schedule;

import com.mikront.util.Concat;
import com.mikront.util.Utils;
import com.mikront.util.debug.Log;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.NodeFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class Schedule {
    public static final File CACHE_DIR = new File("cache");
    public static final LocalDate DATE_FROM;
    public static final LocalDate DATE_TO;
    private static final Charset CHARSET = Charset.forName("windows-1251");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String URL = "https://dekanat.nung.edu.ua/cgi-bin/timetable.cgi";


    static {
        var now = LocalDate.now();

        var from = now.withMonth(8).withDayOfMonth(1);
        if (from.isAfter(now))
            from = from.minusYears(1);

        var to = from.plusYears(1);

        DATE_FROM = from;
        DATE_TO = to;
        Log.i("Schedule::static: setting date range as " + from + " to " + to);
    }


    public static List<String> getTeachers() {
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
                .sorted(Utils.COLLATOR)
                .toList();
    }

    public static List<String> getGroups() {
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
                .sorted(Utils.COLLATOR)
                .toList();
    }

    @Nullable
    public static Document getSchedule(String teacher, String group) {
        var connection = Jsoup.connect(URL + "?n=700")
                .data("sdate", DATE_FROM.format(FORMATTER))
                .data("edate", DATE_TO.format(FORMATTER));

        String criteria = null;
        if (Utils.notEmpty(teacher)) {
            connection = connection.data("teacher", teacher);
            criteria = teacher;
        } else if (Utils.notEmpty(group)) {
            connection = connection.data("group", group);
            criteria = group;
        }

        Document doc;
        try {
            doc = connection
                    .postDataCharset(CHARSET.name())
                    .post();
        } catch (IOException e) {
            Log.e("Schedule::getSchedule: unable to get schedule");
            Log.e("Schedule::getSchedule:   = catching: ", e);
            return null;
        }

        //Try saving requested page for future offline use
        var cacheFile = new File(Concat.me()
                .word(CACHE_DIR)
                .word(File.separatorChar)
                .word(DATE_FROM.getYear()).word("-").word(DATE_TO.getYear())
                .word("_")
                .when(Utils.notEmpty(criteria))
                .word(criteria)
                .word("_")
                .then()
                .word(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .word(".html")
                .enate());

        if (!CACHE_DIR.exists() && !CACHE_DIR.mkdirs()) {
            Log.w("Schedule::getSchedule: can't create cache directory");
            Log.w("Schedule::getSchedule:   - CACHE_DIR = " + CACHE_DIR.getAbsolutePath());
        }

        try (var stream = new FileOutputStream(cacheFile)) {
            stream.write(doc
                    .filter((node, depth) ->
                            node.toString().contains("charset") ?
                                    NodeFilter.FilterResult.REMOVE :
                                    NodeFilter.FilterResult.CONTINUE)
                    .outerHtml()
                    .getBytes(CHARSET));
        } catch (IOException e) {
            Log.w("Schedule::getSchedule: unable to save cache file");
            Log.w("Schedule::getSchedule:   - cacheFile = " + cacheFile);
            Log.w("Schedule::getSchedule:   = catching: ", e);
        }

        return doc;
    }
}