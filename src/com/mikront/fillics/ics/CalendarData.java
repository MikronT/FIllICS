package com.mikront.fillics.ics;

import com.mikront.util.Concat;

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

        return Concat.me()
                .lines("BEGIN:VCALENDAR",
                        "PRODID:-//Google Inc//Google Calendar 70.9054//EN",
                        "VERSION:2.0",
                        "CALSCALE:GREGORIAN",
                        "METHOD:PUBLISH",

                        "BEGIN:VTIMEZONE",
                        "TZID:Europe/Kiev",
                        "X-LIC-LOCATION:Europe/Kiev",

                        "BEGIN:STANDARD",
                        "TZOFFSETFROM:+0300",
                        "TZOFFSETTO:+0200",
                        "TZNAME:EET",
                        "DTSTART:19701030T010000",
                        "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1FR",
                        "END:STANDARD",

                        "BEGIN:DAYLIGHT",
                        "TZOFFSETFROM:+0300",
                        "TZOFFSETTO:+0300",
                        "TZNAME:EEST",
                        "DTSTART:19700226T235959",
                        "RRULE:FREQ=YEARLY;BYMONTH=2;BYDAY=-1TH",
                        "END:DAYLIGHT",

                        "END:VTIMEZONE")
                .lines(events, item -> item.compile(createdAt))
                .line("END:VCALENDAR")
                .enate();
    }
}