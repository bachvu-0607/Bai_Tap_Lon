package com.uet.domain.result;

import java.io.Serializable;

import com.uet.domain.entity.user.User;

public class AuthenticationResult implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
    public static final String ALREADY_LOGGED_IN = "ALREADY_LOGGED_IN";
    public static final String EXISTED_CITIZEN_ID = "EXISTED_CITIZEN_ID";
    public static final String EXIST_PHONE = "EXIST_PHONE";
    public static final String SERVER_ERROR = "SERVER_ERROR";

    private final boolean success;
    private final User user;
    private final String errorCode;

    private AuthenticationResult(boolean success, User user, String errorCode) {
        this.success = success;
        this.user = user;
        this.errorCode = errorCode;
    }

    public static AuthenticationResult success(User user) {
        return new AuthenticationResult(true, user, null);
    }

    public static AuthenticationResult success() {
        return new AuthenticationResult(true, null, null);
    }

    public static AuthenticationResult failed(String errorCode) {
        return new AuthenticationResult(false, null, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public User getUser() {
        return user;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
