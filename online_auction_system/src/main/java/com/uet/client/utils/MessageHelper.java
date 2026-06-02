package com.uet.client.utils;

import javafx.scene.control.Label;

public final class MessageHelper {
    private static final String INFO_CLASS = "message-info";
    private static final String SUCCESS_CLASS = "message-success";
    private static final String ERROR_CLASS = "message-error";

    private MessageHelper() {
    }

    public static void info(Label label, String message) {
        set(label, message, INFO_CLASS);
    }

    public static void success(Label label, String message) {
        set(label, message, SUCCESS_CLASS);
    }

    public static void error(Label label, String message) {
        set(label, message, ERROR_CLASS);
    }

    public static void clear(Label label) {
        set(label, "", null);
    }

    private static void set(Label label, String message, String styleClass) {
        if (label == null) {
            return;
        }

        label.setText(message == null ? "" : message);
        label.getStyleClass().removeAll(INFO_CLASS, SUCCESS_CLASS, ERROR_CLASS);
        if (styleClass != null) {
            label.getStyleClass().add(styleClass);
        }
    }
}
