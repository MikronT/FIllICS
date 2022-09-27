package com.mikront.fillics.gui;

import com.mikront.fillics.PreferenceManager;
import com.mikront.fillics.ics.CalendarData;
import com.mikront.fillics.schedule.*;
import com.mikront.util.Concat;
import com.mikront.util.Log;
import com.mikront.util.Utils;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;


public class MainForm extends Form {
    private static final File FILE_ICS = new File("import.ics");
    private static final File FILE_MAP_SUBJECTS = new File("map_subjects.txt");
    private static final File FILE_MAP_TYPES = new File("map_types.txt");
    private static final int PROGRESS_MAX = 100;

    private JButton button_request, button_export;
    private JCheckBox checkBox_optional;
    private JComboBox<String> combo_groups, combo_teachers;
    private JProgressBar progressBar;
    private JTextArea field_exclusions;
    private List<Day> schedule;
    private XSpinnerDateModel model_from, model_to;
    private final HashMap<String, String> map_subjects = new HashMap<>();
    private final HashMap<String, String> map_types = new HashMap<>();
    private final JPanel container = getContainer();
    private final PreferenceManager preferenceManager = new PreferenceManager();


    public static void main(String[] args) {
        Log.ging(true);
        Log.level(Log.LEVEL_DEBUG);

        Form.load(MainForm.class);
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        var frame = getFrame();
        frame.setMinimumSize(FORM_MAIN);

        var layout = new GroupLayout(container);
        container.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        var label_teacher = new JLabel(LABEL_TEACHER);
        var label_group = new JLabel(LABEL_GROUP);
        var label_from = new JLabel(LABEL_DATE_FROM);
        var label_to = new JLabel(LABEL_DATE_TO);
        var label_exclusions = new JLabel(LABEL_EXCLUSIONS);

        combo_teachers = Components.newOptionalJComboBox();
        combo_teachers.addItemListener(e -> preferenceManager.setTeacher(e.getItem().toString()));
        combo_teachers.addItemListener(e -> {
            if (!Components.ITEM_UNSET.equals(combo_groups.getSelectedItem()))
                combo_groups.setSelectedItem(Components.ITEM_UNSET);
        });

        combo_groups = Components.newOptionalJComboBox();
        combo_groups.addItemListener(e -> preferenceManager.setGroup(e.getItem().toString()));
        combo_groups.addItemListener(e -> {
            if (!Components.ITEM_UNSET.equals(combo_teachers.getSelectedItem()))
                combo_teachers.setSelectedItem(Components.ITEM_UNSET);
        });

        model_from = new XSpinnerDateModel();
        model_to = new XSpinnerDateModel();

        var spinner_from = Components.newJSpinner(model_from);
        spinner_from.addChangeListener(e -> {
            LocalDate
                    date1 = model_from.getDate(),
                    date2 = model_to.getDate();
            if (date1.compareTo(date2) > 0)
                model_to.setValue(date1);
        });
        var spinner_to = Components.newJSpinner(model_to);
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

        button_request = new JButton(BUTTON_REQUEST);
        button_request.addActionListener(e -> new Thread(this::requestSchedule).start());

        field_exclusions = new JTextArea();
        field_exclusions.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                preferenceManager.setExclusions(getExclusions());
            }
        });

        checkBox_optional = new JCheckBox(CHECK_OPTIONAL);
        checkBox_optional.addActionListener(e ->
                preferenceManager.setShouldIncludeOptional(checkBox_optional.isSelected()));

        button_export = new JButton(BUTTON_EXPORT);
        button_export.setEnabled(false);
        button_export.addActionListener(e -> new Thread(this::exportSchedule).start());

        /*
         * Layout sketch
         *
         * ---                           ---
         * 000000000000000000000000000   0000000000000000
         * ---                           0000000000000000
         * 000000000000000000000000000   0000000000000000
         * ---            ---            0000000000000000
         * 000000000000   000000000000   0000000000000000
         * =================   [00000]   [x]---    000000
         */
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                        .addComponent(label_teacher)
                        .addComponent(combo_teachers, COMBO_WIDTH, COMBO_WIDTH, GroupLayout.DEFAULT_SIZE)
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
                                .addComponent(button_request)))
                .addGap(GAP)
                .addGroup(layout.createParallelGroup()
                        .addComponent(label_exclusions)
                        .addComponent(field_exclusions, FIELD_WIDTH, FIELD_WIDTH, GroupLayout.DEFAULT_SIZE)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(checkBox_optional, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(button_export)))
        );
        layout.setVerticalGroup(layout.createParallelGroup()
                .addGroup(layout.createSequentialGroup()
                        .addComponent(label_teacher)
                        .addComponent(combo_teachers)
                        .addComponent(label_group)
                        .addComponent(combo_groups)
                        .addGroup(layout.createParallelGroup()
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(label_from)
                                        .addComponent(spinner_from))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(label_to)
                                        .addComponent(spinner_to)))
                        .addGap(GAP)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(progressBar)
                                .addComponent(button_request)))
                .addGroup(layout.createSequentialGroup()
                        .addComponent(label_exclusions)
                        .addComponent(field_exclusions)
                        .addGap(GAP)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(checkBox_optional)
                                .addComponent(button_export)))
        );

        Components.applyDefaults(container);
    }


    @Override
    protected void onPostShow() {
        super.onPostShow();

        new Thread(this::presetOptions).start();
    }

    private void presetOptions() {
        requestLists();

        combo_teachers.setSelectedItem(preferenceManager.getTeacher());
        combo_groups.setSelectedItem(preferenceManager.getGroup());
        setExclusions(preferenceManager.getExclusions());
        checkBox_optional.setSelected(preferenceManager.getShouldIncludeOptional());
    }

    private void requestLists() {
        setProgress(50, STEP_GETTING_TEACHERS);

        combo_teachers.setEnabled(false);
        combo_groups.setEnabled(false);

        Schedule.getTeachers().forEach(combo_teachers::addItem);
        combo_teachers.setEnabled(true);

        setProgress(PROGRESS_MAX, STEP_GETTING_GROUPS);

        Schedule.getGroups().forEach(combo_groups::addItem);
        combo_groups.setEnabled(true);

        try (var stream = new FileInputStream(FILE_MAP_SUBJECTS);
             var scanner = new Scanner(stream)) {
            map_subjects.clear();
            while (scanner.hasNextLine()) {
                var strings = scanner.nextLine().split(";");
                if (strings.length != 2)
                    continue;
                map_subjects.put(strings[0].toLowerCase(Locale.ROOT), strings[1]);
            }
        } catch (IOException e) {
            Log.w("MainForm::requestLists: unable to load subjects map");
            Log.w("MainForm::requestLists:   - file = " + FILE_MAP_SUBJECTS);
            Log.w("MainForm::requestLists:   = catching: ", e);
        }

        try (var stream = new FileInputStream(FILE_MAP_TYPES);
             var scanner = new Scanner(stream)) {
            map_types.clear();
            while (scanner.hasNextLine()) {
                var strings = scanner.nextLine().split(";");
                if (strings.length != 2)
                    continue;
                map_types.put(strings[0].toLowerCase(Locale.ROOT), strings[1]);
            }
        } catch (IOException e) {
            Log.w("MainForm::requestLists: unable to load types map");
            Log.w("MainForm::requestLists:   - file = " + FILE_MAP_TYPES);
            Log.w("MainForm::requestLists:   = catching: ", e);
        }

        resetProgress();
    }

    private void requestSchedule() {
        var teacher = (String) combo_teachers.getSelectedItem();
        var group = (String) combo_groups.getSelectedItem();

        if (Utils.isEmpty(teacher) && Utils.isEmpty(group))
            return;

        setProgress(60, STEP_GETTING_SCHEDULE);

        Document doc = Schedule.getSchedule(
                teacher,
                group,
                model_from.getDate(),
                model_to.getDate());

        setProgress(100, STEP_COMPILING_DATA);

        schedule = Parser.of(doc)
                .setDefaultGroup(group)
                .parse();

        button_export.setEnabled(true);
        resetProgress();
    }

    private void exportSchedule() {
        button_request.setEnabled(false);
        button_export.setEnabled(false);

        boolean shouldSkipOptional = !checkBox_optional.isSelected();
        var exclusions = getExclusions();

        CalendarData data = new CalendarData();
        for (Day day : schedule)
            for (Cell cell : day)
                for (Session session : cell) {
                    if (shouldSkipOptional && session.isOptional()) continue;

                    var sSubject = session.getSubject().toLowerCase(Locale.ROOT);
                    if (exclusions.stream().anyMatch(sSubject::contains)) continue;

                    var sType = session.getType().toLowerCase(Locale.ROOT);
                    if (exclusions.stream().anyMatch(sType::contains)) continue;

                    var sGroup = session.getGroup().toLowerCase(Locale.ROOT);
                    if (exclusions.stream().anyMatch(sGroup::contains)) continue;

                    if (map_subjects.containsKey(sSubject)) session.setSubject(map_subjects.get(sSubject));
                    if (map_types.containsKey(sType)) session.setType(map_types.get(sType));

                    data.addEvent(session.toEvent(day, cell));
                }

        try (var stream = new FileOutputStream(FILE_ICS)) {
            stream.write(data.compile().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e("MainForm::requestSchedule: unable to write file");
            Log.e("MainForm::requestSchedule:   - file = " + FILE_ICS);
            Log.e("MainForm::requestSchedule:   = catching: ", e);
        }

        button_request.setEnabled(true);
        button_export.setEnabled(true);
    }


    private void setProgress(int progress, String title) {
        progressBar.setValue(progress);
        progressBar.setString(title);
    }

    private void resetProgress() {
        setProgress(0, STEP_READY);
    }

    private List<String> getExclusions() {
        return Arrays.stream(field_exclusions.getText()
                        .toLowerCase(Locale.ROOT)
                        .split(Parser.REGEX_NEWLINE.pattern()))
                .toList();
    }

    private void setExclusions(List<String> list) {
        if (list.isEmpty())
            return;

        field_exclusions.setText(Concat.me()
                .lines(list, item -> item)
                .enate());
    }
}