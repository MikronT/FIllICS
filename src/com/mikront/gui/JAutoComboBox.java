package com.mikront.gui;

import de.orbitalcomputer.JComboBoxAutoCompletion;

import javax.swing.*;


public class JAutoComboBox extends JComboBox<String> {
    public static final String ITEM_UNSET = "";


    public JAutoComboBox() {
        //Enable editing features
        setEditable(true);
        JComboBoxAutoCompletion.enable(this);

        //Add mouse scrolling
        addMouseWheelListener(event -> {
            if (!isEnabled())
                return;

            int i = getSelectedIndex();
            int rotation = event.getWheelRotation();

            if (rotation < 0 && i == 0) return;
            if (rotation > 0 && i == getItemCount() - 1) return;

            setSelectedIndex(rotation < 0 ?
                    i - 1 :
                    i + 1);
        });

        //Add empty item as optional
        addItem(ITEM_UNSET);
    }
}