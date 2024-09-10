package com.mikront.fillics;

import com.mikront.fillics.ics.CalendarData;
import com.mikront.fillics.schedule.*;
import com.mikront.gui.*;
import com.mikront.util.Concat;
import com.mikront.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger log = LogManager.getLogger();

    private static final File FILE_ICS = new File("import.ics");
    private static final File FILE_MAP_SUBJECTS = new File("map_subjects.txt");
    private static final File FILE_MAP_TYPES = new File("map_types.txt");
    private static final int PROGRESS_MAX = 100;

    private JButton button_request, button_export;
    private JCheckBoxList checkBoxes_types, checkBoxes_subjects, checkBoxes_groups;
    private JComboBox<String> combo_group, combo_teacher;
    private JDateSpinner spinner_from, spinner_to;
    private JProgressBar progressBar;
    private JTextField input_group;
    private List<Day> schedule;
    private XSpinnerDateModel model_from, model_to;
    private final HashMap<String, String> map_subjects = new HashMap<>();
    private final HashMap<String, String> map_types = new HashMap<>();
    private final PreferenceManager preferenceManager = new PreferenceManager();


    public static void main(String[] args) {
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

        button_export = new JButton(lang.getString("button_export"));
        button_export.setEnabled(false);
        button_export.addActionListener(_ -> new Thread(this::exportSchedule).start());


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
        out.setBorder(BorderFactory.createTitledBorder(lang.getString("panel_request")));

        var label_teacher = new JLabel(lang.getString("label_teacher"));
        var label_group = new JLabel(lang.getString("label_group"));

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

        button_request = new JButton(lang.getString("button_request"));
        button_request.addActionListener(_ -> new Thread(() -> {
            button_request.setEnabled(false);

            requestSchedule();
            updateFilters(3);

            button_request.setEnabled(true);
            spinner_from.setEnabled(true);
            spinner_to.setEnabled(true);
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
        out.setBorder(BorderFactory.createTitledBorder(lang.getString("panel_import")));

        var label_group = new JLabel(lang.getString("label_placeholder_group"));

        input_group = new JTextField();
        input_group.setText(preferenceManager.getGroup());

        var button_open = new JButton(lang.getString("button_open"));
        button_open.addActionListener(_ -> {
            JFileChooser picker = new JFileChooser();
            if (Schedule.CACHE_DIR.exists()) //Use cache dir if possible
                picker.setCurrentDirectory(Schedule.CACHE_DIR);
            else //Set current executable path
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
                log.warn("Failed to read file '{}'", selectedFile, ex);
                return;
            }

            log.info("User picked file '{}' to read", selectedFile);

            importScheduleFrom(selectedFile);
            updateFilters(3);

            spinner_from.setEnabled(true);
            spinner_to.setEnabled(true);
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
        out.setBorder(BorderFactory.createTitledBorder(lang.getString("panel_period")));

        var label_from = new JLabel(lang.getString("label_date_from"));
        var label_to = new JLabel(lang.getString("label_date_to"));

        model_from = new XSpinnerDateModel(
                Schedule.DATE_FROM::compareTo,
                Schedule.DATE_TO::compareTo);
        model_to = new XSpinnerDateModel(
                Schedule.DATE_FROM::compareTo,
                Schedule.DATE_TO::compareTo,
                model_from.getValue().plusDays(6)); //Default period of a week

        spinner_from = new JDateSpinner(model_from);
        spinner_from.setEnabled(false);
        spinner_from.addChangeListener(_ -> {
            LocalDate
                    date1 = model_from.getValue(),
                    date2 = model_to.getValue();

            if (date1.isAfter(date2)) {
                model_to.setValue(date1); //Move 2nd bound forward
                return;
            }

            updateFilters(3);
        });

        spinner_to = new JDateSpinner(model_to);
        spinner_to.setEnabled(false);
        spinner_to.addChangeListener(_ -> {
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
        checkBoxes_types.setBorder(BorderFactory.createTitledBorder(lang.getString("panel_types")));
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
        checkBoxes_subjects.setBorder(BorderFactory.createTitledBorder(lang.getString("panel_subjects")));
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
        checkBoxes_groups.setBorder(BorderFactory.createTitledBorder(lang.getString("panel_groups")));
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

        setProgress(50, lang.getString("step_getting_teachers"));

        Schedule.getTeachers().forEach(combo_teacher::addItem);
        combo_teacher.setEnabled(true);

        setProgress(PROGRESS_MAX, lang.getString("step_getting_groups"));

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
            log.warn("Unable to load subjects map from file '{}'", FILE_MAP_SUBJECTS, e);
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
            log.warn("Unable to load types map from file '{}'", FILE_MAP_TYPES, e);
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

        setProgress(60, lang.getString("step_getting_schedule"));

        Document doc = Schedule.getSchedule(teacher, group);

        setProgress(100, lang.getString("step_compiling_data"));

        schedule = Parser.init()
                .setDocument(doc)
                .setDefaultGroup(group)
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
            log.error("Parsing file '{}' failed", file, e);
            return;
        }

        schedule = Parser.init()
                .setDocument(doc)
                .setDefaultGroup(group)
                .parse();
    }

    private void updateFilters(int amount) {
        switch (amount) {
            case 3:
                //Update types, subjects, and groups filters
                var sessions3 = getSessions();
                var newTypes = getSessionData(sessions3, MainForm::getSessionTypeOrUnknown);

                checkBoxes_types.replaceWith(newTypes);
                preferenceManager.getFilterTypes().forEach(s -> checkBoxes_types.setChecked(s, false));

            case 2:
                //Update only subjects and groups filters
                var sessions2 = getSessions().stream()
                        .filter(session -> checkBoxes_types.isChecked(getSessionTypeOrUnknown(session)))
                        .toList();
                var newSubjects = getSessionData(sessions2, Session::getSubject);

                checkBoxes_subjects.replaceWith(newSubjects);
                preferenceManager.getFilterSubjects().forEach(s -> checkBoxes_subjects.setChecked(s, false));

            case 1:
                //Update only groups filters
                var sessions1 = getSessions().stream()
                        .filter(session -> checkBoxes_types.isChecked(getSessionTypeOrUnknown(session)))
                        .filter(session -> {
                            var subject = session.getSubject();
                            if (Utils.isEmpty(subject))
                                log.info("Session '{}' subject '{}' is empty", session, subject);

                            return checkBoxes_subjects.isChecked(subject);
                        })
                        .toList();
                var newGroups = getSessionData(sessions1, MainForm::getSessionGroupOrUnknown);

                checkBoxes_groups.replaceWith(newGroups);
                preferenceManager.getFilterGroups().forEach(s -> checkBoxes_groups.setChecked(s, false));
        }
    }

    private void exportSchedule() {
        button_request.setEnabled(false);
        button_export.setEnabled(false);

        CalendarData data = new CalendarData();
        var start = model_from.getValue();
        var end = model_to.getValue();

        for (Day day : schedule) {
            var date = day.getDate();
            if (date.isBefore(start) || date.isAfter(end))
                continue;

            for (Cell cell : day)
                for (Session session : cell) {
                    if (!checkBoxes_types.isChecked(getSessionTypeOrUnknown(session))) continue;

                    var subject = session.getSubject();
                    if (!checkBoxes_subjects.isChecked(subject)) continue;

                    if (!checkBoxes_groups.isChecked(getSessionGroupOrUnknown(session))) continue;

                    var type = session.getType();
                    var type_final = map_types.getOrDefault(type.toLowerCase(), type);
                    var subject_final = map_subjects.getOrDefault(subject.toLowerCase(), subject);

                    data.addEvent(session.toEvent(
                            day,
                            cell,
                            _ -> Concat.me()
                                    .word(subject_final)
                                    .when(Utils.notEmpty(type_final))
                                    .words(" (", type_final, ")")
                                    .enate(),
                            Session::getDescription));
                }
        }

        try (var stream = new FileOutputStream(FILE_ICS)) {
            stream.write(data.compile().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Unable to write file '{}'", FILE_ICS, e);
        }

        button_request.setEnabled(true);
        button_export.setEnabled(true);
    }


    private void setProgress(int progress, String title) {
        progressBar.setValue(progress);
        progressBar.setString(title);
    }

    private void resetProgress() {
        setProgress(0, lang.getString("step_ready"));
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

    private static String getSessionTypeOrUnknown(Session session) {
        String type = session.getType();
        return Utils.isEmpty(type) ?
                lang.getString("unknown_type") :
                type;
    }

    private static String getSessionGroupOrUnknown(Session session) {
        String group = session.getGroup();
        return Utils.isEmpty(group) ?
                lang.getString("unknown_group") :
                group;
    }
}