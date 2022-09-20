package com.mikront.fillics.schedule;

import com.mikront.fillics.util.U;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


public class Day implements Iterable<Cell> {
    private static final Pattern REGEX_DATE_WEEKDAY = Pattern.compile("(\\d{2})\\.(\\d{2})\\.(\\d{4}).*");

    private final List<Cell> cells = new ArrayList<>();
    private final LocalDate date;


    public Day(String date) {
        this(LocalDate.parse(REGEX_DATE_WEEKDAY.matcher(date).replaceAll("$3-$2-$1")));
    }

    public Day(LocalDate date) {
        this.date = date;
    }

    public void add(Cell cell) {
        cells.add(cell);
    }


    public LocalDate getDate() {
        return date;
    }

    public boolean isEmpty() {
        return cells.isEmpty();
    }

    public int size() {
        return cells.size();
    }

    @Override
    public String toString() {
        if (isEmpty())
            return "";

        StringBuilder builder = new StringBuilder()
                .append(date);

        for (Cell c : cells)
            U.NL(builder).append(c.toString().indent(2));

        return builder.toString();
    }


    @NotNull
    @Override
    public CellIterator iterator() {
        return new CellIterator();
    }

    private class CellIterator implements Iterator<Cell> {
        private int cursor = 0;


        @Override
        public boolean hasNext() {
            return cursor != size();
        }

        @Override
        public Cell next() {
            return cells.get(cursor++);
        }
    }
}