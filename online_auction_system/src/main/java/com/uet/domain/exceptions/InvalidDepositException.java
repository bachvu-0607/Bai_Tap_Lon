package com.uet.domain.exceptions;

/**
 * Ngoại lệ xảy ra khi có lỗi trong quá trình nạp tiền.
 * Được ném ra trong các trường hợp: 
 * - Số tiền nạp vào không hợp lệ (ví dụ: nhỏ hơn hoặc bằng 0)
 */
public class InvalidDepositException extends Exception {
    
    /**
     * Khởi tạo ngoại lệ liên quan đến lỗi nạp tiền vào ví.
     * 
     * @param message Chuỗi thông báo lỗi chi tiết.
     */
    public InvalidDepositException(String message) {
        super(message);
    }
}