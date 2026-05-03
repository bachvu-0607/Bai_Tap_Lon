package com.uet.domain.entity;

//Lớp Auction đóng vai trò là bộ quản lý trung tâm cho một sản phẩm cụ thể đang được rao bán

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.uet.domain.enums.AuctionStatus;
import com.uet.domain.exceptions.InsufficientBalanceException;
import com.uet.domain.exceptions.InvalidBidException;
import com.uet.domain.exceptions.InvalidTransactionException;


public class Auction extends Entity{
    
    private Item item; // Vật phẩm được đấu giá
    private Seller seller; //Người bán sản phẩm
    private LocalDateTime startTime; //Thời gian bắt đầu đấu giá
    private LocalDateTime endTime; //Thời gian kết thúc đấu giá
    private double currentMaxPrice; //Giá cao nhất hiện tại
    private List<BidTransaction> historyBids; // Lưu Danh sách lượt đặt giá
    private Bidder winner; // Người thắng cuộc

    private AuctionStatus status;

    public Auction(String id, Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime){
        super(id);
        this.item = item;
        this.seller = seller;
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentMaxPrice = item.getStartingPrice();
        this.status = AuctionStatus.OPEN;
        this.historyBids = new ArrayList<>();
    } 

    //Cập nhập trạng thái phiên đấu giá
    public synchronized void updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        
        if (status == AuctionStatus.OPEN && now.isAfter(startTime) && now.isBefore(endTime)) {
            this.status = AuctionStatus.RUNNING;
        } else if (status == AuctionStatus.RUNNING && now.isAfter(endTime)) {
            this.status = AuctionStatus.FINISHED;
            // Nếu không có ai đặt giá, có thể chuyển sang CANCELED
            if (winner == null) {
                this.status = AuctionStatus.CANCELED;
            }
        }
    }



    /*
     * Đặt giá mới. Tự động hoàn tiền cho người cũ và tạm giữ tiền người mới.
     */
    public synchronized void updateCurrentPrice(Bidder bidder, double newPrice) throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
        if (status != AuctionStatus.RUNNING) {
            throw new InvalidBidException("Phiên đấu giá hiện không diễn ra!");
        }
        if (newPrice <= this.currentMaxPrice) {
            throw new InvalidBidException("Giá đặt phải lớn hơn " + currentMaxPrice);
        }
        if (!bidder.canAfford(newPrice)) {
            throw new InvalidBidException("Tài khoản không đủ số dư khả dụng!");
        }

        // Hoàn tiền cho người cũ và Tạm giữ tiền người mới
        if (winner != null) {
            winner.unlockFunds(this.currentMaxPrice);
        }
        bidder.lockFunds(newPrice);

        // Cập nhật lịch sử và cập nhập trạng thái
        BidTransaction newBid = new BidTransaction(bidder, newPrice);
        historyBids.add(newBid);
        this.currentMaxPrice = newPrice;
        this.winner = bidder;
    }

    /*
    *Gia hạn thời gian kết thúc khi có bid giây cuối
     */
    public void extendEndTime(long extraSeconds){
        this.endTime = this.endTime.plusSeconds(extraSeconds);
    }



    // Xác nhận thanh toán cuối cùng (Chuyển sang PAID)
    public synchronized void confirmPayment() throws InvalidTransactionException, InsufficientBalanceException {
        if (this.status != AuctionStatus.FINISHED) {
            throw new InvalidTransactionException("Phiên chưa kết thúc, không thể thanh toán!");
        }
        if (winner == null) {
            throw new InvalidTransactionException("Không có người thắng cuộc!");
        }
        winner.commitPayment(this.currentMaxPrice);
        this.status = AuctionStatus.PAID;
    }



    public AuctionStatus getStatus(){
        return this.status;
    }
    public void setStatus(AuctionStatus status){
        this.status = status;
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
    public Bidder getWinner(){
        return this.winner;
    }
    public LocalDateTime getStartTime(){
        return this.startTime;
    }
    public LocalDateTime getEndTime(){
        return this.endTime;
    }

}
