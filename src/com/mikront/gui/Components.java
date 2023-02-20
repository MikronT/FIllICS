package com.mikront.gui;

import com.mikront.util.debug.Log;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class Components {
    private static final Font FONT_DEFAULT = Font.decode("Segoe UI");


    /**
     * List all the components recursively through the component tree
     *
     * @param root component to start recursive search from
     * @return list of components
     */
    public static <T extends JComponent> List<JComponent> getWholeTree(T root) {
        List<JComponent> out = new ArrayList<>();
        for (Component c : root.getComponents())
            if (c instanceof JComponent j) {
                Log.v("Components::getWholeTree: component found = " + j.getUIClassID());
                out.add(j);
                out.addAll(getWholeTree(j));
            }
        return out;
    }


    /**
     * Apply default settings to a bunch of components
     *
     * @param components array of components
     * @param <T>        any JComponent derivative
     */
    public static <T extends JComponent> void applyDefaults(List<T> components) {
        components.forEach(c -> c.setFont(FONT_DEFAULT));
    }
}