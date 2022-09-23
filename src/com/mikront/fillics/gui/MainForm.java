package com.mikront.fillics.gui;

import com.mikront.util.Log;


public class MainForm extends Form {
    public static void main(String[] args) {
        Log.ging(true);
        Log.level(Log.LEVEL_DEBUG);

        Form.load(MainForm.class);
    }
}