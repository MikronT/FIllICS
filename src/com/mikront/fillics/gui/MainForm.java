package com.mikront.fillics.gui;

import com.mikront.fillics.schedule.Request;
import com.mikront.util.Log;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;


public class MainForm extends Form {
    private final JPanel container = getContainer();
    private final List<String> teachers = Request.teachers();
    private final List<String> groups = Request.groups();


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
        var label_group = new JLabel(LABEL_GROUP);
        var label_from = new JLabel(LABEL_DATE_FROM);
        var label_to = new JLabel(LABEL_DATE_TO);

        var combo_teachers = newJComboBox(teachers);
        var combo_groups = newJComboBox(groups);

        var model_from = new XSpinnerDateModel();
        var spinner_from = newJSpinner(model_from);

        var model_to = new XSpinnerDateModel();
        var spinner_to = newJSpinner(model_to);

        spinner_from.addChangeListener(e -> {
            LocalDate
                    date1 = model_from.getDate(),
                    date2 = model_to.getDate();
            if (date1.compareTo(date2) > 0)
                model_to.setValue(date1);
        });
        spinner_to.addChangeListener(e -> {
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
                .addComponent(combo_teachers)
                .addComponent(label_group)
                .addComponent(combo_groups)
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup()
                                .addComponent(label_from)
                                .addComponent(spinner_from))
                        .addGroup(layout.createParallelGroup()
                                .addComponent(label_to)
                                .addComponent(spinner_to)))
                .addComponent(button_request, GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(label_teacher)
                .addComponent(combo_teachers)
                .addGap(GAP)
                .addComponent(label_group)
                .addComponent(combo_groups)
                .addGap(GAP)
                .addGroup(layout.createParallelGroup()
                        .addComponent(label_from)
                        .addComponent(label_to))
                .addGroup(layout.createParallelGroup()
                        .addComponent(spinner_from)
                        .addComponent(spinner_to))
                .addGap(GAP)
                .addComponent(button_request)
        );

        Components.applyDefaults(container);
    }


    private static JComboBox<String> newJComboBox(List<String> list) {
        var box = new JComboBox<String>();
        box.addMouseWheelListener(e -> MouseWheelScroller.scroll(box, e));

        box.addItem("");
        list.forEach(box::addItem);
        return box;
    }

    private static JSpinner newJSpinner(XSpinnerDateModel model) {
        var spinner = new JSpinner();
        spinner.setModel(model);
        spinner.addMouseWheelListener(e -> MouseWheelScroller.scroll(spinner, e));
        return spinner;
    }
}