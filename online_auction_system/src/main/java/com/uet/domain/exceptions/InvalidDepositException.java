package com.uet.domain.exceptions;

/**
 * Ngoại lệ xảy ra khi có lỗi trong quá trình nạp tiền.
 * Được ném ra trong các trường hợp: 
 * - Số tiền nạp vào không hợp lệ (ví dụ: nhỏ hơn hoặc bằng 0)
 */
public class InvalidDepositException extends Exception {
    public InvalidDepositException(String message) {
        super(message);
    }
}