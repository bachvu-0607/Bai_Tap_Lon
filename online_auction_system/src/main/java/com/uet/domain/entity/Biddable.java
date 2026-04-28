package com.uet.domain.entity;

import com.uet.domain.exceptions.InsufficientBalanceException;
import com.uet.domain.exceptions.InvalidTransactionException;

/**
 * Interface cho các đối tượng có khả năng tham gia đấu giá.
 * Định nghĩa các hành vi liên quan đến quản lý tiền đặt giá.
 */
public interface Biddable {
    // Tạm giữ tiền cho lượt đặt giá
    void lockFunds(double amount) throws InvalidTransactionException, InsufficientBalanceException;
    // Hoàn trả lại số tiền đã tạm giữ
    void unlockFunds(double amount) throws InvalidTransactionException, InsufficientBalanceException;
    // Xác nhận thanh toán số tiền đã tạm giữ
    void commitPayment(double amount) throws InvalidTransactionException, InsufficientBalanceException;
    // Kiểm tra khả năng chi trả
    boolean canAfford(double amount);
}
