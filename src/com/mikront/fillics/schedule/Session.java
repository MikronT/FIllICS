package com.mikront.fillics.schedule;

import com.mikront.fillics.ics.Event;
import com.mikront.util.Concat;
import com.mikront.util.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


public class Session {
    private String subject, type, group, teacher, teacher_position, teacher2, auditory, link;


    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubject() {
        return subject == null ? "" : subject;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group == null ? "" : group;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public void setTeacherPosition(String position) {
        this.teacher_position = position;
    }

    public void setTeacher2(String teacher2) {
        this.teacher2 = teacher2;
    }

    public void setAuditory(String auditory) {
        this.auditory = auditory;
    }

    public void setLink(String link) {
        this.link = link;
    }


    private String getTitle() {
        return Concat.me()
                .word(subject)
                .when(Utils.notEmpty(type))
                .words(" (", type, ")")
                .enate();
    }

    private String getDescription() {
        return Concat.me()
                .when(Utils.notEmpty(group))
                .line(group)
                .then()

                .when(Utils.notEmpty(teacher2))
                .line(teacher2).words(" замість ", teacher)
                .otherwise(Utils.notEmpty(teacher))
                .line(teacher).words(" (", teacher_position, ")")
                .then()

                .when(Utils.notEmpty(auditory))
                .line("Аудиторія ").word(auditory)
                .then()

                .when(Utils.notEmpty(link))
                .line(link)
                .enate();
    }

    public boolean isOptional() {
        return subject.contains("(в)");
    }

    public Event toEvent(Day day, Cell cell) {
        LocalDate date = day.getDate();
        LocalTime time = cell.getTime();
        LocalDateTime from = LocalDateTime.of(date, time);
        LocalDateTime to = from.plusMinutes(Cell.DURATION_DEFAULT_MINUTES);

        return Event.begin()
                .setTimeFrom(from)
                .setTimeTo(to)
                .setTitle(getTitle())
                .setDescription(getDescription());
    }

    @Override
    public String toString() {
        if (Utils.isEmpty(subject))
            return "";

        return Concat.me().lines(getTitle(), getDescription()).enate();
    }
}