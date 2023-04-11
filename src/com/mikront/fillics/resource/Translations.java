package com.mikront.fillics.resource;

public class Translations {
    public static final String EN = "en";
    public static final String UK = "uk";


    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public static String getTranslation(String lang, Strings id) {
        return switch (id) {
            case APP_NAME -> "FIllICS";

            case PANEL_REQUEST -> switch (lang) {
                default -> "Request schedule from IFNTUOG servers";
                case UK -> "Отримайте розклад із серверів ІФНТУНГ";
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
            case BUTTON_REQUEST -> switch (lang) {
                default -> "Request";
                case UK -> "Отримати";
            };
            case PANEL_IMPORT -> switch (lang) {
                default -> "Import downloaded schedule webpage";
                case UK -> "Імпортуйте завантажену сторінку розкладу";
            };
            case LABEL_PLACEHOLDER_GROUP -> switch (lang) {
                default -> "Your group name";
                case UK -> "Назва групи";
            };
            case BUTTON_OPEN -> switch (lang) {
                default -> "Select file";
                case UK -> "Вибрати файл";
            };
            case PANEL_TYPES -> switch (lang) {
                default -> "Filter session types";
                case UK -> "Виберіть необхідні типи занять";
            };
            case PANEL_SUBJECTS -> switch (lang) {
                default -> "Choose your subjects";
                case UK -> "Оберіть свої дисципліни";
            };
            case PANEL_GROUPS -> switch (lang) {
                default -> "Select student groups you belong to";
                case UK -> "Позначте групи, до яких належите";
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