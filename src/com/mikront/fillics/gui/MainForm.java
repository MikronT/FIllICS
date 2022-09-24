package com.mikront.fillics.gui;

import com.mikront.fillics.schedule.Request;
import com.mikront.util.Log;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;


public class MainForm extends Form {
    private final JPanel container = getContainer();


    public static void main(String[] args) {
        Log.ging(true);
        Log.level(Log.LEVEL_DEBUG);

        Form.load(MainForm.class);
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        var frame = getFrame();
        frame.setMinimumSize(new Dimension(300, 240));

        var layout = new GroupLayout(container);
        container.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);


        var label_teacher = new JLabel(LABEL_TEACHER);

        var comboBox_teachers = new JComboBox<String>();
        Request.teachers().forEach(comboBox_teachers::addItem);

        var label_group = new JLabel(LABEL_GROUP);

        var comboBox_groups = new JComboBox<String>();
        Request.groups().forEach(comboBox_groups::addItem);


        var label_date_from = new JLabel(LABEL_DATE_FROM);

        var spinner_date_from = new JSpinner();
        var model_from = new XSpinnerDateModel();
        spinner_date_from.setModel(model_from);

        var label_date_to = new JLabel(LABEL_DATE_TO);

        var spinner_date_to = new JSpinner();
        var model_to = new XSpinnerDateModel();
        spinner_date_to.setModel(model_to);

        spinner_date_from.addChangeListener(e -> {
            LocalDate
                    date1 = model_from.getDate(),
                    date2 = model_to.getDate();
            if (date1.compareTo(date2) > 0)
                model_to.setValue(date1);
        });
        spinner_date_to.addChangeListener(e -> {
            LocalDate
                    date1 = model_from.getDate(),
                    date2 = model_to.getDate();
            if (date1.compareTo(date2) > 0)
                model_from.setValue(date2);
        });
        spinner_date_from.addMouseWheelListener(e ->
                model_from.setValue(e.getWheelRotation() < 0 ?
                        model_from.getNextValue() :
                        model_from.getPreviousValue()));
        spinner_date_to.addMouseWheelListener(e ->
                model_to.setValue(e.getWheelRotation() < 0 ?
                        model_to.getNextValue() :
                        model_to.getPreviousValue()));


        var button_request = new JButton(BUTTON_REQUEST);


        Components.applyDefaults(container);
    }
}