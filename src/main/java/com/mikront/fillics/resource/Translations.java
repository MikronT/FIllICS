package com.mikront.fillics.resource;

public class Translations {
    public static final String EN = "en";
    public static final String UK = "uk";


    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public static String getTranslation(String lang, Strings id) {
        return switch (id) {
            case APP_NAME -> "FIllICS";

            case PANEL_PERIOD -> switch (lang) {
                case UK -> "Встановіть часові рамки розкладу";
                default -> "Adjust schedule period";
            };
            case LABEL_DATE_FROM -> switch (lang) {
                case UK -> "Від дати";
                default -> "From date";
            };
            case LABEL_DATE_TO -> switch (lang) {
                case UK -> "По дату";
                default -> "To date";
            };
            case PANEL_REQUEST -> switch (lang) {
                case UK -> "Отримайте розклад із серверів ІФНТУНГ";
                default -> "Request schedule from IFNTUOG servers";
            };
            case LABEL_TEACHER -> switch (lang) {
                case UK -> "Викладач";
                default -> "Teacher";
            };
            case LABEL_GROUP -> switch (lang) {
                case UK -> "Група";
                default -> "Group";
            };
            case BUTTON_REQUEST -> switch (lang) {
                case UK -> "Отримати";
                default -> "Request";
            };
            case PANEL_IMPORT -> switch (lang) {
                case UK -> "Імпортуйте завантажену сторінку розкладу";
                default -> "Import downloaded schedule webpage";
            };
            case LABEL_PLACEHOLDER_GROUP -> switch (lang) {
                case UK -> "Назва групи";
                default -> "Your group name";
            };
            case BUTTON_OPEN -> switch (lang) {
                case UK -> "Вибрати файл";
                default -> "Select file";
            };
            case PANEL_TYPES -> switch (lang) {
                case UK -> "Виберіть необхідні типи занять";
                default -> "Filter session types";
            };
            case PANEL_SUBJECTS -> switch (lang) {
                case UK -> "Оберіть свої дисципліни";
                default -> "Choose your subjects";
            };
            case PANEL_GROUPS -> switch (lang) {
                case UK -> "Позначте групи, до яких належите";
                default -> "Select student groups you belong to";
            };
            case BUTTON_EXPORT -> switch (lang) {
                case UK -> "Експортувати";
                default -> "Export";
            };

            case UNKNOWN_TYPE -> switch (lang) {
                case UK -> "Невідомий тип заняття";
                default -> "Unknown session type";
            };
            case UNKNOWN_GROUP -> switch (lang) {
                case UK -> "Невідома група";
                default -> "Unknown group";
            };

            case STEP_READY -> switch (lang) {
                case UK -> "Готово";
                default -> "Ready";
            };
            case STEP_GETTING_TEACHERS -> switch (lang) {
                case UK -> "Отримання викладачів";
                default -> "Getting teachers";
            };
            case STEP_GETTING_GROUPS -> switch (lang) {
                case UK -> "Отримання груп";
                default -> "Getting groups";
            };
            case STEP_GETTING_SCHEDULE -> switch (lang) {
                case UK -> "Отримання розкладу";
                default -> "Getting schedule";
            };
            case STEP_COMPILING_DATA -> switch (lang) {
                case UK -> "Компіляція даних";
                default -> "Compiling data";
            };
        };
    }
}