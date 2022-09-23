package com.mikront.fillics.gui;

import com.mikront.util.Log;

import javax.swing.*;


public class MainForm extends Form {
    private final JPanel container = getContainer();


    public static void main(String[] args) {
        Log.ging(true);
        Log.level(Log.LEVEL_DEBUG);

        Form.load(MainForm.class);
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        Components.applyDefaults(container);
    }
}