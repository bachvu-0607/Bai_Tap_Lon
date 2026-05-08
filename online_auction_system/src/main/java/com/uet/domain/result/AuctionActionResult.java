package com.uet.domain.result;

import java.io.Serializable;

public class AuctionActionResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;

    private AuctionActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static AuctionActionResult success(String message) {
        return new AuctionActionResult(true, message);
    }

    public static AuctionActionResult failed(String message) {
        return new AuctionActionResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
