package com.uet.domain.entity.user;

import com.uet.domain.contract.Payable;
import com.uet.domain.exceptions.InvalidDepositException;

public class Seller extends User implements Payable {
    private double balance;

    public Seller(String citizenId, String name, String phoneNumber, String password, String address) {
        super(citizenId, name, phoneNumber, password, address);
        this.balance = 0;
    }

    public Seller(String id, String citizenId, String name, String phoneNumber, String password, String address) {
        super(id, citizenId, name, phoneNumber, password, address);
        this.balance = 0;
    }

    //Nạp tiền vào ví (VD: nhận tiền từ phiên đấu giá thành công)
    @Override
    public void deposit(double amount) throws InvalidDepositException {
        if (amount <= 0) {
            throw new InvalidDepositException("Số tiền nạp phải lớn hơn 0!");
        }
        this.balance += amount;
    }

    @Override
    public double getBalance() { return this.balance; }

    @Override
    public double getAvailableBalance() { return this.balance; }

    @Override
    public String toString() {
        return "Seller: " + getUserName() + " (ID: " + getId() + ") - Balance: " + balance + "$";
    }
}
