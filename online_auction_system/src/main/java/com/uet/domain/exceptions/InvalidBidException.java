package com.uet.domain.exceptions;

/**
 * Ngoại lệ xảy ra khi người dùng đặt mức giá không hợp lệ.
 */
public class InvalidBidException extends Exception {
    public InvalidBidException(String message) {
        super(message);
    }
}

