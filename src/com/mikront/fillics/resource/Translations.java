package com.mikront.fillics.resource;

public class Translations {
    public static final String EN = "en";
    public static final String UK = "uk";


    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public static String getTranslation(String lang, Strings id) {
        return switch (id) {
            case APP_NAME -> "FIllICS";

            case PANEL_REQUEST -> switch (lang) {
                default -> "Request schedule from IFNTUOG server";
                case UK -> "Отримати розклад із сервера ІФНТУНГ";
            };
            case LABEL_TEACHER -> switch (lang) {
                default -> "Teacher";
                case UK -> "Викладач";
            };
            case LABEL_GROUP -> switch (lang) {
                default -> "Group";
                case UK -> "Група";
            };
            case LABEL_DATE_FROM -> switch (lang) {
                default -> "From date";
                case UK -> "Від дати";
            };
            case LABEL_DATE_TO -> switch (lang) {
                default -> "To date";
                case UK -> "По дату";
            };
            case LABEL_TYPES -> switch (lang) {
                default -> "Session types";
                case UK -> "Типи занять";
            };
            case LABEL_SUBJECTS -> switch (lang) {
                default -> "Subjects";
                case UK -> "Предмети";
            };
            case LABEL_GROUPS -> switch (lang) {
                default -> "Student groups";
                case UK -> "Студентські групи";
            };
            case BUTTON_REQUEST -> switch (lang) {
                default -> "Request";
                case UK -> "Отримати";
            };
            case BUTTON_EXPORT -> switch (lang) {
                default -> "Export";
                case UK -> "Експортувати";
            };

            case UNKNOWN_TYPE -> switch (lang) {
                default -> "Unknown session type";
                case UK -> "Невідомий тип заняття";
            };
            case UNKNOWN_GROUP -> switch (lang) {
                default -> "Unknown group";
                case UK -> "Невідома група";
            };

            case STEP_READY -> switch (lang) {
                default -> "Ready";
                case UK -> "Готово";
            };
            case STEP_GETTING_TEACHERS -> switch (lang) {
                default -> "Getting teachers";
                case UK -> "Отримання викладачів";
            };
            case STEP_GETTING_GROUPS -> switch (lang) {
                default -> "Getting groups";
                case UK -> "Отримання груп";
            };
            case STEP_GETTING_SCHEDULE -> switch (lang) {
                default -> "Getting schedule";
                case UK -> "Отримання розкладу";
            };
            case STEP_COMPILING_DATA -> switch (lang) {
                default -> "Compiling data";
                case UK -> "Компіляція даних";
            };
        };
    }
}