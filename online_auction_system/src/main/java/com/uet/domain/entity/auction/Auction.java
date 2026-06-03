package com.uet.domain.entity.auction;

//Lớp Auction đóng vai trò là bộ quản lý trung tâm cho một sản phẩm cụ thể đang được rao bán

import java.time.Duration;
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

    // Cập nhật trạng thái theo thời gian thực tế: chờ mở, đang chạy, hết hạn hoặc hủy nếu không có winner.
    public synchronized boolean updateStatus() {
        AuctionStatus oldStatus = this.status;
        LocalDateTime now = LocalDateTime.now();

        if (status == AuctionStatus.PENDING_APPROVAL || status == AuctionStatus.REJECTED) {
            return false;
        }
        
        if ((status == AuctionStatus.OPEN || status == AuctionStatus.RUNNING) && !now.isBefore(endTime)) {
            setStatus(winner == null ? AuctionStatus.CANCELED : AuctionStatus.FINISHED);
        } else if (status == AuctionStatus.OPEN && !now.isBefore(startTime) && now.isBefore(endTime)) {
            setStatus(AuctionStatus.RUNNING);
        }
        return this.status != oldStatus;
    }

    // Kiểm tra bid có được phép đặt ở trạng thái hiện tại và có đạt bước giá tối thiểu không.
    public void validateBid(double amount) throws InvalidBidException{
        if (status != AuctionStatus.RUNNING) {
            throw new InvalidBidException("Phiên đấu giá hiện không diễn ra!");
        }

        double minimumBid = getMinimumNextBid();
        if (amount < minimumBid) {
            throw new InvalidBidException("Giá đặt tối thiểu là " + minimumBid);
        }
    }

    // Xử lý nghiệp vụ đặt giá trong entity: validate, hoàn tiền winner cũ, khóa tiền bidder mới và ghi bid thắng mới.
    public synchronized void placeBid(Bidder bidder, double amount) throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
        this.validateBid(amount);
        boolean sameWinner = winner != null && winner.getId().equals(bidder.getId());
        double reusableLockedAmount = sameWinner ? Math.min(this.currentMaxPrice, bidder.getLockedBalance()) : 0;
        if (bidder.getAvailableBalance() + reusableLockedAmount < amount) {
            throw new InvalidBidException("Tài khoản không đủ số dư khả dụng!");
        }

        // Hoàn tiền cho người cũ và Tạm giữ tiền người mới
        if (winner != null) {
            if (sameWinner) {
                unlockBidderOwnPreviousFunds(bidder);
            } else {
                unlockPreviousWinnerFunds();
            }
            markCurrentWinningBidOutbid(winner.getId());
        }
        bidder.lockFunds(amount);

        // Cập nhật lịch sử và cập nhập trạng thái
        BidTransaction newBid = new BidTransaction("BID-" + getId() + "-" + (historyBids.size() + 1), bidder, amount, BidStatus.WINNING);
        historyBids.add(newBid);
        this.currentMaxPrice = amount;
        this.winner = bidder;
        this.notifyObservers();
    }

    // Đánh dấu bid WINNING hiện tại của winner cũ thành OUTBID trước khi winner mới lên dẫn đầu.
    private void markCurrentWinningBidOutbid(String winnerId) {
        for (int i = historyBids.size() - 1; i >= 0; i--) {
            BidTransaction bid = historyBids.get(i);
            if (bid.getStatus() == BidStatus.WINNING && bid.getBidder().getId().equals(winnerId)) {
                bid.setStatus(BidStatus.OUTBID);
                return;
            }
        }
    }

    // Khi cùng một bidder tăng giá của chính mình, trả phần tiền cũ để khóa lại theo mức mới.
    private void unlockBidderOwnPreviousFunds(Bidder bidder) throws InvalidTransactionException, InsufficientBalanceException {
        double lockedAmount = bidder.getLockedBalance();
        if (lockedAmount <= 0) {
            return;
        }
        bidder.unlockFunds(Math.min(this.currentMaxPrice, lockedAmount));
    }

    // Hoàn tiền đang bị giữ cho winner cũ khi họ bị outbid hoặc phiên bị hủy.
    private void unlockPreviousWinnerFunds() throws InvalidTransactionException, InsufficientBalanceException {
        double lockedAmount = winner.getLockedBalance();
        if (lockedAmount <= 0) {
            return;
        }
        winner.unlockFunds(Math.min(this.currentMaxPrice, lockedAmount));
    }

    // Xóa ảnh hưởng của bidder bị ban khỏi phiên: hủy bid của họ và đẩy winner hợp lệ phía sau lên nếu cần.
    public synchronized boolean cancelBidderParticipation(String bidderId) throws InvalidTransactionException, InsufficientBalanceException {
        if (bidderId == null || this.status == AuctionStatus.PAID || this.status == AuctionStatus.CANCELED) {
            return false;
        }

        boolean changed = cancelBidRecordsByBidder(bidderId);
        if (winner != null && winner.getId().equals(bidderId)) {
            unlockPreviousWinnerFunds();
            this.winner = null;
            this.currentMaxPrice = item.getStartingPrice();
            promoteFallbackWinner();
            changed = true;
        }

        if (changed) {
            this.notifyObservers();
        }
        return changed;
    }

    // Tìm bid hợp lệ gần nhất trong lịch sử để thay thế winner bị ban; nếu không còn ai thì hủy phiên đã kết thúc.
    private void promoteFallbackWinner() {
        for (int i = historyBids.size() - 1; i >= 0; i--) {
            BidTransaction bid = historyBids.get(i);
            if (bid.getStatus() == BidStatus.CANCELED || bid.getStatus() == BidStatus.PAID) {
                continue;
            }
            try {
                bid.getBidder().lockFunds(bid.getBidAmount());
                bid.setStatus(BidStatus.WINNING);
                this.winner = bid.getBidder();
                this.currentMaxPrice = bid.getBidAmount();
                return;
            } catch (InvalidTransactionException | InsufficientBalanceException e) {
                bid.setStatus(BidStatus.CANCELED);
            }
        }

        if (this.status == AuctionStatus.FINISHED) {
            this.status = AuctionStatus.CANCELED;
        }
    }

    // Hủy phiên khi seller bị ban: hoàn tiền winner, hủy bid mở và đánh dấu sản phẩm bị gỡ.
    public synchronized boolean cancelBecauseSellerBanned() throws InvalidTransactionException, InsufficientBalanceException {
        if (this.status == AuctionStatus.PAID || this.status == AuctionStatus.REJECTED || this.status == AuctionStatus.CANCELED) {
            return false;
        }

        if (winner != null) {
            unlockPreviousWinnerFunds();
        }
        cancelAllOpenBidRecords();
        this.winner = null;
        this.currentMaxPrice = item.getStartingPrice();
        this.status = AuctionStatus.CANCELED;
        this.item.setStatus(ItemStatus.REMOVED);
        this.notifyObservers();
        return true;
    }

    // Đánh dấu toàn bộ bid chưa chốt của một bidder thành CANCELED.
    private boolean cancelBidRecordsByBidder(String bidderId) {
        boolean changed = false;
        for (BidTransaction bid : historyBids) {
            if (bid.getBidder().getId().equals(bidderId)
                    && bid.getStatus() != BidStatus.PAID
                    && bid.getStatus() != BidStatus.CANCELED) {
                bid.setStatus(BidStatus.CANCELED);
                changed = true;
            }
        }
        return changed;
    }

    // Đánh dấu mọi bid chưa thanh toán/chưa hủy thành CANCELED khi cả phiên bị hủy.
    private void cancelAllOpenBidRecords() {
        for (BidTransaction bid : historyBids) {
            if (bid.getStatus() != BidStatus.PAID && bid.getStatus() != BidStatus.CANCELED) {
                bid.setStatus(BidStatus.CANCELED);
            }
        }
    }

    // Gia hạn thời gian kết thúc phiên và báo cho observer biết phiên đã thay đổi.
    public synchronized void extendEndTime(long extraSeconds){
        this.endTime = this.endTime.plusSeconds(extraSeconds);
        this.notifyObservers();
    }

    // Nếu có bid ở sát thời điểm đóng phiên, kéo dài endTime để tránh kiểu "canh giây cuối".
    public synchronized boolean extendEndTimeIfCloseToEnd(long thresholdSeconds, long extraSeconds) {
        LocalDateTime now = LocalDateTime.now();
        if (thresholdSeconds <= 0 || extraSeconds <= 0 || !now.isBefore(endTime)) {
            return false;
        }

        long remainingSeconds = Duration.between(now, endTime).getSeconds();
        if (remainingSeconds > thresholdSeconds) {
            return false;
        }

        extendEndTime(extraSeconds);
        return true;
    }

    // Xác nhận thanh toán cuối cùng: trừ tiền winner, đánh dấu bid cuối là PAID và chuyển item sang SOLD.
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

    public void restoreBidHistory(List<BidTransaction> historyBids) {
        this.historyBids = new ArrayList<>(historyBids);
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
