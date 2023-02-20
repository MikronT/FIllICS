package com.mikront.fillics.gui;

import javax.swing.*;
import java.awt.event.MouseWheelEvent;


public class MouseWheelScroller {
    public static void scroll(JComboBox<?> comboBox, MouseWheelEvent event) {
        if (!comboBox.isEnabled())
            return;

        int i = comboBox.getSelectedIndex();
        int rotation = event.getWheelRotation();

        if (rotation < 0 && i == 0) return;
        if (rotation > 0 && i == comboBox.getItemCount() - 1) return;

        comboBox.setSelectedIndex(rotation < 0 ?
                i - 1 :
                i + 1);
    }
}