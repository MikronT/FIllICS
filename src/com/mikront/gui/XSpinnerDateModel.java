package com.mikront.gui;

import javax.swing.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;


public class XSpinnerDateModel extends AbstractSpinnerModel implements Serializable {
    private Comparable<LocalDate> start, end;
    private LocalDate value;


    public XSpinnerDateModel() {
        this(null, null);
    }

    public XSpinnerDateModel(Comparable<LocalDate> start, Comparable<LocalDate> end) {
        this(start, end, LocalDate.now());
    }

    public XSpinnerDateModel(Comparable<LocalDate> start, Comparable<LocalDate> end, LocalDate value) {
        setStart(start);
        setEnd(end);
        setValue(value);
    }


    public void setStart(Comparable<LocalDate> start) {
        if (!Objects.equals(start, this.start)) {
            this.start = start;
            fireStateChanged();
        }
    }

    public void setEnd(Comparable<LocalDate> end) {
        if (!Objects.equals(end, this.end)) {
            this.end = end;
            fireStateChanged();
        }
    }

    @Override
    public void setValue(Object value) {
        if (!(value instanceof LocalDate)) {
            throw new IllegalArgumentException("LocalDate instance expected");
        }
        if (!value.equals(this.value)) {
            this.value = (LocalDate) value;
            fireStateChanged();
        }
    }


    public LocalDate getDate() {
        return value;
    }

    @Override
    public Object getValue() {
        return getDate();
    }

    @Override
    public Object getNextValue() {
        LocalDate next = value.plusDays(1);
        if (end == null)
            return next;
        return end.compareTo(next) >= 0 ? next : null;
    }

    @Override
    public Object getPreviousValue() {
        LocalDate prev = value.minusDays(1);
        if (start == null)
            return prev;
        return start.compareTo(prev) >= 0 ? prev : null;
    }
}