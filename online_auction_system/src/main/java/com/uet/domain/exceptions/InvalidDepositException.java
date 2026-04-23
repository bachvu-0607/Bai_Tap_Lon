package com.uet.domain.exceptions;

/**
 * Ngoại lệ xảy ra khi có lỗi trong quá trình nạp tiền.
 */
public class InvalidDepositException extends Exception {
    public InvalidDepositException(String message) {
        super(message);
    }
}