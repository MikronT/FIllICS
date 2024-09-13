package com.mikront.fillics.schedule;

import com.mikront.util.Concat;
import com.mikront.util.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.NodeFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;


public class Schedule {
    private static final Logger log = LogManager.getLogger();

    public static final File CACHE_DIR = new File("cache");
    public static final LocalDate DATE_FROM;
    public static final LocalDate DATE_TO;
    private static final Charset CHARSET = Charset.forName("windows-1251");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String URL = "https://dekanat.nung.edu.ua/cgi-bin/timetable.cgi";


    static {
        var now = LocalDate.now();

        var from = now.withMonth(8).withDayOfMonth(1);
        if (from.isAfter(now))
            from = from.minusYears(1);

        var to = from.plusYears(1);

        DATE_FROM = from;
        DATE_TO = to;
        log.info("Setting date range from '{}' to '{}'", from, to);
    }


    public static List<String> getTeachers() {
        Document doc;
        try {
            doc = Jsoup.connect(URL + "?n=701&lev=141").get();
        } catch (IOException e) {
            log.error("Unable to get the teachers list", e);
            return Collections.emptyList();
        }

        JSONObject root = new JSONObject(doc.wholeText());
        return root.getJSONArray("suggestions")
                .toList()
                .stream()
                .map(Object::toString)
                .filter(s -> !s.contains("!")) //Get rid of fired teachers
                .filter(s -> !s.contains("Вакансія")) //Get rid of vacancies
                //FIXME 2024-09-11: Potentially problematic
                //.map(s -> s.replace("*", "")) //Get rid of asterisks
                .sorted(Utils.HOME_COLLATOR)
                .toList();
    }

    public static List<String> getGroups() {
        Document doc;
        try {
            doc = Jsoup.connect(URL + "?n=701&lev=142").get();
        } catch (IOException e) {
            log.error("Unable to get the groups list", e);
            return Collections.emptyList();
        }

        var root = new JSONObject(doc.wholeText());
        return root.getJSONArray("suggestions")
                .toList()
                .stream()
                .map(Object::toString)
                .sorted(Utils.HOME_COLLATOR)
                .toList();
    }

    @Nullable
    public static Document getSchedule(String teacher, String group) {
        var connection = Jsoup.connect(URL + "?n=700")
                .data("sdate", DATE_FROM.format(FORMATTER))
                .data("edate", DATE_TO.format(FORMATTER));

        String criteria = null;
        if (Utils.notEmpty(teacher)) {
            connection = connection.data("teacher", teacher);
            criteria = teacher;
        } else if (Utils.notEmpty(group)) {
            connection = connection.data("group", group);
            criteria = group;
        }

        Document doc;
        try {
            doc = connection
                    .postDataCharset(CHARSET.name())
                    .post();
        } catch (IOException e) {
            log.error("Unable to get the requested schedule", e);
            return null;
        }

        //Try saving requested page for future offline use
        var cacheFile = new File(Concat.me()
                .word(CACHE_DIR)
                .word(File.separatorChar)
                .word(DATE_FROM.getYear()).word("-").word(DATE_TO.getYear())
                .word("_")
                .when(Utils.notEmpty(criteria))
                .word(criteria)
                .word("_")
                .then()
                .word(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .word(".html")
                .enate());

        if (!CACHE_DIR.exists() && !CACHE_DIR.mkdirs()) {
            log.warn("Can't create cache dir '{}'", CACHE_DIR.getAbsolutePath());
        }

        try (var stream = new FileOutputStream(cacheFile)) {
            stream.write(doc
                    .filter((node, _) ->
                            node.toString().contains("charset") ?
                                    NodeFilter.FilterResult.REMOVE :
                                    NodeFilter.FilterResult.CONTINUE)
                    .outerHtml()
                    .getBytes(CHARSET));
        } catch (IOException e) {
            log.warn("Unable to save cache file '{}'", cacheFile, e);
        }

        return doc;
    }
}