package com.mikront.fillics.gui;

import com.mikront.util.Log;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class Components {
    private static final Font FONT_DEFAULT = Font.decode("Segoe UI");


    public static void applyDefaults(JComponent component) {
        for (var i : Components.getRecursively(component))
            i.setFont(FONT_DEFAULT);
    }


    public static List<JComponent> getRecursively(JComponent root) {
        List<JComponent> out = new ArrayList<>();

        Log.v("Components::getRecursively: components found = [");
        for (Component c : root.getComponents())
            if (c instanceof JComponent j) {
                Log.v("Components::getRecursively:     " + c.getName());
                out.add(j);
                out.addAll(getRecursively(j));
            }
        Log.v("Components::getRecursively: ]");

        return out;
    }
}