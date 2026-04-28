package com.uet.domain.entity;

import com.uet.domain.exceptions.InvalidDepositException;

/**
 * Interface cho các đối tượng có khả năng quản lý tài chính.
 */
public interface Payable {
    //Nạp tiền vào ví
    void deposit(double amount) throws InvalidDepositException;
    //Tổng số tiền trong ví (bao gồm cả tiền đang tạm giữ)
    double getBalance();
    //Tổng số tiền có thể sử dụng (trừ tiền đang tạm giữ)
    double getAvailableBalance();
}
