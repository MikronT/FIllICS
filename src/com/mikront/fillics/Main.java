package com.mikront.fillics;

import com.mikront.fillics.ics.CalendarData;
import com.mikront.fillics.schedule.Cell;
import com.mikront.fillics.schedule.Day;
import com.mikront.fillics.schedule.Session;
import com.mikront.fillics.util.Parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main {
    private static final File FILE_ICS = new File("import.ics");
    private static final String DEFAULT_URL = "https://dekanat.nung.edu.ua/cgi-bin/timetable.cgi?n=700&group=4175";
    public static final String DEFAULT_GROUP = "ІП-20-3";
    private static final Map<String, String> REMAP_SUBJECTS = new HashMap<>(Map.of(
            "Менеджмент проєктів програмного забезпечення", "Pj Mgmt",
            "Паралельне програмування", "Intimate Dev",
            "Технологія компонентного програмування для веб", "Web",
            "Моделювання та аналіз програмного забезпечення", "Soft Modelling",
            "Аналіз вимог до програмного забезпечення", "Soft Analysis",
            "Іноземна мова (анг) (за професійним спрямуванням)", "English",
            "Військова підготовка", "Pixels"
    ));
    private static final Map<String, String> REMAP_TYPES = new HashMap<>(Map.of(
            "Лаб", "P",
            "Пр", "P",
            "Л", "L"
    ));


    public static void main(String[] args) throws IOException {
        ChronoLocalDateTime<?> currentTime = ChronoLocalDateTime.from(LocalDateTime.now());
        ChronoLocalDate daysAhead = ChronoLocalDate.from(currentTime).plus(2, ChronoUnit.DAYS);

        Document document = Jsoup.connect(DEFAULT_URL).get();
        List<Day> days = Parser.of(document)
                .setDefaultGroup(DEFAULT_GROUP)
                .parse();

        CalendarData calendar = new CalendarData();
        for (Day day : days) {
            LocalDate date = day.getDate();
            if (date.isAfter(daysAhead)) break;

            System.out.println(day);

            for (Cell cell : day) {
                LocalDateTime time = LocalDateTime.of(date, cell.getTime());
                if (time.isBefore(currentTime)) continue;

                for (Session s : cell) {
                    String subject = s.getSubject();
                    String type = s.getType();

                    if (s.isOptional()) continue; //Skip optional
                    if (subject.contains("німецька")) continue; //Get rid of German
                    if (s.getGroup().contains(DEFAULT_GROUP + ".1")) continue; //Remove subgroup 1 ones

                    if (REMAP_SUBJECTS.containsKey(subject)) s.setSubject(REMAP_SUBJECTS.get(subject));
                    if (REMAP_TYPES.containsKey(type)) s.setType(REMAP_TYPES.get(type));

                    calendar.addEvent(s.toEvent(day, cell));
                }
            }
        }

        try (var stream = new FileOutputStream(FILE_ICS)) {
            stream.write(calendar.compile().getBytes(StandardCharsets.UTF_8));
        }
    }
}