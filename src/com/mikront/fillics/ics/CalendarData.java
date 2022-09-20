package com.mikront.fillics.ics;

import com.mikront.fillics.util.U;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class CalendarData {
    @SuppressWarnings("SpellCheckingInspection")
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private final List<Event> events = new ArrayList<>();


    public void addEvent(Event event) {
        events.add(event);
    }

    @SuppressWarnings("SpellCheckingInspection")
    public String compile() {
        ZonedDateTime now = ZonedDateTime.now();
        String createdAt = FORMATTER.format(now);


        StringBuilder builder = new StringBuilder();
        U.NL(builder).append("BEGIN:VCALENDAR");
        U.NL(builder).append("PRODID:-//Google Inc//Google Calendar 70.9054//EN");
        U.NL(builder).append("VERSION:2.0");
        U.NL(builder).append("CALSCALE:GREGORIAN");
        U.NL(builder).append("METHOD:PUBLISH");

        U.NL(builder).append("BEGIN:VTIMEZONE");
        U.NL(builder).append("TZID:Europe/Kiev");
        U.NL(builder).append("X-LIC-LOCATION:Europe/Kiev");

        U.NL(builder).append("BEGIN:STANDARD");
        U.NL(builder).append("TZOFFSETFROM:+0300");
        U.NL(builder).append("TZOFFSETTO:+0200");
        U.NL(builder).append("TZNAME:EET");
        U.NL(builder).append("DTSTART:19701030T010000");
        U.NL(builder).append("RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1FR");
        U.NL(builder).append("END:STANDARD");

        U.NL(builder).append("BEGIN:DAYLIGHT");
        U.NL(builder).append("TZOFFSETFROM:+0300");
        U.NL(builder).append("TZOFFSETTO:+0300");
        U.NL(builder).append("TZNAME:EEST");
        U.NL(builder).append("DTSTART:19700226T235959");
        U.NL(builder).append("RRULE:FREQ=YEARLY;BYMONTH=2;BYDAY=-1TH");
        U.NL(builder).append("END:DAYLIGHT");

        U.NL(builder).append("END:VTIMEZONE");

        for (Event e : events)
            U.NL(builder).append(e.compile(createdAt));

        U.NL(builder).append("END:VCALENDAR");
        return builder.toString();
    }
}