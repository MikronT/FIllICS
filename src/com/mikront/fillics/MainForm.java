package com.mikront.fillics;

import com.mikront.fillics.ics.CalendarData;
import com.mikront.fillics.resource.Dimens;
import com.mikront.fillics.resource.Strings;
import com.mikront.fillics.schedule.*;
import com.mikront.gui.*;
import com.mikront.util.Concat;
import com.mikront.util.Utils;
import com.mikront.util.debug.Log;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
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
    private JTextField input_group;
    private List<Day> schedule;
    private XSpinnerDateModel model_from, model_to;
    private final HashMap<String, String> map_subjects = new HashMap<>();
    private final HashMap<String, String> map_types = new HashMap<>();
    private final PreferenceManager preferenceManager = new PreferenceManager();


    public static void main(String[] args) {
        Log.ging(true);
        Log.level(Log.LEVEL_WARN);

        Form.load(MainForm.class);
    }

    @Override
    protected void onCreate() {
        var panel_root = getRootPanel();
        var panel_request = createScheduleRequestPanel();
        var panel_import = createFileImportPanel();
        var panel_period = createPeriodFilterPanel();
        var panel_types = createTypesFilterPanel();
        var panel_subjects = createSubjectsFilterPanel();
        var panel_groups = createGroupsFilterPanel();

        button_export = new JButton(getString(Strings.BUTTON_EXPORT));
        button_export.setEnabled(false);
        button_export.addActionListener(e -> new Thread(this::exportSchedule).start());


        var layout = initGroupLayoutFor(panel_root);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(panel_request) //Request schedule
                        .addComponent(panel_import) //Import schedule
                )
                .addGap(Dimens.GAP_BIG)
                .addGroup(layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(panel_period, //Filter period
                                        Dimens.PERIOD_WIDTH,
                                        Dimens.PERIOD_WIDTH,
                                        Dimens.PERIOD_WIDTH)
                                .addComponent(panel_types) //Filter types
                        )
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(panel_subjects, //Filter subjects
                                        Dimens.SUBJECTS_WIDTH,
                                        Dimens.SUBJECTS_WIDTH,
                                        DEFAULT_SIZE)
                                .addGroup(layout.createParallelGroup()
                                        .addComponent(panel_groups, //Filter groups
                                                Dimens.GROUPS_WIDTH,
                                                Dimens.GROUPS_WIDTH,
                                                DEFAULT_SIZE)
                                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                .addComponent(button_export) //Export button
                                        )
                                )
                        )
                )
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(panel_request)
                                .addGap(Dimens.GAP_MEDIUM)
                                .addComponent(panel_import)
                        )
                )
                .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addComponent(panel_period)
                                .addComponent(panel_types)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(panel_subjects,
                                        Dimens.SUBJECTS_HEIGHT,
                                        Dimens.SUBJECTS_HEIGHT,
                                        DEFAULT_SIZE)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(panel_groups)
                                        .addGroup(layout.createParallelGroup()
                                                .addComponent(button_export)
                                        )
                                )
                        )
                )
        );
    }

    private JPanel createScheduleRequestPanel() {
        var out = new JPanel();
        out.setBorder(BorderFactory.createTitledBorder(getString(Strings.PANEL_REQUEST)));

        var label_teacher = new JLabel(getString(Strings.LABEL_TEACHER));
        var label_group = new JLabel(getString(Strings.LABEL_GROUP));

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

        progressBar = new JProgressBar();
        progressBar.setMaximum(PROGRESS_MAX);
        progressBar.setStringPainted(true);
        resetProgress();

        button_request = new JButton(getString(Strings.BUTTON_REQUEST));
        button_request.addActionListener(e -> new Thread(() -> {
            button_request.setEnabled(false);

            requestSchedule();
            updateFilters(3);

            button_request.setEnabled(true);
            button_export.setEnabled(true);
        }).start());


        var layout = initGroupLayoutFor(out);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(label_teacher)
                .addComponent(combo_teacher,
                        Dimens.TEACHER_WIDTH,
                        Dimens.TEACHER_WIDTH,
                        DEFAULT_SIZE)
                .addComponent(label_group)
                .addComponent(combo_group)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(progressBar)
                        .addComponent(button_request))
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(label_teacher)
                .addComponent(combo_teacher)
                .addComponent(label_group)
                .addComponent(combo_group)
                .addGap(Dimens.GAP_MEDIUM)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(progressBar)
                        .addComponent(button_request))
        );

        return out;
    }

    private JPanel createFileImportPanel() {
        var out = new JPanel();
        out.setBorder(BorderFactory.createTitledBorder(getString(Strings.PANEL_IMPORT)));

        var label_group = new JLabel(getString(Strings.LABEL_PLACEHOLDER_GROUP));

        input_group = new JTextField();
        input_group.setText(preferenceManager.getGroup());

        var button_open = new JButton(getString(Strings.BUTTON_OPEN));
        button_open.addActionListener(e -> {
            JFileChooser picker = new JFileChooser();
            //Set current executable path
            picker.setCurrentDirectory(FileSystems.getDefault().getPath(".").toFile());

            //Filter file system objects
            picker.setFileSelectionMode(JFileChooser.FILES_ONLY);
            picker.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    var name = f.toString();

                    int i = name.lastIndexOf('.'); //Find extension
                    if (i == -1)
                        return true; //The file has no extension or is a folder
                    var extension = name.substring(i + 1);

                    //Show only webpages
                    return extension.contains("htm");
                }

                @Override
                public String getDescription() {
                    return null;
                }
            });

            //Ask user to choose a file
            picker.showOpenDialog(out);
            var selectedFile = picker.getSelectedFile();

            if (selectedFile == null)
                return; //Dialog closed

            try {
                if (Files.size(selectedFile.toPath()) == 0)
                    return; //File is empty or invalid
            } catch (IOException ex) {
                Log.w("MainForm::createFileImportPanel: file reading error");
                Log.w("MainForm::createFileImportPanel:   - selectedFile = " + selectedFile);
                Log.w("MainForm::createFileImportPanel:   = catching: ", e);
                return;
            }

            Log.i("MainForm::createFileImportPanel: user picked a file to read");
            Log.i("MainForm::createFileImportPanel:   - selectedFile = " + selectedFile);

            importScheduleFrom(selectedFile);
            updateFilters(3);

            button_export.setEnabled(true);
        });


        var layout = initGroupLayoutFor(out);
        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(label_group)
                .addComponent(input_group)
                .addComponent(button_open, GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(label_group)
                .addComponent(input_group)
                .addComponent(button_open)
        );

        return out;
    }

    private JPanel createPeriodFilterPanel() {
        var out = new JPanel();
        out.setBorder(BorderFactory.createTitledBorder(getString(Strings.PANEL_PERIOD)));

        var label_from = new JLabel(getString(Strings.LABEL_DATE_FROM));
        var label_to = new JLabel(getString(Strings.LABEL_DATE_TO));

        model_from = new XSpinnerDateModel(
                Schedule.DATE_FROM::compareTo,
                Schedule.DATE_TO::compareTo);
        model_to = new XSpinnerDateModel(
                Schedule.DATE_FROM::compareTo,
                Schedule.DATE_TO::compareTo,
                model_from.getValue().plusDays(6)); //Default period of a week

        var spinner_from = new JDateSpinner(model_from);
        spinner_from.addChangeListener(e -> {
            LocalDate
                    date1 = model_from.getValue(),
                    date2 = model_to.getValue();

            if (date1.isAfter(date2)) {
                model_to.setValue(date1); //Move 2nd bound forward
                return;
            }

            updateFilters(3);
        });

        var spinner_to = new JDateSpinner(model_to);
        spinner_to.addChangeListener(e -> {
            LocalDate
                    date1 = model_from.getValue(),
                    date2 = model_to.getValue();

            if (date1.isAfter(date2)) {
                model_from.setValue(date2); //Move 1st bound back
                return;
            }

            updateFilters(3);
        });


        var layout = initGroupLayoutFor(out);
        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addComponent(label_from)
                .addGap(Dimens.GAP_SMALL)
                .addComponent(spinner_from)
                .addGap(Dimens.GAP_MEDIUM)
                .addComponent(label_to)
                .addGap(Dimens.GAP_SMALL)
                .addComponent(spinner_to)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                .addComponent(label_from)
                .addComponent(spinner_from)
                .addComponent(label_to)
                .addComponent(spinner_to)
        );

        return out;
    }

    private JCheckBoxList createTypesFilterPanel() {
        checkBoxes_types = new JCheckBoxList(true);
        checkBoxes_types.setOrientation(JCheckBoxList.ORIENTATION_HORIZONTAL);
        checkBoxes_types.setBorder(BorderFactory.createTitledBorder(getString(Strings.PANEL_TYPES)));
        checkBoxes_types.setOnItemCheckedListener((title, checked) -> {
            updateFilters(2);

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
            updateFilters(1);

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

        Document doc = Schedule.getSchedule(teacher, group);

        setProgress(100, getString(Strings.STEP_COMPILING_DATA));

        schedule = Parser.init(this)
                .setDocument(doc)
                .setDefaultGroup(group)
                .setPeriod(model_from.getValue(), model_to.getValue())
                .parse();

        resetProgress();
    }

    private void importScheduleFrom(File file) {
        var group = input_group.getText();
        if (Utils.isEmpty(group))
            return;

        Document doc;
        try {
            doc = Jsoup.parse(file);
        } catch (IOException e) {
            Log.e("MainForm::importScheduleFrom: file parsing failed");
            Log.e("MainForm::importScheduleFrom:   - file = " + file);
            Log.e("MainForm::importScheduleFrom:   = catching: ", e);
            return;
        }

        schedule = Parser.init(this)
                .setDocument(doc)
                .setDefaultGroup(group)
                .setPeriod(model_from.getValue(), model_to.getValue())
                .parse();
    }

    private void updateFilters(int amount) {
        switch (amount) {
            case 3:
                //Update types, subjects, and groups filters
                var sessions3 = getSessions();
                var newTypes = getSessionData(sessions3, Session::getTypeOrUnknown);

                checkBoxes_types.replaceWith(newTypes);
                preferenceManager.getFilterTypes().forEach(s -> checkBoxes_types.setChecked(s, false));

            case 2:
                //Update only subjects and groups filters
                var sessions2 = getSessions().stream()
                        .filter(session -> checkBoxes_types.isChecked(session.getTypeOrUnknown()))
                        .toList();
                var newSubjects = getSessionData(sessions2, Session::getSubject);

                checkBoxes_subjects.replaceWith(newSubjects);
                preferenceManager.getFilterSubjects().forEach(s -> checkBoxes_subjects.setChecked(s, false));

            case 1:
                //Update only groups filters
                var sessions1 = getSessions().stream()
                        .filter(session -> checkBoxes_types.isChecked(session.getTypeOrUnknown()))
                        .filter(session -> {
                            var subject = session.getSubject();
                            if (Utils.isEmpty(subject)) {
                                Log.i("MainForm::updateFilters: Predicate-> subject is empty");
                                Log.i("MainForm::updateFilters: Predicate->   - session = " + session);
                                Log.i("MainForm::updateFilters: Predicate->   - subject = " + subject);
                            }
                            return checkBoxes_subjects.isChecked(subject);
                        })
                        .toList();
                var newGroups = getSessionData(sessions1, Session::getGroupOrUnknown);

                checkBoxes_groups.replaceWith(newGroups);
                preferenceManager.getFilterGroups().forEach(s -> checkBoxes_groups.setChecked(s, false));
        }
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
        var start = model_from.getValue();
        var end = model_to.getValue();

        for (Day day : schedule) {
            var date = day.getDate();
            if (date.isBefore(start) || date.isAfter(end))
                continue;

            for (Cell cell : day)
                for (Session session : cell)
                    out.add(session);
        }
        return out;
    }

    private static List<String> getSessionData(List<Session> sessions, Function<Session, String> mapper) {
        return sessions.stream()
                .map(mapper)
                .distinct()
                .toList();
    }
}