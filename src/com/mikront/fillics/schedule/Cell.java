package com.mikront.fillics.schedule;

import com.mikront.util.Concat;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.util.*;


public class Cell implements Iterable<Session> {
    public static final int
            DURATION_DEFAULT_MINUTES = 80,
            DURATION_FULL_HOURS = 12;
    private static final String DELIM = " | ";
    private static final HashMap<Integer, LocalTime> TIMETABLE = new HashMap<>(Map.of(
            0, LocalTime.of(9, 0),
            1, LocalTime.of(9, 0),
            2, LocalTime.of(10, 30),
            3, LocalTime.of(12, 0),
            4, LocalTime.of(13, 40),
            5, LocalTime.of(15, 10),
            6, LocalTime.of(16, 40),
            7, LocalTime.of(18, 10),
            8, LocalTime.of(19, 40)));

    private final int number;

    private final List<Session> sessions = new ArrayList<>();


    public Cell(int number) {
        this.number = number;
    }


    public void add(Session session) {
        sessions.add(session);
    }


    public LocalTime getTime() {
        return TIMETABLE.get(number);
    }

    public boolean isEmpty() {
        return sessions.isEmpty();
    }

    public int size() {
        return sessions.size();
    }

    @Override
    public String toString() {
        if (isEmpty())
            return "";

        LocalTime time = getTime();

        return Concat.me()
                .word(number).word(DELIM)
                .word(time).word('-').word(time.plusMinutes(DURATION_DEFAULT_MINUTES))
                .lines(sessions, item -> item.toString().indent(2))
                .enate();
    }


    @NotNull
    @Override
    public Iterator<Session> iterator() {
        return new SessionIterator();
    }

    private class SessionIterator implements Iterator<Session> {
        private int cursor = 0;


        @Override
        public boolean hasNext() {
            return cursor != size();
        }

        @Override
        public Session next() {
            return sessions.get(cursor++);
        }
    }
}