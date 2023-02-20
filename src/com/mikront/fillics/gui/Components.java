package com.mikront.fillics.gui;

import com.mikront.util.debug.Log;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class Components {
    private static final Font FONT_DEFAULT = Font.decode("Segoe UI");


    public static void applyDefaults(JComponent component) {
        for (var i : Components.getComponentsRecursively(component))
            i.setFont(FONT_DEFAULT);
    }


    public static List<JComponent> getComponentsRecursively(JComponent root) {
        List<JComponent> out = new ArrayList<>();
        for (Component c : root.getComponents())
            if (c instanceof JComponent j) {
                Log.v("Components::getComponentsRecursively: component found = " + j.getUIClassID());
                out.add(j);
                out.addAll(getComponentsRecursively(j));
            }
        return out;
    }
}