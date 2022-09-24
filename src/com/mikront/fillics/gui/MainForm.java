package com.mikront.fillics.gui;

import com.mikront.fillics.schedule.Request;
import com.mikront.util.Log;

import javax.swing.*;
import java.awt.*;
import java.text.Collator;
import java.time.LocalDate;
import java.util.Locale;


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
        frame.setMinimumSize(new Dimension(320, 280));

        var layout = new GroupLayout(container);
        container.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);


        var label_teacher = new JLabel(LABEL_TEACHER);

        Collator collator = Collator.getInstance(new Locale("uk", "UA"));

        var comboBox_teachers = new JComboBox<String>();
        comboBox_teachers.addItem("");
        Request.teachers()
                .stream()
                .filter(s -> !s.contains("!")) //Get rid of fired teachers
                .filter(s -> !s.contains("Вакансія")) //Get rid of vacancies
                .map(s -> s.replace("*", "")) //Get rid of asterisks
                .sorted(collator::compare)
                .forEach(comboBox_teachers::addItem);
        comboBox_teachers.addMouseWheelListener(e -> MouseWheelScroller.scroll(comboBox_teachers, e));

        var label_group = new JLabel(LABEL_GROUP);

        var comboBox_groups = new JComboBox<String>();
        comboBox_groups.addItem("");
        Request.groups()
                .stream()
                .sorted(collator::compare)
                .forEach(comboBox_groups::addItem);
        comboBox_groups.addMouseWheelListener(e -> MouseWheelScroller.scroll(comboBox_groups, e));


        var label_date_from = new JLabel(LABEL_DATE_FROM);

        var spinner_date_from = new JSpinner();
        var model_from = new XSpinnerDateModel();
        spinner_date_from.setModel(model_from);
        spinner_date_from.addMouseWheelListener(e -> MouseWheelScroller.scroll(spinner_date_from, e));

        var label_date_to = new JLabel(LABEL_DATE_TO);

        var spinner_date_to = new JSpinner();
        var model_to = new XSpinnerDateModel();
        spinner_date_to.setModel(model_to);
        spinner_date_to.addMouseWheelListener(e -> MouseWheelScroller.scroll(spinner_date_to, e));

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


        var button_request = new JButton(BUTTON_REQUEST);


        /*
         * Layout sketch
         *
         * V\H   ||||||          |||||
         *
         * |     ---
         * |     000000000000000000000000000
         * |     ---
         * |     000000000000000000000000000
         * ||    ---             ---
         * ||    00000000000     00000000000
         * |                          000000
         */
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(label_teacher)
                .addComponent(comboBox_teachers)
                .addComponent(label_group)
                .addComponent(comboBox_groups)
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(label_date_from)
                                .addComponent(spinner_date_from))
                        .addGroup(layout.createParallelGroup()
                                .addComponent(label_date_to)
                                .addComponent(spinner_date_to)))
                .addComponent(button_request, GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(label_teacher)
                .addComponent(comboBox_teachers)
                .addGap(GAP)
                .addComponent(label_group)
                .addComponent(comboBox_groups)
                .addGap(GAP)
                .addGroup(layout.createParallelGroup()
                        .addComponent(label_date_from)
                        .addComponent(label_date_to))
                .addGroup(layout.createParallelGroup()
                        .addComponent(spinner_date_from)
                        .addComponent(spinner_date_to))
                .addGap(GAP)
                .addComponent(button_request)
        );

        Components.applyDefaults(container);
    }
}