package com.uet.domain.entity.auction;

//Lớp Auction đóng vai trò là bộ quản lý trung tâm cho một sản phẩm cụ thể đang được rao bán

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.uet.domain.entity.Entity;
import com.uet.domain.entity.item.Item;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.entity.user.Seller;
import com.uet.domain.enums.AuctionStatus;
import com.uet.domain.enums.BidStatus;
import com.uet.domain.enums.ItemStatus;
import com.uet.domain.exceptions.InsufficientBalanceException;
import com.uet.domain.exceptions.InvalidBidException;
import com.uet.domain.exceptions.InvalidTransactionException;
import com.uet.domain.observer.AuctionObserver;


public class Auction extends Entity{
    
    private Item item; // Vật phẩm được đấu giá
    private Seller seller; //Người bán sản phẩm
    private AuctionTime auctionTime; //Thời gian bắt đầu đấu giá
   // private AuctionTime endTime; //Thời gian kết thúc đấu giá
    private double currentMaxPrice; //Giá cao nhất hiện tại
    private List<BidTransaction> historyBids; // Lưu Danh sách lượt đặt giá
    private Bidder winner; // Người thắng cuộc
    private double minIncrement;
    private List<AuctionObserver> observers = new ArrayList<>();

    private AuctionStatus status;

    public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime) {
        this(item, seller, startTime, endTime, 1);
    }

    public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime, double minIncrement) {
        super();
        initializeAuction(item, seller, startTime, endTime, minIncrement);
    }

    public Auction(String id, Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime) {
        this(id, item, seller, startTime, endTime, 1);
    }

    public Auction(String id, Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime, double minIncrement) {
        super(id);
        initializeAuction(item, seller, startTime, endTime, minIncrement);
    }

    private void initializeAuction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime, double minIncrement) {
        if (minIncrement <= 0) {
            throw new IllegalArgumentException("Bước giá tối thiểu phải lớn hơn 0!");
        }
        this.item = item;
        this.seller = seller;
        this.auctionTime = new AuctionTime(startTime, endTime);
        this.currentMaxPrice = item.getStartingPrice();
        this.minIncrement = minIncrement;
        this.status = AuctionStatus.OPEN;
        this.item.setStatus(ItemStatus.IN_AUCTION);
        this.historyBids = new ArrayList<>();
    }


    //Cập nhập trạng thái phiên đấu giá
    public synchronized boolean updateStatus() {
        AuctionStatus oldStatus = this.status;
        LocalDateTime now = LocalDateTime.now();

        if (status == AuctionStatus.PENDING_APPROVAL || status == AuctionStatus.REJECTED) {
            return false;
        }
        
        if ((status == AuctionStatus.OPEN || status == AuctionStatus.RUNNING) && !now.isBefore(auctionTime.getEndTime())) {
            this.status = AuctionStatus.FINISHED;
            // Nếu không có ai đặt giá, có thể chuyển sang CANCELED
            if (winner == null) {
                this.status = AuctionStatus.CANCELED;
            }
        } else if (status == AuctionStatus.OPEN && !now.isBefore(auctionTime.getStartTime()) && now.isBefore(auctionTime.getEndTime())) {
            this.status = AuctionStatus.RUNNING;
        }
        return this.status != oldStatus;
    }

    public void validateBid(double amount) throws InvalidBidException{
        if (status != AuctionStatus.RUNNING) {
            throw new InvalidBidException("Phiên đấu giá hiện không diễn ra!");
        }

        double minimumBid = getMinimumNextBid();
        if (amount < minimumBid) {
            throw new InvalidBidException("Giá đặt tối thiểu là " + minimumBid);
        }
    }

    public synchronized void placeBid(Bidder bidder, double amount) throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
        this.validateBid(amount);
        if (!bidder.canAfford(amount)) {
            throw new InvalidBidException("Tài khoản không đủ số dư khả dụng!");
        }

        // Hoàn tiền cho người cũ và Tạm giữ tiền người mới
        if (winner != null) {
            winner.unlockFunds(this.currentMaxPrice);
            historyBids.get(historyBids.size() - 1).setStatus(BidStatus.OUTBID);
        }
        bidder.lockFunds(amount);

        // Cập nhật lịch sử và cập nhập trạng thái
        BidTransaction newBid = new BidTransaction("BID-" + getId() + "-" + (historyBids.size() + 1), bidder, amount, BidStatus.WINNING);
        historyBids.add(newBid);
        this.currentMaxPrice = amount;
        this.winner = bidder;
        this.notifyObservers();
    }

    // public void extendEndTime(long extraSeconds){
    //     this.endTime = this.endTime.plusSeconds(extraSeconds);
    // }


    // Xác nhận thanh toán cuối cùng (Chuyển sang PAID)
    public synchronized void confirmPayment() throws InvalidTransactionException, InsufficientBalanceException {
        if (this.status != AuctionStatus.FINISHED) {
            throw new InvalidTransactionException("Phiên chưa kết thúc, không thể thanh toán!");
        }
        if (winner == null) {
            throw new InvalidTransactionException("Không có người thắng cuộc!");
        }
        winner.commitPayment(this.currentMaxPrice);
        if (!historyBids.isEmpty()) {
            historyBids.get(historyBids.size() - 1).setStatus(BidStatus.PAID);
        }
        this.item.setStatus(ItemStatus.SOLD);
        this.status = AuctionStatus.PAID;
    }

    public void addObserver(AuctionObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    private void notifyObservers() {
        for (AuctionObserver observer : observers) {
            observer.update(this);
        }
    }


    public AuctionStatus getStatus(){
        return this.status;
    }

    public double getMinIncrement() {
        return this.minIncrement;
    }

    public double getMinimumNextBid() {
        return this.currentMaxPrice + this.minIncrement;
    }

    public void setStatus(AuctionStatus status){
        this.status = status;
    }

    public void restoreState(AuctionStatus status, double currentMaxPrice, Bidder winner) {
        this.status = status;
        this.currentMaxPrice = currentMaxPrice;
        this.winner = winner;
    }

    public Item getItem(){
        return this.item;
    }
    public Seller getSeller(){
        return this.seller;
    }

    public double getCurrentMaxPrice(){
        return this.currentMaxPrice;
    }
    public List<BidTransaction> getHistoryBids(){
        return Collections.unmodifiableList(this.historyBids);
    }
    public Bidder getWinner(){
        return this.winner;
    }
    public LocalDateTime getStartTime(){
        return this.auctionTime.getStartTime();
    }
    public LocalDateTime getEndTime(){
        return this.auctionTime.getEndTime();
    }

}
