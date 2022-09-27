package com.mikront.fillics.gui;

import com.mikront.util.Log;
import de.orbitalcomputer.JComboBoxAutoCompletion;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class Components {
    public static final String ITEM_UNSET = "";

    private static final Font FONT_DEFAULT = Font.decode("Segoe UI");


    public static JComboBox<String> newOptionalJComboBox() {
        var box = new JComboBox<String>();
        box.setEditable(true);
        JComboBoxAutoCompletion.enable(box);

        box.addMouseWheelListener(e -> MouseWheelScroller.scroll(box, e));

        box.addItem(ITEM_UNSET);
        return box;
    }

    public static JSpinner newJSpinner(XSpinnerDateModel model) {
        var spinner = new JSpinner();
        spinner.setModel(model);
        spinner.addMouseWheelListener(e -> MouseWheelScroller.scroll(spinner, e));
        return spinner;
    }


    public static void applyDefaults(JComponent component) {
        for (var i : Components.getComponentsRecursively(component))
            i.setFont(FONT_DEFAULT);
    }


    public static List<JComponent> getComponentsRecursively(JComponent root) {
        List<JComponent> out = new ArrayList<>();

        Log.v("Components::getComponentsRecursively: components found = [");
        for (Component c : root.getComponents())
            if (c instanceof JComponent j) {
                Log.v("Components::getComponentsRecursively:     " + c.getName());
                out.add(j);
                out.addAll(getComponentsRecursively(j));
            }
        Log.v("Components::getComponentsRecursively: ]");

        return out;
    }
}