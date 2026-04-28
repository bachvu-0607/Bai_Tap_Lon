package com.uet.domain.entity;
import java.util.ArrayList;
import java.util.List;
import com.uet.domain.exceptions.*;

public class Bidder extends User implements Payable, Biddable {
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
    

    public Bidder(String userName, String password, String id) {
        super(userName, password, id);
        this.balance = 0;
        this.lockedBalance = 0;
        this.maxBidLimit = 0;
        autoBidEnabled = false;
    }

    //Bật/Tắt tính năng tự động đấu giá
    public void enableAutoBid(double maxBidLimit) throws InsufficientBalanceException, InvalidTransactionException {
        if (maxBidLimit <= 0) {
            throw new InvalidTransactionException("Giới hạn giá tự động đặt phải lớn hơn 0!");
        }
        if (maxBidLimit > getAvailableBalance()) { 
            throw new InsufficientBalanceException("Giới hạn tự động đặt giá không được vượt quá số dư khả dụng!");
        }
        this.maxBidLimit = maxBidLimit;
        this.autoBidEnabled = true;
    }
    public void disableAutoBid() {
        this.autoBidEnabled = false;
        this.maxBidLimit = 0;
    }

    @Override
    public void deposit(double amount) throws InvalidDepositException{
        if(amount > 0){
            this.balance += amount;
        }
        else{
            throw new InvalidDepositException("Số tiền nạp phải lớn hơn 0");
        }
    }

    //Thêm lịch sử đặt giá
    public void addBidToHistory(BidTransaction bid) {
        this.bidHistory.add(bid);
    }

    @Override
    public boolean canAfford(double amount) {
        return getAvailableBalance() >= amount;
    }

    @Override
    public void lockFunds(double amount) throws InvalidTransactionException, InsufficientBalanceException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Số tiền cần tạm giữ phải lớn hơn 0!");
        }
        if (amount > getAvailableBalance()) {
            throw new InsufficientBalanceException("Số dư khả dụng không đủ để tạm giữ!");
        }
        this.lockedBalance += amount;
    }

    /**
     * Hoàn trả lại số tiền đã tạm giữ khi có người khác đặt giá cao hơn.
     * Giúp người dùng có lại tiền để tiếp tục tham gia đấu giá
     */
    @Override
    public void unlockFunds(double amount) throws InvalidTransactionException, InsufficientBalanceException {
        // Không cho phép âm tiền
        if (amount <= 0) {
            throw new InvalidTransactionException("Số tiền cần hoàn trả phải lớn hơn 0!");
        }
        
        // Không cho phép hoàn trả tiền khi không có số dư bị tạm giữ
        if (this.lockedBalance <= 0) {
            throw new InsufficientBalanceException("Không có số tiền nào đang bị tạm giữ!");
        }

        // Không cho phép hoàn trả nhiều hơn số tiền đang bị tạm giữ
        if (amount > this.lockedBalance) {
            throw new InsufficientBalanceException("Số tiền hoàn trả không được vượt quá số tiền đang bị tạm giữ!");
        }

        this.lockedBalance -= amount;

    }

    // Thực hiện trừ tiền vĩnh viễn khi trạng thái chuyển sang PAID
    @Override
    public void commitPayment(double amount) throws InvalidTransactionException, InsufficientBalanceException {
        if (amount <= 0) {
            throw new InvalidTransactionException("Số tiền thanh toán phải lớn hơn 0!");
        }
        if (amount > this.lockedBalance) {
            throw new InsufficientBalanceException("Số tiền thanh toán không được vượt quá số tiền đã tạm giữ!");
        }
        this.balance -= amount;
        this.lockedBalance -= amount;
    }


    //getter & setter
    @Override
    public double getAvailableBalance() { return balance - lockedBalance; }

    @Override
    public double getBalance() { return balance; }
    public double getLockedBalance() { return lockedBalance; }
    public double getMaxBidLimit() { return maxBidLimit; }
    public void setMaxBidLimit(double maxBidLimit) throws InvalidTransactionException, InsufficientBalanceException {
        if(maxBidLimit <= 0) {
            throw new InvalidTransactionException("Giới hạn giá tự động đặt phải lớn hơn 0!");
        }
        if(maxBidLimit > this.balance){
            throw new InsufficientBalanceException("Giới hạn giá tự động đặt không được vượt quá số dư khả dụng!");
        }   
        this.maxBidLimit = maxBidLimit;
    }

    public boolean isAutoBidEnabled() { return autoBidEnabled; }

    public List<BidTransaction> getBidHistory() { return bidHistory; }

    public List<Item> getPlacedProducts() { return placedProducts; }

    public void addPlacedProduct(Item item) {
        this.placedProducts.add(item);
    }

    @Override
    public String toString() {
        return "Bidder: " + getUserName() + 
               " (ID: " + getId() + ") - " +
                "Balance: " + balance + "$";
    }
    
}