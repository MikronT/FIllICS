package com.mikront.fillics;

import com.mikront.fillics.schedule.Day;
import com.mikront.fillics.schedule.Parser;
import com.mikront.util.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class Test {
    private static final File FILE_SCHEDULE = new File("schedule.html");


    public static void main(String[] args) throws IOException {
        Document document = Jsoup.parse(FILE_SCHEDULE, StandardCharsets.UTF_8.name());
        List<Day> days = Parser.of(document)
                .setDefaultGroup(Main.DEFAULT_GROUP)
                .parse();

        for (Day day : days)
            Log.d(day);
    }
}