package com.mikront.fillics.schedule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger log = LogManager.getLogger();

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
            REGEX_STREAM1 = Pattern.compile(".*[Пп]отік.+\\((.*)\\)"),
            REGEX_STREAM2 = Pattern.compile(".*[Гг]рупа.+\\((.*)\\)"),
            REGEX_SUBGROUP1 = Pattern.compile(".*[Пп]ідгр.*(\\d).*");
    private static final String
            PREFIX_SUBGROUP2 = ".*група.*",
            SUFFIX_SUBGROUP2 = "\\.(\\d).+ \\(.*\\)";
    private static final String CACHE_INDENT = " ".repeat(13);

    private Document document;
    private Pattern regex_subgroup2;
    private String defaultGroup = "Наркомани-алкоголіки";


    private Parser() {}

    @Contract(" -> new")
    public static @NotNull Parser init() {
        return new Parser();
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
            String date = div.getElementsByTag("h4").getFirst().text();
            Day day = new Day(date);
            days.add(day);

            for (Element tr : div.getElementsByTag("tr")) {
                Elements td = tr.getElementsByTag("td");

                Row row = parseRow(
                        Integer.parseInt(td.getFirst().text()),
                        td.get(2));
                if (row != null)
                    day.add(row);
            }
        }

        return days;
    }

    private Row parseRow(int number, Element block) {
        String wholeText = block.wholeText();

        var blockChildren = block.getElementsByAttribute("href");
        String link = "";
        if (!blockChildren.isEmpty())
            link = blockChildren.getFirst().attribute("href").getValue();

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

        log.trace("Reading row lines '{}'", lines);


        Row row = new Row(number);
        Session current = null;

        for (String s : lines) {
            Session temp = trySearchingTitleToInitClass(s);
            if (temp != null) {
                current = temp;

                if (current.getTitle().contains("Увага! Заняття відмінено!")) {
                    log.info("Found cancelled class '{}'", s);
                    continue; //Not add to the row but continue filling with info
                }

                row.add(current);
                continue;
            }
            assert current != null;

            if (trySearchingLink(current, s)) {
                current.setLink(link);
                continue;
            }
            if (trySearchingSubgroup(current, s)) continue;
            if (trySearchingStreamGroup(current, s)) continue;
            if (trySearchingRoom(current, s)) {
                //If there's a room specified, try looking for a teacher
                trySearchingTeacher(current, s); //Result doesn't really matter; the line is processed otherwise
                continue;
            }
            if (trySearchingTeacher(current, s)) continue;

            log.warn("Entry '{}' was able to pass all checks", s);
        }

        return row;
    }

    private Session trySearchingTitleToInitClass(String s) {
        //Get teachers, title, and type first
        Matcher matcher = REGEX_TEACHERS_TITLE_TYPE.matcher(s);
        if (matcher.matches()) {
            log.trace("Matched teachers '{}'", s);
            var current = new Session();
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
            log.trace("Matched title-type '{}'", s);
            var current = new Session();
            current.setSubject(matcher.replaceAll("$1"));
            current.setType(matcher.replaceAll("$2"));
            return current;
        }

        //Get title first
        matcher = REGEX_TITLE.matcher(s);
        if (matcher.matches()) {
            log.trace("Matched title '{}'", s);
            var current = new Session();
            current.setSubject(matcher.replaceAll("$1"));
            return current;
        }
        return null;
    }

    @SuppressWarnings("unused")
    private boolean trySearchingLink(@NotNull Session current, String s) {
        //Get link
        Matcher matcher = REGEX_LINK.matcher(s);
        if (matcher.matches()) {
            log.trace("Matched link '{}'", s);
            //current.setLink(matcher.replaceAll("$1"));
            return true;
        }
        return false;
    }

    private boolean trySearchingStreamGroup(@NotNull Session current, String s) {
        //Get stream group
        for (Pattern p : List.of(REGEX_STREAM1, REGEX_STREAM2)) {
            Matcher matcher = p.matcher(s);
            if (matcher.matches()) {
                log.trace("Matched stream '{}'", s);
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
                log.trace("Matched subgroup '{}'", s);
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
            log.trace("Matched room '{}'", s);
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
                log.trace("Matched teacher-position '{}'", s);
                current.setTeacherPosition(matcher.replaceAll("$1"));
                current.setTeacher(matcher.replaceAll("$2"));
                return true;
            }
        }
        //Get teacher without position
        Matcher matcher = REGEX_TEACHER1.matcher(s);
        if (matcher.matches()) {
            log.trace("Matched teacher '{}'", s);
            current.setTeacher(matcher.replaceAll("$1"));
            return true;
        }
        return false;
    }
}
