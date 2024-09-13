package com.mikront.fillics.schedule;

import com.mikront.util.Concat;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


public class Day implements Iterable<Row> {
    private static final Pattern REGEX_DATE_WEEKDAY = Pattern.compile("(\\d{2})\\.(\\d{2})\\.(\\d{4}).*");

    private final List<Row> rows = new ArrayList<>();
    private final LocalDate date;


    public Day(String date) {
        this(LocalDate.parse(REGEX_DATE_WEEKDAY.matcher(date).replaceAll("$3-$2-$1")));
    }

    public Day(LocalDate date) {
        this.date = date;
    }

    public void add(Row row) {
        rows.add(row);
    }


    public LocalDate getDate() {
        return date;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public int size() {
        return rows.size();
    }

    @Override
    public String toString() {
        if (isEmpty())
            return "";

        return Concat.me()
                .word(date)
                .lines(rows, item -> item.toString().indent(2))
                .enate();
    }


    @NotNull
    @Override
    public RowIterator iterator() {
        return new RowIterator();
    }

    public class RowIterator implements Iterator<Row> {
        private int cursor = 0;


        @Override
        public boolean hasNext() {
            return cursor != size();
        }

        @Override
        public Row next() {
            return rows.get(cursor++);
        }
    }
}