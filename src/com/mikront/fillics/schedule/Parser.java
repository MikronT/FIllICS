package com.mikront.fillics.schedule;

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
            REGEX_AUDITORY = Pattern.compile(".*(\\d+\\.\\d+)\\.ауд\\."),
            REGEX_LINK = Pattern.compile(".*(http\\S+).*"),
            REGEX_TEACHER = Pattern.compile("^ (\\S+) (\\S+ \\S+ \\S+).*"),
            REGEX_TEACHER2_TEACHER_TITLE_TYPE = Pattern.compile("^Увага! Заміна! (.+) замість: (\\S+) (\\S+ \\S+ \\S+) (.+) \\((.+)\\)$"),
            REGEX_TITLE = Pattern.compile("^([^h\\s].+)$"),
            REGEX_TITLE_TYPE = Pattern.compile("^([^h\\s].+) \\((.+)\\)$"),
            REGEX_STREAM1 = Pattern.compile(".+Спец\\.потік.+\\((.*)\\)"),
            REGEX_STREAM2 = Pattern.compile(".+Потік\\s+(.*)"),
            REGEX_STREAM3 = Pattern.compile(".*Збірна група.+\\((.*)\\)"),
            REGEX_SUBGROUP1 = Pattern.compile(".*підгр.*(\\d).*");
    private static final String
            PREFIX_SUBGROUP2 = ".*група.*",
            SUFFIX_SUBGROUP2 = "\\.(\\d).+ \\(.*\\)";

    private Pattern regex_subgroup2;
    private String defaultGroup = "Норкомани-алкоголіки";
    private final Document document;


    private Parser(Document document) {
        this.document = document;
    }

    @Contract("_ -> new")
    public static @NotNull Parser of(Document document) {
        return new Parser(document);
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
                .getElementsByClass("col-md-6");

        return parseDays(htmlDays);
    }

    private List<Day> parseDays(Elements blocks) {
        List<Day> days = new ArrayList<>();

        for (Element div : blocks) {
            if (div.hasClass("col-xs-12"))
                continue;

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
            if (s.contains("Meeting")) continue;

            if (s.contains("ст. викладач")) s = s.replace("ст. викладач", "старший викладач");

            //Get rid of asterisks
            int index = s.indexOf('*');
            if (index != -1) //Found!
                if (index == 0) {
                    index = s.indexOf("* "); //Check with space
                    if (index != -1)
                        s = s.replace("* ", "");
                    else s = s.replace("*", "");
                } else s = s.replace("*", "");

            lines.add(s);
        }

        Log.v("Parser::parseCell: lines = [");
        lines.forEach(s -> Log.v("Parser::parseCell:     '" + s + "'"));
        Log.v("Parser::parseCell: ]");


        Cell cell = new Cell(number);
        Session current = null;

        for (String s : lines) {
            Session temp;
            if ((temp = trySearchingTitleToInitClass(s)) != null) {
                current = temp;
                cell.add(current);
                continue;
            }
            assert current != null;

            if (trySearchingLink(current, s)) continue;
            if (trySearchingSubgroup(current, s)) continue;
            if (trySearchingStreamGroup(current, s)) continue;
            if (trySearchingAuditory(current, s)) {
                //If there's an auditory specified, try looking for a teacher
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
        Matcher matcher = REGEX_TEACHER2_TEACHER_TITLE_TYPE.matcher(s);
        if (matcher.matches()) {
            Log.v("Parser::trySearchingTitleToInitClass: teachers = " + s);
            Session current = new Session();
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
            Session current = new Session();
            current.setSubject(matcher.replaceAll("$1"));
            current.setType(matcher.replaceAll("$2"));
            return current;
        }

        //Get title first
        matcher = REGEX_TITLE.matcher(s);
        if (matcher.matches()) {
            Log.v("Parser::trySearchingTitleToInitClass: title = " + s);
            Session current = new Session();
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

    private boolean trySearchingSubgroup(Session current, String s) {
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

    private boolean trySearchingAuditory(Session current, String s) {
        //Get auditory
        Matcher matcher = REGEX_AUDITORY.matcher(s);
        if (matcher.matches()) {
            Log.v("Parser::trySearchingAuditory: auditory = " + s);
            current.setAuditory(matcher.replaceAll("$1"));
            return true;
        }
        return false;
    }

    private boolean trySearchingTeacher(Session current, String s) {
        //Get teacher
        Matcher matcher = REGEX_TEACHER.matcher(s);
        if (matcher.matches()) {
            Log.v("Parser::trySearchingTeacher: teacher = " + s);
            current.setTeacherPosition(matcher.replaceAll("$1"));
            current.setTeacher(matcher.replaceAll("$2"));
            return true;
        }
        return false;
    }
}