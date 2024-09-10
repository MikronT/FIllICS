package com.mikront.fillics.ics;

import com.mikront.util.Concat;
import org.jetbrains.annotations.Contract;

import java.time.LocalDateTime;


public class Event {
    private String title, description;
    private LocalDateTime from, to;


    private Event() {
    }

    @Contract("-> new")
    public static Event begin() {
        return new Event();
    }


    public Event setTimeFrom(LocalDateTime from) {
        this.from = from;
        return this;
    }

    public Event setTimeTo(LocalDateTime to) {
        this.to = to;
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
                .line("DTSTART;").word(CalendarData.FORMATTER.format(from))
                .line("DTEND;").word(CalendarData.FORMATTER.format(to))
                .line("CREATED;").word(createdAt)
                .line("DESCRIPTION:").word(description.replace(Concat.LINE_SEPARATOR, "\\n"))
                .line("LAST-MODIFIED;").word(createdAt)
                .line("SUMMARY:").word(title)
                .line("END:VEVENT")
                .enate();
    }
}