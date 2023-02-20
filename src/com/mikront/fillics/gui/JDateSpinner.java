package com.mikront.fillics.gui;

import javax.swing.*;
import java.awt.*;


public class JDateSpinner extends JSpinner {
    public JDateSpinner(XSpinnerDateModel model) {
        super.setModel(model);

        //Add mouse scrolling
        addMouseWheelListener(event -> {
            if (!isEnabled())
                return;

            model.setValue(event.getWheelRotation() < 0 ?
                    model.getNextValue() :
                    model.getPreviousValue());
        });

        //Fix background color
        getEditor().getComponent(0).setBackground(Color.WHITE);
    }
}