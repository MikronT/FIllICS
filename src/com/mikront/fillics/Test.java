package com.mikront.fillics;

import com.mikront.fillics.schedule.Day;
import com.mikront.fillics.schedule.Parser;
import com.mikront.fillics.schedule.Request;
import com.mikront.util.Log;
import org.jsoup.nodes.Document;

import java.io.File;
import java.util.List;


public class Test {
    private static final File FILE_SCHEDULE = new File("schedule.html");


    public static void main(String[] args) {
        Log.ging(true);
        Log.level(Log.LEVEL_VERBOSE);

        Document document = Request.schedule(null, "ІП-20-3", null, null);
        //Document document = Jsoup.parse(FILE_SCHEDULE, StandardCharsets.UTF_8.name());
        List<Day> days = Parser.of(document)
                .setDefaultGroup(Main.DEFAULT_GROUP)
                .parse();

        Log.d("Test::main: days = [");
        for (Day day : days)
            Log.d("Test::main:     " + day);
        Log.d("Test::main: ]");
    }
}