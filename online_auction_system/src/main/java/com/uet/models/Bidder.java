package com.uet.models;

import java.util.ArrayList;

public class Bidder extends User{
    private double Balance;
    private ArrayList<Item> Placed_Products = new ArrayList<Item>();
    
    public Bidder() {}
    public Bidder(String Name, String Phone_Number,  String ID, String Password, String Address) {
        super(Name,Phone_Number, ID, Password, Address);
        this.Balance = 0;
        System.out.println("Account created successfully!");
    }

    /*
    double getBalance(){return this.Balance;}
    
    // Nạp tiền
    public void deposit(double amount){
        this.Balance += amount;
        System.out.println("Deposit successfully");
    }
    
    // Ra giá
    public boolean placeBid(Auction a, double bidAmount) {
        // Kiểm tra tiền trong ví
        if(this.Balance < bidAmount) {
            System.out.println("You don't have enough money in your balance");
            return false;
        }
        
        // Kiểm tra giá đặt có lớn hơn giá cao nhất hiện tại không
        if(bidAmount <= a.getBest_Bid_Amount()) {
            System.out.println("You need to place more than current best bid amount");
            return false;
        }
        
        // Nếu mọi thứ hợp lệ, mới gửi lệnh đặt giá sang Auction
        boolean success = a.receiveBid(this, bidAmount);
        
        if(success) {
            this.Balance -= bidAmount;
            Placed_Products.add(a.getProduct());
            System.out.println("Place bid successfully");
            return true;
        } else {
            System.out.println("This auction is out of dated (Hết hạn) hoặc chưa mở!");
            return false;
        }
    }
    */
}