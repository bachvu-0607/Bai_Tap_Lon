package com.uet.domain.result;

import java.io.Serializable;

public class ProductPostResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;

    private ProductPostResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ProductPostResult success(String message) {
        return new ProductPostResult(true, message);
    }

    public static ProductPostResult failed(String message) {
        return new ProductPostResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
