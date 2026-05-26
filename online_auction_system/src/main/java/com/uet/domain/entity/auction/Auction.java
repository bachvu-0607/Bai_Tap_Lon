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
    private LocalDateTime startTime; //Thời gian bắt đầu đấu giá
    private LocalDateTime endTime; //Thời gian kết thúc đấu giá
    private double currentMaxPrice; //Giá cao nhất hiện tại
    private List<BidTransaction> historyBids; // Lưu Danh sách lượt đặt giá
    private Bidder winner; // Người thắng cuộc
    private double minIncrement;
    private List<AuctionObserver> observers = new ArrayList<>();
    private static final long SNIPE_WINDOW_SECONDS = 60;   // Ngưỡng kích hoạt (60s cuối)
    private static final long EXTEND_SECONDS = 120;        // Gia hạn thêm 120s

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
        this.startTime = startTime;
        this.endTime = endTime;
        this.currentMaxPrice = item.getStartingPrice();
        this.minIncrement = minIncrement;
        this.status = AuctionStatus.OPEN;
        this.item.setStatus(ItemStatus.IN_AUCTION);
        this.historyBids = new ArrayList<>();
    }

    //Cập nhập trạng thái phiên đấu giá (có notify observers)
    public synchronized boolean updateStatus() {
        AuctionStatus oldStatus = this.status;
        if (updateStatusQuietly()) {
            notifyObservers();
        }
        return this.status != oldStatus;
    }

    // Cập nhật trạng thái KHÔNG gọi notifyObservers — dùng khi caller tự quản lý broadcast
    public synchronized boolean updateStatusQuietly() {
        AuctionStatus oldStatus = this.status;
        LocalDateTime now = LocalDateTime.now();

        if (status == AuctionStatus.PENDING_APPROVAL || status == AuctionStatus.REJECTED
                || status == AuctionStatus.FINISHED || status == AuctionStatus.PAID
                || status == AuctionStatus.CANCELED) {
            return false;
        }

        if ((status == AuctionStatus.OPEN || status == AuctionStatus.RUNNING) && !now.isBefore(endTime)) {
            this.status = (winner == null) ? AuctionStatus.CANCELED : AuctionStatus.FINISHED;
        } else if (status == AuctionStatus.OPEN && !now.isBefore(startTime) && now.isBefore(endTime)) {
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
    // Đặt giá mới, có kiểm tra hợp lệ và cập nhật trạng thái phiên đấu giá
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

    public void extendEndTime(long extraSeconds){
        this.endTime = this.endTime.plusSeconds(extraSeconds);
        this.notifyObservers();
    }

    /**
     * Xác nhận thanh toán cuối cùng cho phiên đấu giá.
     * Chỉ được gọi khi phiên ở trạng thái FINISHED (đã có người thắng, đã hết giờ).
     *
     * Thực hiện 4 bước:
     * 1. Kiểm tra điều kiện hợp lệ (FINISHED + có winner)
     * 2. Gọi winner.commitPayment(): trừ tiền vĩnh viễn khỏi balance và lockedBalance của winner
     * 3. Đánh dấu lượt đặt giá cuối cùng trong bộ nhớ → BidStatus.PAID
     * 4. Đánh dấu sản phẩm → SOLD, trạng thái phiên → PAID
     *
     * Lưu ý: Sau khi gọi hàm này, cần gọi WalletRepository.updateBalance() và
     * AuctionRepository.updateAuction() để lưu thay đổi vào database.
     */
    public synchronized void confirmPayment() throws InvalidTransactionException, InsufficientBalanceException {
        // Kiểm tra phiên phải ở trạng thái FINISHED mới cho phép thanh toán
        if (this.status != AuctionStatus.FINISHED) {
            throw new InvalidTransactionException("Phiên chưa kết thúc, không thể thanh toán!");
        }
        // Kiểm tra phải có người thắng
        if (winner == null) {
            throw new InvalidTransactionException("Không có người thắng cuộc!");
        }
        // Trừ tiền vĩnh viễn: balance -= amount, lockedBalance -= amount
        winner.commitPayment(this.currentMaxPrice);
        // Cập nhật trạng thái lượt đặt giá cuối trong bộ nhớ (nếu có)
        if (!historyBids.isEmpty()) {
            historyBids.get(historyBids.size() - 1).setStatus(BidStatus.PAID);
        }
        // Đánh dấu sản phẩm đã được bán
        this.item.setStatus(ItemStatus.SOLD);
        // Chuyển phiên sang trạng thái PAID (gán trực tiếp vì không cần notify ở đây,
        // AuctionManager.processPayment() sẽ gọi notifyUpdated() sau khi mọi thứ xong)
        this.status = AuctionStatus.PAID;
    }
    /**
     * Anti-sniping: kiểm tra xem bid vừa đặt có rơi vào "cửa sổ snipe" không.
     * Nếu có → kéo dài endTime, notify toàn bộ observers.
     * Trả về true nếu thực sự đã gia hạn (để caller biết cần persist DB).
     */
    public synchronized boolean checkAndExtendIfSniped() {
        long remaining = java.time.Duration.between(LocalDateTime.now(), this.endTime).getSeconds();

        // Chỉ kích hoạt nếu bid rơi vào SNIPE_WINDOW cuối
        if (remaining > SNIPE_WINDOW_SECONDS) {
            return false;
        }

        // Tính newEndTime = now + EXTEND_SECONDS
        LocalDateTime newEnd = LocalDateTime.now().plusSeconds(EXTEND_SECONDS);

        // Chỉ gia hạn nếu newEnd thực sự dài hơn endTime hiện tại
        // Tránh trường hợp bid đến dồn dập gây gia hạn chồng chất vô hạn
        if (!newEnd.isAfter(this.endTime)) {
            return false;
        }

        this.endTime = newEnd;
        this.notifyObservers(); // Push AUCTION_UPDATED đến tất cả client
        return true;
    }
    public void addObserver(AuctionObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    public void notifyUpdated() {
        notifyObservers();
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

    public synchronized void setStatus(AuctionStatus status){
        if (this.status == status) {
            return;
        }
        this.status = status;
        this.notifyObservers();
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
        return this.startTime;
    }
    public LocalDateTime getEndTime(){
        return this.endTime;
    }

}
