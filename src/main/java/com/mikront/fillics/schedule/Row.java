package com.mikront.fillics.schedule;

import com.mikront.util.Concat;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.util.*;


public class Row implements Iterable<Session> {
    public static final int DURATION_DEFAULT_PAIR = 80;
    private static final HashMap<Integer, LocalTime> TIMETABLE_DEFAULT = new HashMap<>(Map.of(
            1, LocalTime.of(8, 0),
            2, LocalTime.of(9, 30),
            3, LocalTime.of(11, 0),
            4, LocalTime.of(12, 50),
            5, LocalTime.of(14, 20),
            6, LocalTime.of(15, 50),
            7, LocalTime.of(17, 20),
            8, LocalTime.of(18, 50)));
    private static final String DELIM = " | ";

    private final int number;

    private final List<Session> sessions = new ArrayList<>();


    public Row(int number) {
        this.number = number;
    }


    public void add(Session session) {
        sessions.add(session);
    }


    public LocalTime getTime() {
        return TIMETABLE_DEFAULT.get(number);
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
                .word(time).word('-').word(time.plusMinutes(DURATION_DEFAULT_PAIR))
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