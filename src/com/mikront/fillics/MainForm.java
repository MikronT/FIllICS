package com.mikront.fillics;

import com.mikront.fillics.ics.CalendarData;
import com.mikront.fillics.resource.Dimens;
import com.mikront.fillics.resource.Strings;
import com.mikront.fillics.schedule.*;
import com.mikront.gui.*;
import com.mikront.util.Concat;
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
        var panel_root = getRootPanel();

        var panel_request = createRequestPanel();
        var panel_types = createTypesFilterPanel();
        var panel_subjects = createSubjectsFilterPanel();
        var panel_groups = createGroupsFilterPanel();

        button_export = new JButton(getString(Strings.BUTTON_EXPORT));
        button_export.setEnabled(false);
        button_export.addActionListener(e -> new Thread(this::exportSchedule).start());


        var layout = initGroupLayoutFor(panel_root);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                //Request schedule and filter types
                .addGroup(layout.createParallelGroup()
                        .addComponent(panel_request)
                        .addComponent(panel_types)
                )
                .addGap(Dimens.GAP_BIG)
                //Filter subjects
                .addComponent(panel_subjects, Dimens.SUBJECTS_WIDTH, Dimens.SUBJECTS_WIDTH, DEFAULT_SIZE)
                .addGap(Dimens.GAP_BIG)
                //Filter groups and export button
                .addGroup(layout.createParallelGroup()
                        .addComponent(panel_groups, Dimens.GROUPS_WIDTH, Dimens.GROUPS_WIDTH, DEFAULT_SIZE)
                        .addComponent(button_export, GroupLayout.Alignment.TRAILING)
                )
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                //Request schedule and filter types
                .addGroup(layout.createSequentialGroup()
                        .addComponent(panel_request)
                        .addGap(Dimens.GAP_BIG)
                        .addComponent(panel_types, Dimens.TYPES_HEIGHT, Dimens.TYPES_HEIGHT, DEFAULT_SIZE)
                )
                //Filter subjects
                .addComponent(panel_subjects)
                //Filter groups and export button
                .addGroup(layout.createSequentialGroup()
                        .addComponent(panel_groups, Dimens.GROUPS_HEIGHT, Dimens.GROUPS_HEIGHT, DEFAULT_SIZE)
                        .addComponent(button_export)
                )
        );
    }

    private JPanel createRequestPanel() {
        var out = new JPanel();
        out.setBorder(BorderFactory.createTitledBorder(getString(Strings.PANEL_REQUEST)));

        var label_teacher = new JLabel(getString(Strings.LABEL_TEACHER));
        var label_group = new JLabel(getString(Strings.LABEL_GROUP));
        var label_from = new JLabel(getString(Strings.LABEL_DATE_FROM));
        var label_to = new JLabel(getString(Strings.LABEL_DATE_TO));

        combo_teacher = new JAutoComboBox();
        combo_teacher.addItemListener(e -> {
            //Update preference state
            preferenceManager.setTeacher(e.getItem().toString());

            //Unset group filter
            if (!JAutoComboBox.ITEM_UNSET.equals(combo_group.getSelectedItem()))
                combo_group.setSelectedItem(JAutoComboBox.ITEM_UNSET);
        });

        combo_group = new JAutoComboBox();
        combo_group.addItemListener(e -> {
            //Update preference state
            preferenceManager.setGroup(e.getItem().toString());

            //Unset teacher filter
            if (!JAutoComboBox.ITEM_UNSET.equals(combo_teacher.getSelectedItem()))
                combo_teacher.setSelectedItem(JAutoComboBox.ITEM_UNSET);
        });

        model_from = new XSpinnerDateModel();
        model_to = new XSpinnerDateModel();

        var spinner_from = new JDateSpinner(model_from);
        spinner_from.addChangeListener(e -> {
            LocalDate
                    date1 = model_from.getDate(),
                    date2 = model_to.getDate();
            //Reset if date out of bounds
            if (date1.isAfter(date2))
                model_to.setValue(date1);
        });

        var spinner_to = new JDateSpinner(model_to);
        spinner_to.addChangeListener(e -> {
            LocalDate
                    date1 = model_from.getDate(),
                    date2 = model_to.getDate();
            //Reset if date out of bounds
            if (date1.isAfter(date2))
                model_from.setValue(date2);
        });

        progressBar = new JProgressBar();
        progressBar.setMaximum(PROGRESS_MAX);
        progressBar.setStringPainted(true);
        resetProgress();

        button_request = new JButton(getString(Strings.BUTTON_REQUEST));
        button_request.addActionListener(e -> new Thread(() -> {
            button_request.setEnabled(false);

            requestSchedule();
            presetFilters();
            filterByTypes();
            filterBySubjects();

            button_request.setEnabled(true);
            button_export.setEnabled(true);
        }).start());


        var layout = initGroupLayoutFor(out);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(label_teacher)
                .addComponent(combo_teacher, Dimens.TEACHER_WIDTH, Dimens.TEACHER_WIDTH, DEFAULT_SIZE)
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
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
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
                .addGap(Dimens.GAP_MEDIUM)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(progressBar)
                        .addComponent(button_request))
        );

        return out;
    }

    private JCheckBoxList createTypesFilterPanel() {
        checkBoxes_types = new JCheckBoxList(true);
        checkBoxes_types.setBorder(BorderFactory.createTitledBorder(getString(Strings.PANEL_TYPES)));
        checkBoxes_types.setOnItemCheckedListener((title, checked) -> {
            filterByTypes();
            filterBySubjects();

            if (!checked)
                preferenceManager.addFilterType(title);
            else preferenceManager.removeFilterType(title);
        });
        return checkBoxes_types;
    }

    private JCheckBoxList createSubjectsFilterPanel() {
        checkBoxes_subjects = new JCheckBoxList(true);
        checkBoxes_subjects.setBorder(BorderFactory.createTitledBorder(getString(Strings.PANEL_SUBJECTS)));
        checkBoxes_subjects.setOnItemCheckedListener((title, checked) -> {
            filterBySubjects();

            if (!checked)
                preferenceManager.addFilterSubject(title);
            else preferenceManager.removeFilterSubject(title);
        });
        return checkBoxes_subjects;
    }

    private JCheckBoxList createGroupsFilterPanel() {
        checkBoxes_groups = new JCheckBoxList(true);
        checkBoxes_groups.setBorder(BorderFactory.createTitledBorder(getString(Strings.PANEL_GROUPS)));
        checkBoxes_groups.setOnItemCheckedListener((title, checked) -> {
            if (!checked)
                preferenceManager.addFilterGroup(title);
            else preferenceManager.removeFilterGroup(title);
        });
        return checkBoxes_groups;
    }

    private static GroupLayout initGroupLayoutFor(JComponent component) {
        var out = new GroupLayout(component);

        //Enable automatic gaps
        out.setAutoCreateGaps(true);
        out.setAutoCreateContainerGaps(true);

        //Bind layout to the component
        component.setLayout(out);
        return out;
    }


    @Override
    protected void onPostShow() {
        new Thread(this::presetRequestOptions).start();
    }

    private void presetRequestOptions() {
        button_request.setEnabled(false);
        combo_teacher.setEnabled(false);
        combo_group.setEnabled(false);

        setProgress(50, getString(Strings.STEP_GETTING_TEACHERS));

        Schedule.getTeachers().forEach(combo_teacher::addItem);
        combo_teacher.setEnabled(true);

        setProgress(PROGRESS_MAX, getString(Strings.STEP_GETTING_GROUPS));

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

        setProgress(60, getString(Strings.STEP_GETTING_SCHEDULE));

        Document doc = Schedule.getSchedule(
                teacher,
                group,
                model_from.getDate(),
                model_to.getDate());

        setProgress(100, getString(Strings.STEP_COMPILING_DATA));

        schedule = Parser.init(this)
                .setDocument(doc)
                .setDefaultGroup(group)
                .parse();

        resetProgress();
    }

    private void presetFilters() {
        var sessions = getSessions();
        checkBoxes_types.replaceWith(getSessionData(sessions, Session::getTypeOrUnknown));
        checkBoxes_subjects.replaceWith(getSessionData(sessions, Session::getSubject));
        checkBoxes_groups.replaceWith(getSessionData(sessions, Session::getGroupOrUnknown));

        preferenceManager.getFilterTypes().forEach(s -> checkBoxes_types.setChecked(s, false));
        preferenceManager.getFilterSubjects().forEach(s -> checkBoxes_subjects.setChecked(s, false));
        preferenceManager.getFilterGroups().forEach(s -> checkBoxes_groups.setChecked(s, false));
    }

    private void filterByTypes() {
        var newSessions = getSessions().stream()
                .filter(session -> checkBoxes_types.isChecked(session.getTypeOrUnknown()))
                .toList();
        var newSubjects = getSessionData(newSessions, Session::getSubject);
        var newGroups = getSessionData(newSessions, Session::getGroupOrUnknown);

        checkBoxes_subjects.replaceWith(newSubjects);
        checkBoxes_groups.replaceWith(newGroups);
    }

    private void filterBySubjects() {
        var newSessions = getSessions().stream()
                .filter(session -> checkBoxes_types.isChecked(session.getTypeOrUnknown()))
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
        var newGroups = getSessionData(newSessions, Session::getGroupOrUnknown);

        checkBoxes_groups.replaceWith(newGroups);
    }

    private void exportSchedule() {
        button_request.setEnabled(false);
        button_export.setEnabled(false);

        CalendarData data = new CalendarData();
        for (Day day : schedule)
            for (Cell cell : day)
                for (Session session : cell) {
                    if (!checkBoxes_types.isChecked(session.getTypeOrUnknown())) continue;

                    var subject = session.getSubject();
                    if (!checkBoxes_subjects.isChecked(subject)) continue;

                    if (!checkBoxes_groups.isChecked(session.getGroupOrUnknown())) continue;

                    var type = session.getType();
                    var type_final = map_types.getOrDefault(type.toLowerCase(), type);
                    var subject_final = map_subjects.getOrDefault(subject.toLowerCase(), subject);

                    data.addEvent(session.toEvent(day, cell, session1 -> Concat.me()
                            .word(subject_final)
                            .when(Utils.notEmpty(type_final))
                            .words(" (", type_final, ")")
                            .enate(), Session::getDescription));
                }

        try (var stream = new FileOutputStream(FILE_ICS)) {
            stream.write(data.compile().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            Log.e("MainForm::exportSchedule: unable to write file");
            Log.e("MainForm::exportSchedule:   - file = " + FILE_ICS);
            Log.e("MainForm::exportSchedule:   = catching: ", e);
        }

        button_request.setEnabled(true);
        button_export.setEnabled(true);
    }


    private void setProgress(int progress, String title) {
        progressBar.setValue(progress);
        progressBar.setString(title);
    }

    private void resetProgress() {
        setProgress(0, getString(Strings.STEP_READY));
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