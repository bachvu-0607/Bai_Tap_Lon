package com.uet.domain.exceptions;

/**
 * Ngoại lệ xảy ra khi một giao dịch tài chính không hợp lệ.
 * Được ném ra trong các trường hợp: 
 * - Số tiền giao dịch không hợp lệ (ví dụ: nhỏ hơn hoặc bằng 0)
 * - Giới hạn giá tự động đặt (auto-bid) không hợp lệ
 * - Các thao tác tạm giữ, hoàn trả, thanh toán với số tiền sai
 */
public class InvalidTransactionException extends Exception {
    public InvalidTransactionException(String message) {
        super(message);
    }   
}
