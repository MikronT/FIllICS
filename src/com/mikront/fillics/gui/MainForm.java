package com.mikront.fillics.gui;

import com.mikront.fillics.ics.CalendarData;
import com.mikront.fillics.schedule.*;
import com.mikront.util.Log;
import com.mikront.util.Utils;
import de.orbitalcomputer.JComboBoxAutoCompletion;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;


public class MainForm extends Form {
    private static final File FILE_ICS = new File("import.ics");
    private static final String ITEM_UNSET = "";
    private static final int PROGRESS_MAX = 100;

    private JComboBox<String> combo_groups, combo_teachers;
    private JProgressBar progressBar;
    private XSpinnerDateModel model_from, model_to;
    private final JPanel container = getContainer();
    private final ActionListener buttonClickListener = e -> {
        var teacher = (String) combo_teachers.getSelectedItem();
        var group = (String) combo_groups.getSelectedItem();

        if (Utils.isEmpty(teacher) && Utils.isEmpty(group))
            return;

        Document doc = Request.schedule(
                teacher,
                group,
                model_from.getDate(),
                model_to.getDate());

        List<Day> days = Parser.of(doc)
                .setDefaultGroup(group)
                .parse();

        CalendarData data = new CalendarData();
        for (Day day : days)
            for (Cell cell : day)
                for (Session session : cell)
                    data.addEvent(session.toEvent(day, cell));

        try (var stream = new FileOutputStream(FILE_ICS)) {
            stream.write(data.compile().getBytes(StandardCharsets.UTF_8));
        } catch (IOException ex) {
            Log.e("MainForm: buttonClickListener-> unable to write file");
            Log.e("MainForm: buttonClickListener->   - file = " + FILE_ICS);
            Log.e("MainForm: buttonClickListener->   = catching: ", e);
        }
    };


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

        combo_teachers = newJComboBox();
        combo_groups = newJComboBox();

        combo_teachers.addItemListener(e -> combo_groups.setSelectedItem(ITEM_UNSET));
        combo_groups.addItemListener(e -> combo_teachers.setSelectedItem(ITEM_UNSET));

        model_from = new XSpinnerDateModel();
        model_to = new XSpinnerDateModel();
        var spinner_from = newJSpinner(model_from);
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

        progressBar = new JProgressBar();
        progressBar.setMaximum(PROGRESS_MAX);
        progressBar.setStringPainted(true);
        resetProgress();

        var button = new JButton(BUTTON_REQUEST);
        button.addActionListener(buttonClickListener);

        /*
         * Layout sketch
         *
         * H: || || [(||) (||)] [||]
         * V:
         * |     ---
         * |     000000000000000000000000000
         * |     ---
         * |     000000000000000000000000000
         * ||    ---            ---
         * ||    000000000000   000000000000
         * ||    ==================   000000
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
                .addGroup(layout.createSequentialGroup()
                        .addComponent(progressBar)
                        .addComponent(button))
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
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(progressBar)
                        .addComponent(button))
        );

        Components.applyDefaults(container);
    }

    @Override
    protected void inBackground() {
        super.inBackground();

        requestLists();
    }


    private static JComboBox<String> newJComboBox() {
        var box = new JComboBox<String>();
        box.setEditable(true);
        JComboBoxAutoCompletion.enable(box);

        box.addMouseWheelListener(e -> MouseWheelScroller.scroll(box, e));

        box.addItem(ITEM_UNSET);
        return box;
    }

    private static JSpinner newJSpinner(XSpinnerDateModel model) {
        var spinner = new JSpinner();
        spinner.setModel(model);
        spinner.addMouseWheelListener(e -> MouseWheelScroller.scroll(spinner, e));
        return spinner;
    }

    private void requestLists() {
        setProgress(50, LOADING_TEACHERS);

        combo_teachers.setEnabled(false);
        Request.teachers().forEach(combo_teachers::addItem);
        combo_teachers.setEnabled(true);

        setProgress(PROGRESS_MAX, LOADING_GROUPS);

        combo_groups.setEnabled(false);
        Request.groups().forEach(combo_groups::addItem);
        combo_groups.setEnabled(true);

        resetProgress();
    }

    private void resetProgress() {
        progressBar.setValue(0);
        progressBar.setString(LOADING_READY);
    }

    private void setProgress(int progress, String title) {
        progressBar.setValue(progress);
        progressBar.setString(title);
    }
}