package com.mikront.fillics;

import com.mikront.fillics.gui.Components;
import com.mikront.fillics.gui.Form;
import com.mikront.fillics.gui.JCheckBoxList;
import com.mikront.fillics.gui.XSpinnerDateModel;
import com.mikront.fillics.ics.CalendarData;
import com.mikront.fillics.schedule.*;
import com.mikront.util.Utils;
import com.mikront.util.debug.Build;
import com.mikront.util.debug.Log;
import org.jsoup.nodes.Document;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

import static javax.swing.GroupLayout.DEFAULT_SIZE;


public class MainForm extends Form {
    private static final File FILE_ICS = new File("import.ics");
    private static final File FILE_MAP_SUBJECTS = new File("map_subjects.txt");
    private static final File FILE_MAP_TYPES = new File("map_types.txt");
    private static final int PROGRESS_MAX = 100;

    private JButton button_request, button_export;
    private JCheckBoxList checkBoxes_types, checkBoxes_subjects, checkBoxes_groups;
    private JComboBox<String> combo_group, combo_teacher;
    private JProgressBar progressBar;
    private List<Day> schedule;
    private XSpinnerDateModel model_from, model_to;
    private final HashMap<String, String> map_subjects = new HashMap<>();
    private final HashMap<String, String> map_types = new HashMap<>();
    private final PreferenceManager preferenceManager = new PreferenceManager();


    public static void main(String[] args) {
        Build.debug(false);
        Log.ging(true);
        Log.level(Log.LEVEL_DEBUG);

        Form.load(MainForm.class);
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        var container = getContainer();
        var layout = new GroupLayout(container);
        container.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);


        var label_teacher = new JLabel(LABEL_TEACHER);
        var label_group = new JLabel(LABEL_GROUP);
        var label_from = new JLabel(LABEL_DATE_FROM);
        var label_to = new JLabel(LABEL_DATE_TO);

        combo_teacher = Components.newOptionalJComboBox();
        combo_teacher.addItemListener(e -> preferenceManager.setTeacher(e.getItem().toString()));
        combo_teacher.addItemListener(e -> {
            if (!Components.ITEM_UNSET.equals(combo_group.getSelectedItem()))
                combo_group.setSelectedItem(Components.ITEM_UNSET);
        });

