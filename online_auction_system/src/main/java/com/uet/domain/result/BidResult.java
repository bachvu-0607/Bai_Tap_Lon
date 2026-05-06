package com.uet.domain.result;

import java.io.Serializable;

public class BidResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;

    private BidResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static BidResult success(String message) {
        return new BidResult(true, message);
    }

    public static BidResult failed(String message) {
        return new BidResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
