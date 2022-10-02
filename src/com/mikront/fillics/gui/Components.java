package com.mikront.fillics.gui;

import com.mikront.util.debug.Log;
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
        spinner.getEditor().getComponent(0).setBackground(Color.WHITE);
        return spinner;
    }


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