        combo_group = Components.newOptionalJComboBox();
        combo_group.addItemListener(e -> preferenceManager.setGroup(e.getItem().toString()));
        combo_group.addItemListener(e -> {
            if (!Components.ITEM_UNSET.equals(combo_teacher.getSelectedItem()))
                combo_teacher.setSelectedItem(Components.ITEM_UNSET);
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
        button_request.addActionListener(e -> new Thread(() -> {
            button_request.setEnabled(false);

            requestSchedule();
            presetFilters();
            filterByTypes();
            filterBySubjects();

            button_request.setEnabled(true);
            button_export.setEnabled(true);
        }).start());


        var label_types = new JLabel(LABEL_TYPES);
        var label_subjects = new JLabel(LABEL_SUBJECTS);
        var label_groups = new JLabel(LABEL_GROUPS);

        checkBoxes_types = new JCheckBoxList(true);
        checkBoxes_subjects = new JCheckBoxList(true);
        checkBoxes_groups = new JCheckBoxList(true);

        checkBoxes_types.setOnItemCheckedListener((title, checked) -> {
            filterByTypes();
            filterBySubjects();

            if (!checked)
                preferenceManager.addFilterType(title);
            else preferenceManager.removeFilterType(title);
        });
        checkBoxes_subjects.setOnItemCheckedListener((title, checked) -> {
            filterBySubjects();

            if (!checked)
                preferenceManager.addFilterSubject(title);
            else preferenceManager.removeFilterSubject(title);
        });
        checkBoxes_groups.setOnItemCheckedListener((title, checked) -> {
            if (!checked)
                preferenceManager.addFilterGroup(title);
            else preferenceManager.removeFilterGroup(title);
        });

        button_export = new JButton(BUTTON_EXPORT);
        button_export.setEnabled(false);
        button_export.addActionListener(e -> new Thread(this::exportSchedule).start());


        layout.setHorizontalGroup(layout.createSequentialGroup()
                //Request schedule
                .addGroup(layout.createParallelGroup()
                        .addComponent(label_teacher)
                        .addComponent(combo_teacher, TEACHER_WIDTH, TEACHER_WIDTH, DEFAULT_SIZE)
                        .addComponent(label_group)
                        .addComponent(combo_group)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(label_from)
                                        .addComponent(spinner_from))
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(label_to)
                                        .addComponent(spinner_to)))
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(progressBar)
                                .addComponent(button_request))
                        .addComponent(label_types)
                        .addComponent(checkBoxes_types)
                )
                .addGap(GAP_BIG)
                //Filter subjects
                .addGroup(layout.createParallelGroup()
                        .addComponent(label_subjects, SUBJECTS_WIDTH, SUBJECTS_WIDTH, DEFAULT_SIZE)
                        .addComponent(checkBoxes_subjects)
                )
                .addGap(GAP_BIG)
                //Filter groups and types and export
                .addGroup(layout.createParallelGroup()
                        .addComponent(label_groups)
                        .addComponent(checkBoxes_groups, GROUPS_WIDTH, GROUPS_WIDTH, DEFAULT_SIZE)
                        .addComponent(button_export, GroupLayout.Alignment.TRAILING)
                )
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                //Request schedule
                .addGroup(layout.createSequentialGroup()
                        .addComponent(label_teacher)
                        .addComponent(combo_teacher)
                        .addComponent(label_group)
                        .addComponent(combo_group)
                        .addGroup(layout.createParallelGroup()
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(label_from)
                                        .addComponent(spinner_from))
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(label_to)
                                        .addComponent(spinner_to)))
                        .addGap(GAP_MEDIUM)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                                .addComponent(progressBar)
                                .addComponent(button_request))
                        .addGap(GAP_BIG)
                        .addComponent(label_types)
                        .addComponent(checkBoxes_types, TYPES_HEIGHT, TYPES_HEIGHT, DEFAULT_SIZE)
                )
                //Filter subjects
                .addGroup(layout.createSequentialGroup()
                        .addComponent(label_subjects)
                        .addComponent(checkBoxes_subjects)
                )
                //Filter groups and types and export
                .addGroup(layout.createSequentialGroup()
                        .addComponent(label_groups)
                        .addComponent(checkBoxes_groups, GROUPS_HEIGHT, GROUPS_HEIGHT, DEFAULT_SIZE)
                        .addComponent(button_export)
                )
        );

        Components.applyDefaults(container);
    }


    @Override
    protected void onPostShow() {
        super.onPostShow();

        new Thread(this::presetRequestOptions).start();
    }

    private void presetRequestOptions() {
        button_request.setEnabled(false);
        combo_teacher.setEnabled(false);
        combo_group.setEnabled(false);

        setProgress(50, STEP_GETTING_TEACHERS);

        Schedule.getTeachers().forEach(combo_teacher::addItem);
        combo_teacher.setEnabled(true);

        setProgress(PROGRESS_MAX, STEP_GETTING_GROUPS);

        Schedule.getGroups().forEach(combo_group::addItem);
        combo_group.setEnabled(true);

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
            Log.w("MainForm::presetRequestOptions: unable to load subjects map");
            Log.w("MainForm::presetRequestOptions:   - file = " + FILE_MAP_SUBJECTS);
            Log.w("MainForm::presetRequestOptions:   = catching: ", e);
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
            Log.w("MainForm::presetRequestOptions: unable to load types map");
            Log.w("MainForm::presetRequestOptions:   - file = " + FILE_MAP_TYPES);
            Log.w("MainForm::presetRequestOptions:   = catching: ", e);
        }

        combo_teacher.setSelectedItem(preferenceManager.getTeacher());
        combo_group.setSelectedItem(preferenceManager.getGroup());

        resetProgress();

        button_request.setEnabled(true);
    }

    private void requestSchedule() {
        var teacher = (String) combo_teacher.getSelectedItem();
        var group = (String) combo_group.getSelectedItem();

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

        resetProgress();
    }

    private void presetFilters() {
        var sessions = getSessions();
        checkBoxes_types.replaceWith(getSessionData(sessions, Session::getType));
        checkBoxes_subjects.replaceWith(getSessionData(sessions, Session::getSubject));
        checkBoxes_groups.replaceWith(getSessionData(sessions, Session::getGroup));

        preferenceManager.getFilterTypes().forEach(s -> checkBoxes_types.setChecked(s, false));
        preferenceManager.getFilterSubjects().forEach(s -> checkBoxes_subjects.setChecked(s, false));
        preferenceManager.getFilterGroups().forEach(s -> checkBoxes_groups.setChecked(s, false));
    }

    private void filterByTypes() {
        var newSessions = getSessions().stream()
                .filter(session -> checkBoxes_types.isChecked(session.getType()))
                .toList();
        var newSubjects = getSessionData(newSessions, Session::getSubject);
        var newGroups = getSessionData(newSessions, Session::getGroup);

        checkBoxes_subjects.replaceWith(newSubjects);
        checkBoxes_groups.replaceWith(newGroups);
    }

    private void filterBySubjects() {
        var newSessions = getSessions().stream()
                .filter(session -> {
                    var subject = session.getSubject();
                    if (Utils.isEmpty(subject)) {
                        Log.i("MainForm::filterBySubjects: Predicate-> subject is empty");
                        Log.i("MainForm::filterBySubjects: Predicate->   - session = " + session);
                        Log.i("MainForm::filterBySubjects: Predicate->   - subject = " + subject);
                    }

                    return checkBoxes_subjects.isChecked(subject);
                })
                .toList();
        var newGroups = getSessionData(newSessions, Session::getGroup);

        checkBoxes_groups.replaceWith(newGroups);
    }

    private void exportSchedule() {
        button_request.setEnabled(false);
        button_export.setEnabled(false);

        CalendarData data = new CalendarData();
        for (Day day : schedule)
            for (Cell cell : day)
                for (Session session : cell) {
                    var type = session.getType();
                    var subject = session.getSubject();
                    var group = session.getGroup();

                    if (!checkBoxes_types.isChecked(type)) continue;
                    if (!checkBoxes_subjects.isChecked(subject)) continue;
                    if (!checkBoxes_groups.isChecked(group)) continue;

                    if (map_types.containsKey(type)) session.setType(map_types.get(type));
                    if (map_subjects.containsKey(subject)) session.setSubject(map_subjects.get(subject));

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


    private List<Session> getSessions() {
        List<Session> out = new ArrayList<>();
        for (Day day : schedule)
            for (Cell cell : day)
                for (Session session : cell)
                    out.add(session);
        return out;
    }

    private static List<String> getSessionData(List<Session> sessions, Function<Session, String> mapper) {
        return sessions.stream()
                .map(mapper)
                .distinct()
                .toList();
    }
}