package com.mikront.fillics.ics;

import com.mikront.util.Concat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class CalendarData {
    private static final Logger log = LogManager.getLogger();

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("'TZID=Europe/Kiev:'yyyyMMdd'T'HHmmss");

    private final List<Event> events = new ArrayList<>();


    public void addEvent(Event event) {
        events.add(event);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public String compile() {
        String createdAt = FORMATTER.format(LocalDateTime.now());
        log.trace("Compiling CalendarData structure at '{}'", createdAt);

        return Concat.me()
                .lines("BEGIN:VCALENDAR",
                        "PRODID:-//Google Inc//Google Calendar 70.9054//EN",
                        "VERSION:2.0",
                        "CALSCALE:GREGORIAN",
                        "METHOD:PUBLISH",
                        "X-WR-TIMEZONE:Europe/Kiev",

                        "BEGIN:VTIMEZONE",
                        "TZID:Europe/Kiev",
                        "X-LIC-LOCATION:Europe/Kiev",

                        "BEGIN:DAYLIGHT",
                        "TZOFFSETFROM:+0200",
                        "TZOFFSETTO:+0300",
                        "TZNAME:EEST",
                        "DTSTART:19700329T000000",
                        "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU",
                        "END:DAYLIGHT",

                        "BEGIN:STANDARD",
                        "TZOFFSETFROM:+0300",
                        "TZOFFSETTO:+0200",
                        "TZNAME:EET",
                        "DTSTART:19701025T000000",
                        "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU",
                        "END:STANDARD",

                        "END:VTIMEZONE")
                .lines(events, item -> item.compile(createdAt))
                .line("END:VCALENDAR")
                .enate();
    }
}