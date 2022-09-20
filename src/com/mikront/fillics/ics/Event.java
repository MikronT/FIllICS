package com.mikront.fillics.ics;

import com.mikront.fillics.util.U;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;


public class Event {
    private String title, description;
    private ZonedDateTime from, to;


    private Event() {
    }

    public static Event begin() {
        return new Event();
    }


    public Event setTimeFrom(LocalDateTime from) {
        this.from = U.toGMT(from);
        return this;
    }

    public Event setTimeTo(LocalDateTime to) {
        this.to = U.toGMT(to);
        return this;
    }

    public Event setTitle(String title) {
        this.title = title;
        return this;
    }

    public Event setDescription(String description) {
        this.description = description;
        return this;
    }

    @SuppressWarnings("SpellCheckingInspection")
    protected String compile(String createdAt) {
        StringBuilder builder = new StringBuilder()
                .append("BEGIN:VEVENT");
        U.NL(builder).append("DTSTART:").append(CalendarData.FORMATTER.format(from));
        U.NL(builder).append("DTEND:").append(CalendarData.FORMATTER.format(to));
        U.NL(builder).append("CREATED:").append(createdAt);
        U.NL(builder).append("DESCRIPTION:").append(description.replace(U.NL, "\\n"));
        U.NL(builder).append("LAST-MODIFIED:").append(createdAt);
        U.NL(builder).append("SUMMARY:").append(title);
        U.NL(builder).append("END:VEVENT");

        return builder.toString();
    }
}