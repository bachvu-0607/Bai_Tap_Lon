package com.uet.domain.exceptions;

/**
 * Ngoại lệ xảy ra khi số dư của người dùng không đủ để thực hiện thao tác.
 * Được ném ra trong các trường hợp:
 * - Số dư khả dụng không đủ để tạm giữ (lock) cho lượt đặt giá mới
 * - Giới hạn auto-bid vượt quá số dư khả dụng hiện tại
 * - Yêu cầu hoàn trả (unlock) vượt quá số tiền đang bị tạm giữ
 * - Số tiền thanh toán vượt quá số tiền đã tạm giữ
 */
public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
