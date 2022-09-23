package com.mikront.fillics.schedule;

import com.mikront.util.Log;
import com.mikront.util.Utils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class Request {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String URL = "https://dekanat.nung.edu.ua/cgi-bin/timetable.cgi";


    public static List<String> teachers() {
        List<String> out = new ArrayList<>();

        Document doc;
        try {
            doc = Jsoup.connect(URL + "?n=701&lev=141").get();
        } catch (IOException e) {
            Log.e("Request::teachers: unable to get teachers list");
            Log.e("Request::teachers:   = catching: ", e);
            return out;
        }

        JSONObject root = new JSONObject(doc.wholeText());
        JSONArray array = root.getJSONArray("suggestions");
        array.forEach(o -> out.add(o.toString()));

        return out;
    }

    public static List<String> groups() {
        List<String> out = new ArrayList<>();

        Document doc;
        try {
            doc = Jsoup.connect(URL + "?n=701&lev=142").get();
        } catch (IOException e) {
            Log.e("Request::groups: unable to get groups list");
            Log.e("Request::groups:   = catching: ", e);
            return out;
        }

        JSONObject root = new JSONObject(doc.wholeText());
        JSONArray array = root.getJSONArray("suggestions");
        array.forEach(o -> out.add(o.toString()));

        return out;
    }

    @Nullable
    public static Document schedule(String teacher, String group, LocalDate from, LocalDate to) {
        var connection = Jsoup.connect(URL + "?n=700");

        if (Utils.notEmpty(teacher))
            connection = connection.data("teacher", teacher);
        if (Utils.notEmpty(group))
            connection = connection.data("group", group);
        if (from != null)
            connection = connection.data("sdate", from.format(formatter));
        if (to != null)
            connection = connection.data("edate", to.format(formatter));

        try {
            return connection
                    .postDataCharset("windows-1251")
                    .post();
        } catch (IOException e) {
            Log.e("Request::schedule: unable to get schedule");
            Log.e("Request::schedule:   = catching: ", e);
        }
        return null;
    }
}