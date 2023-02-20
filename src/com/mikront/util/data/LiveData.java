package com.mikront.util.data;

import java.util.ArrayList;
import java.util.List;


/**
 * Keeps your data synchronized across the program and notifies listeners about the changes
 *
 * @param <T> your data type
 */
public class LiveData<T> {
    private T value;
    private final List<OnDataUpdateListener<T>> listeners = new ArrayList<>();


    public T get() {
        return value;
    }


    public interface OnDataUpdateListener<T> {
        void onUpdate(T value);
    }

    public void observe(OnDataUpdateListener<T> listener) {
        listeners.add(listener);
    }


    public void push(T newValue) {
        value = newValue;
        listeners.forEach(l -> l.onUpdate(get()));
    }
}