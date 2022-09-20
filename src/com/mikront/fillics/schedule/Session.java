package com.mikront.fillics.schedule;

import com.mikront.fillics.ics.Event;
import com.mikront.fillics.util.U;

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
        StringBuilder builder = new StringBuilder()
                .append(subject);

        if (U.notEmpty(type))
            builder.append(" (").append(type).append(")");

        return builder.toString();
    }

    private String getDescription() {
        StringBuilder builder = new StringBuilder();

        if (U.notEmpty(group))
            builder.append(group);

        if (U.notEmpty(teacher2))
            U.NL(builder).append(teacher2).append(" замість ").append(teacher);
        else if (U.notEmpty(teacher))
            U.NL(builder).append(teacher).append(" (").append(teacher_position).append(")");

        if (U.notEmpty(auditory))
            U.NL(builder).append("Аудиторія ").append(auditory);

        if (U.notEmpty(link))
            U.NL(builder).append(link);

        return builder.toString();
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
        if (U.isEmpty(subject))
            return "";

        return getTitle() + U.NL + getDescription();
    }
}