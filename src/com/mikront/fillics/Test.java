package com.mikront.fillics;

import com.mikront.fillics.schedule.Day;
import com.mikront.fillics.schedule.Parser;
import com.mikront.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class Test {
    private static final File FILE_SCHEDULE = new File("schedule.html");


    public static void main(String[] args) throws IOException {
        Log.ging(true);
        Log.level(Log.LEVEL_VERBOSE);

        //Document document = Jsoup.parse(FILE_SCHEDULE, StandardCharsets.UTF_8.name());
        Document document = Jsoup.connect("https://dekanat.nung.edu.ua/cgi-bin/timetable.cgi?n=700")
                //.data("teacher", "")
                .data("group", "ІП-20-3")
                //.data("sdate", "01.08.2022")
                //.data("edate", "01.08.2023")
                .postDataCharset("windows-1251")
                .post();
        List<Day> days = Parser.of(document)
                .setDefaultGroup(Main.DEFAULT_GROUP)
                .parse();

        Log.d("Test::main: days = [");
        for (Day day : days)
            Log.d("Test::main:     " + day);
        Log.d("Test::main: ]");
    }
}