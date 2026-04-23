package com.uet.domain.entity;
import java.util.ArrayList;
import java.util.List;
import com.uet.domain.exceptions.*;

public class Bidder extends User{
    private static final long serialVersionUID = 1L;
    //Số dư người tham gia đấu giá
    private double balance;

    //Số tiền đang bị tạm giữ (Đang đấu giá)
    private double lockedBalance;

    //Phục vụ tính năng đấu giá tự động
    private double maxBidLimit;
    private boolean autoBidEnabled;


    //Danh sách lịch sử đặt giá
    private List<BidTransaction> bidHistory = new ArrayList<BidTransaction>();

    //Danh sách sản phẩm đã đặt giá
    private List<Item> placedProducts = new ArrayList<Item>();
    
    public Bidder() {}
    public Bidder(String UserName, String Password, String ID) {
        super(UserName, Password, ID);
        this.balance = 0;
        autoBidEnabled = false;
        System.out.println("Account created successfully!");
    }

    //Bật/Tắt tính năng tự động đấu giá
    public void enableAutoBid() {
        this.autoBidEnabled = true;
    }
    public void disableAutoBid() {
        this.autoBidEnabled = false;
    }
    

    //Nạp tiền vào ví
    public void deposit(double amount) throws InvalidDepositException{
        if(amount > 0){
            this.balance += amount;
            System.out.println("User: " + getName() + "Đã nạp tiền thành công " + amount + "$");
        }
        else{
            throw new InvalidDepositException("Số tiền nạp phải lớn hơn 0");
        }
    }

    //Thêm lịch sử đặt giá
    public void addBidToHistory(BidTransaction bid) {
        this.bidHistory.add(bid);
    }

    //Kiểm tra xem người tham gia có đủ tiền để đặt giá không
    public boolean canAfford(double amount) {
        return this.balance >= amount;
    }

    // Tạm giữ tiền
    public void lockFunds(double amount) {
        this.lockedBalance += amount;
    }

    /**
     * Hoàn trả lại số tiền đã tạm giữ khi có người khác đặt giá cao hơn.
     * Giúp người dùng có lại tiền để tiếp tục tham gia đấu giá
     */
    public void unlockFunds(double amount) {
        if (amount <= this.lockedBalance) {
            this.lockedBalance -= amount;
        } else {
            this.lockedBalance = 0;
        }
    }

    // Thực hiện trừ tiền vĩnh viễn khi trạng thái chuyển sang PAID
    public void commitPayment(double amount) {
        this.balance -= amount;
        this.lockedBalance -= amount;
    }

    
    //Đặt giá, để sever làm việc này
    // public void placeBid(Auction auction, double bidAmount) throws InvalidBidException{
    //     if(bidAmount <= auction.getCurrentMaxPrice()){
    //         throw new InvalidBidException("Bạn cần đặt giá lớn hơn giá cao nhất hiện tại!");
    //     }
    //     else if(canAfford(bidAmount)){
    //         BidTransaction bid = new BidTransaction(this, bidAmount);
    //         bidHistory.add(bid);
    //         auction.updateCurrentPrice(this, bidAmount);
    //         this.balance -= bidAmount;
    //         System.out.println("Place bid successfully");
    //     }
    //     else{
    //         throw new InvalidBidException("Bạn không đủ tiền để đặt giá!");
    //     }
    // }

    //getter
    public double getAvailableBalance() { return balance - lockedBalance; }

    @Override
    public String toString() {
        return "Bidder: " + UserName + " (ID: " + id + ") - Balance: " + balance + "$";
    }
    
}