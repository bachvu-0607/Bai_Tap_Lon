package com.uet.domain.entity;

import java.util.ArrayList;
import java.util.List;
import com.uet.domain.exceptions.InvalidDepositException;

public class Seller extends User implements Payable {
    private List<Item> soldProducts = new ArrayList<>();
    private double balance;

    public Seller(String userName, String password, String id) {
        super(userName, password, id);
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

    //Thêm sản phẩm đã bán
    public void addSoldProduct(Item item) {
        this.soldProducts.add(item);
    }

    //Lấy danh sách sản phẩm đã bán
    public List<Item> getSoldProducts() { return this.soldProducts; }

    @Override
    public String toString() {
        return "Seller: " + getUserName() + " (ID: " + getId() + ") - Balance: " + balance + "$";
    }
}