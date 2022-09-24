package com.mikront.fillics.gui;

import javax.swing.*;
import java.awt.event.MouseWheelEvent;


public class MouseWheelScroller {
    public static void scroll(JComboBox<?> comboBox, MouseWheelEvent event) {
        int i = comboBox.getSelectedIndex();
        int rotation = event.getWheelRotation();

        if (rotation < 0 && i == 0) return;
        if (rotation > 0 && i == comboBox.getItemCount() - 1) return;

        comboBox.setSelectedIndex(rotation < 0 ?
                i - 1 :
                i + 1);
    }

    public static void scroll(JSpinner spinner, MouseWheelEvent event) {
        var model = spinner.getModel();

        model.setValue(event.getWheelRotation() < 0 ?
                model.getNextValue() :
                model.getPreviousValue());
    }
}