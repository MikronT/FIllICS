package com.mikront.fillics.schedule;

import com.mikront.gui.Context;
import com.mikront.util.debug.Log;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Parser {
    public static final Pattern REGEX_NEWLINE = Pattern.compile("[\r\n]+");
    private static final Pattern
            REGEX_ROOM = Pattern.compile(".*(\\d+\\..+)\\.ауд\\."),
            REGEX_LINK = Pattern.compile(".*(http\\S+).*"),
            REGEX_TEACHER1 = Pattern.compile("^ +(\\S+ \\S+ \\S+).*"),
            REGEX_TEACHER2 = Pattern.compile("^ +(\\S+) (\\S+ \\S+ \\S+).*"),
            REGEX_TEACHER3 = Pattern.compile("^ +\\((\\S+ \\S+)\\) (\\S+ \\S+ \\S+).*"),
            REGEX_TEACHERS_TITLE_TYPE = Pattern.compile("^Увага! Заміна! (.+) замість: (\\S+) (\\S+ \\S+ \\S+) (.+) \\((.+)\\)$"),
            REGEX_TITLE = Pattern.compile("^([^h\\s].+)$"),
            REGEX_TITLE_TYPE = Pattern.compile("^([^h\\s].+) \\((.+)\\)$"),
            REGEX_STREAM1 = Pattern.compile(".+Спец\\.потік.+\\((.*)\\)"),
            REGEX_STREAM2 = Pattern.compile(".+Потік\\s+(.*)"),
            REGEX_STREAM3 = Pattern.compile(".*Збірна група.+\\((.*)\\)"),
            REGEX_SUBGROUP1 = Pattern.compile(".*підгр.*(\\d).*");
    private static final String
            PREFIX_SUBGROUP2 = ".*група.*",
            SUFFIX_SUBGROUP2 = "\\.(\\d).+ \\(.*\\)";
    private static final String CACHE_INDENT = " ".repeat(13);

    private Document document;
    private Pattern regex_subgroup2;
    private String defaultGroup = "Наркомани-алкоголіки";
    private final Context context;


    private Parser(Context context) {
        this.context = context;
    }

    @Contract("_ -> new")
    public static @NotNull Parser init(Context context) {
        return new Parser(context);
    }


    public Parser setDocument(Document document) {
        this.document = document;
        return this;
    }

    public Parser setDefaultGroup(String group) {
        this.defaultGroup = group;
        return this;
    }

    private void prepare() {
        regex_subgroup2 = Pattern.compile(PREFIX_SUBGROUP2 + defaultGroup + SUFFIX_SUBGROUP2);
    }

    public List<Day> parse() {
        prepare();

        Elements htmlDays = document.body()
                .getElementsByClass("col-print-6");

        return parseDays(htmlDays);
    }

    private List<Day> parseDays(Elements blocks) {
        List<Day> days = new ArrayList<>();

        for (Element div : blocks) {
            String date = div.getElementsByTag("h4").get(0).text();
            Day day = new Day(date);
            days.add(day);

            for (Element tr : div.getElementsByTag("tr")) {
                Elements td = tr.getElementsByTag("td");

                Cell cell = parseCell(
                        Integer.parseInt(td.get(0).text()),
                        td.get(2));
                if (cell != null)
                    day.add(cell);
            }
        }

        return days;
    }

    private Cell parseCell(int number, Element block) {
        String wholeText = block.wholeText();

        //No info
        if (wholeText.isBlank())
            return null;

        List<String> lines = new ArrayList<>();
        for (String s : wholeText.split(REGEX_NEWLINE.pattern())) {
            if (s.isBlank()) continue;
            if (s.contains("дистанційно")) continue;
            if (s.contains("Лабораторна робота")) continue;
            if (s.contains("Лекція")) continue;
            if (s.contains("Meeting")) continue;

            //Multiword titles
            if (s.contains("ст. викладач"))
                s = s.replace("ст. викладач", "(старший викладач)");
            if (s.contains("старший викладач"))
                s = s.replace("старший викладач", "(старший викладач)");
            if (s.contains("зав. кафедрою"))
                s = s.replace("зав. кафедрою", "(завідувач кафедрою)");

            //Fix cache indentation
            if (s.contains(CACHE_INDENT))
                s = s.substring(CACHE_INDENT.length());

            //Get rid of asterisks
            int i;
            while ((i = s.indexOf('*')) != -1) { //Redundant asterisks found
                //Try to find both asterisk and space at the beginning of the string
                if (i == 0 && s.charAt(i + 1) == ' ')
                    s = s.replace("* ", "");
                else s = s.replace("*", "");
            }

            lines.add(s);
        }

        Log.v("Parser::parseCell: lines = [");
        lines.forEach(s -> Log.v("Parser::parseCell:     '" + s + "'"));
        Log.v("Parser::parseCell: ]");


        Cell cell = new Cell(number);
        Session current = null;

        for (String s : lines) {
            Session temp = trySearchingTitleToInitClass(s);
            if (temp != null) {
                current = temp;

                if (current.getTitle().contains("Увага! Заняття відмінено!")) {
                    Log.i("Parser::parseCell: found cancelled class");
                    Log.i("Parser::parseCell:   - s = " + s);
                    continue; //Not add to the cell but continue filling with info
                }

                cell.add(current);
                continue;
            }
            assert current != null;

            if (trySearchingLink(current, s)) continue;
            if (trySearchingSubgroup(current, s)) continue;
            if (trySearchingStreamGroup(current, s)) continue;
            if (trySearchingRoom(current, s)) {
                //If there's a room specified, try looking for a teacher
                trySearchingTeacher(current, s); //Result doesn't really matter; the line is processed otherwise
                continue;
            }
            if (trySearchingTeacher(current, s)) continue;

            Log.w("Parser::parseCell: passed checks = " + s);
        }

        return cell;
    }

    private Session trySearchingTitleToInitClass(String s) {
        //Get teachers, title, and type first
        Matcher matcher = REGEX_TEACHERS_TITLE_TYPE.matcher(s);
        if (matcher.matches()) {
            Log.v("Parser::trySearchingTitleToInitClass: teachers = " + s);
            Session current = new Session(context);
            current.setSubject(matcher.replaceAll("$4"));
            current.setType(matcher.replaceAll("$5"));
            current.setTeacherPosition(matcher.replaceAll("$2"));
            current.setTeacher(matcher.replaceAll("$3"));
            current.setTeacher2(matcher.replaceAll("$1"));
            return current;
        }

        //Get title and type first
        matcher = REGEX_TITLE_TYPE.matcher(s);
        if (matcher.matches()) {
            Log.v("Parser::trySearchingTitleToInitClass: title-type = " + s);
            Session current = new Session(context);
            current.setSubject(matcher.replaceAll("$1"));
            current.setType(matcher.replaceAll("$2"));
            return current;
        }

        //Get title first
        matcher = REGEX_TITLE.matcher(s);
        if (matcher.matches()) {
            Log.v("Parser::trySearchingTitleToInitClass: title = " + s);
            Session current = new Session(context);
            current.setSubject(matcher.replaceAll("$1"));
            return current;
        }
        return null;
    }

    private boolean trySearchingLink(@NotNull Session current, String s) {
        //Get link
        Matcher matcher = REGEX_LINK.matcher(s);
        if (matcher.matches()) {
            current.setLink(matcher.replaceAll("$1"));
            return true;
        }
        return false;
    }

    private boolean trySearchingStreamGroup(@NotNull Session current, String s) {
        //Get stream group
        for (Pattern p : List.of(REGEX_STREAM1, REGEX_STREAM2, REGEX_STREAM3)) {
            Matcher matcher = p.matcher(s);
            if (matcher.matches()) {
                Log.v("Parser::trySearchingStreamGroup: stream = " + s);
                current.setGroup(matcher.replaceAll("$1"));
                return true;
            }
        }
        return false;
    }

    private boolean trySearchingSubgroup(@NotNull Session current, String s) {
        //Get subgroup
        for (Pattern p : List.of(REGEX_SUBGROUP1, regex_subgroup2)) {
            Matcher matcher = p.matcher(s);
            if (matcher.matches()) {
                Log.v("Parser::trySearchingSubgroup: subgroup = " + s);
                current.setGroup(defaultGroup + "." + matcher.replaceAll("$1"));
                return true;
            }
        }
        return false;
    }

    private boolean trySearchingRoom(@NotNull Session current, String s) {
        //Get room
        Matcher matcher = REGEX_ROOM.matcher(s);
        if (matcher.matches()) {
            Log.v("Parser::trySearchingRoom: room = " + s);
            current.setRoom(matcher.replaceAll("$1"));
            return true;
        }
        return false;
    }

    private boolean trySearchingTeacher(@NotNull Session current, String s) {
        //Get teacher with position
        for (Pattern p : List.of(REGEX_TEACHER3, REGEX_TEACHER2)) {
            Matcher matcher = p.matcher(s);
            if (matcher.matches()) {
                Log.v("Parser::trySearchingTeacher: teacher = " + s);
                current.setTeacherPosition(matcher.replaceAll("$1"));
                current.setTeacher(matcher.replaceAll("$2"));
                return true;
            }
        }
        //Get teacher without position
        Matcher matcher = REGEX_TEACHER1.matcher(s);
        if (matcher.matches()) {
            Log.v("Parser::trySearchingTeacher: teacher = " + s);
            current.setTeacher(matcher.replaceAll("$1"));
            return true;
        }
        return false;
    }
}