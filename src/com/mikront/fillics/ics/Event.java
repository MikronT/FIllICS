package com.mikront.fillics.ics;

import com.mikront.fillics.util.U;
import com.mikront.util.Concat;

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
        return Concat.me()
                .line("BEGIN:VEVENT")
                .line("DTSTART:").word(CalendarData.FORMATTER.format(from))
                .line("DTEND:").word(CalendarData.FORMATTER.format(to))
                .line("CREATED:").word(createdAt)
                .line("DESCRIPTION:").word(description.replace(Concat.LINE_SEPARATOR, "\\n"))
                .line("LAST-MODIFIED:").word(createdAt)
                .line("SUMMARY:").word(title)
                .line("END:VEVENT")
                .enate();
    }
}