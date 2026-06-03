package com.uet.domain.result;

import java.io.Serializable;

public class UserActionResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;

    private UserActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static UserActionResult success(String message) {
        return new UserActionResult(true, message);
    }

    public static UserActionResult failed(String message) {
        return new UserActionResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
