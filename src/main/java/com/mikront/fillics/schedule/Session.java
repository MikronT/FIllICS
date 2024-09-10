package com.mikront.fillics.schedule;

import com.mikront.fillics.ics.Event;
import com.mikront.util.Concat;
import com.mikront.util.Utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Function;


public class Session {
    private String subject, type, group, teacher, teacher_position, teacher2, room, link;


    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setGroup(String group) {
        this.group = group;
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

    public void setRoom(String room) {
        this.room = room;
    }

    public void setLink(String link) {
        this.link = link;
    }


    public String getTitle() {
        return Concat.me()
                .word(subject)
                .when(Utils.notEmpty(type))
                .words(" (", type, ")")
                .enate();
    }

    public String getDescription() {
        return Concat.me()
                .when(Utils.notEmpty(group))
                .line(group)
                .then()

                .when(Utils.notEmpty(teacher2))
                .line(teacher2).words(" замість ", teacher)
                .otherwise(Utils.notEmpty(teacher))
                .line(teacher)
                .then()
                .when(Utils.notEmpty(teacher_position))
                .words(" (", teacher_position, ")")
                .then()

                .when(Utils.notEmpty(room))
                .line("Аудиторія ").word(room)
                .then()

                .when(Utils.notEmpty(link))
                .line(link)
                .enate();
    }

    public String getSubject() {
        return Utils.isEmpty(subject) ? "" : subject;
    }

    public String getType() {
        return Utils.isEmpty(type) ? "" : type;
    }

    public String getGroup() {
        return Utils.isEmpty(group) ? "" : group;
    }

    public boolean isOptional() {
        return subject.contains("(в)");
    }

    public Event toEvent(Day day, Cell cell) {
        return toEvent(day, cell, Session::getTitle, Session::getDescription);
    }

    public Event toEvent(Day day, Cell cell,
                         Function<Session, String> titleProvider,
                         Function<Session, String> descriptionProvider) {
        LocalDate date = day.getDate();
        LocalTime time = cell.getTime();
        LocalDateTime from = LocalDateTime.of(date, time);
        LocalDateTime to = from.plusMinutes(Cell.DURATION_DEFAULT_PAIR);

        return Event.begin()
                .setTimeFrom(from)
                .setTimeTo(to)
                .setTitle(titleProvider.apply(this))
                .setDescription(descriptionProvider.apply(this));
    }

    @Override
    public String toString() {
        if (Utils.isEmpty(subject))
            return "";

        return Concat.me().lines(getTitle(), getDescription()).enate();
    }
}