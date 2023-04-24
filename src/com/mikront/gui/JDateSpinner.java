package com.mikront.gui;

import javax.swing.*;
import java.awt.*;


public class JDateSpinner extends JSpinner {
    public JDateSpinner(XSpinnerDateModel model) {
        super.setModel(model);

        //Add mouse scrolling
        addMouseWheelListener(event -> {
            if (!isEnabled())
                return;

            var newDate = event.getWheelRotation() < 0 ?
                    model.getNextValue() :
                    model.getPreviousValue();

            if (newDate != null)
                model.setValue(newDate);
        });

        //Fix background color
        getEditor().getComponent(0).setBackground(Color.WHITE);
    }
}