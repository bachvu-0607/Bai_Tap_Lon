package com.uet.domain.entity.auction;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit; 
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap; 
import java.util.List;
import java.util.Map;

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
   
   private Item item; 
   private Seller seller; 
   private LocalDateTime startTime; 
   private LocalDateTime endTime; 
   private double currentMaxPrice; 
   private List<BidTransaction> historyBids; 
   private Bidder winner; 
   private double minIncrement;
   private List<AuctionObserver> observers = new ArrayList<>();
   private AuctionStatus status;

   private Map<Bidder, Double> autoBidLimits = new HashMap<>();

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

   public synchronized boolean updateStatus() {
   AuctionStatus oldStatus = this.status;
   LocalDateTime now = LocalDateTime.now();

   if (status == AuctionStatus.PENDING_APPROVAL || status == AuctionStatus.REJECTED) {
      return false;
   }
   
   if ((status == AuctionStatus.OPEN || status == AuctionStatus.RUNNING) && !now.isBefore(endTime)) {
      this.status = AuctionStatus.FINISHED;
      if (winner == null) {
      this.status = AuctionStatus.CANCELED;
      }
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

   // Tách logic xử lý đấu giá  lõi ra một hàm riêng 
   private synchronized void processCoreBid(Bidder bidder, double amount) throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
   this.validateBid(amount);
   if (!bidder.canAfford(amount)) {
      throw new InvalidBidException("Tài khoản không đủ số dư khả dụng!");
   }

   if (winner != null) {
      winner.unlockFunds(this.currentMaxPrice);
      historyBids.get(historyBids.size() - 1).setStatus(BidStatus.OUTBID);
   }
   bidder.lockFunds(amount);

   BidTransaction newBid = new BidTransaction("BID-" + getId() + "-" + (historyBids.size() + 1), bidder, amount, BidStatus.WINNING);
   historyBids.add(newBid);
   this.currentMaxPrice = amount;
   this.winner = bidder;

   // Gia hạn thời gian
   LocalDateTime now = LocalDateTime.now();
   long minutesLeft = ChronoUnit.MINUTES.between(now, this.endTime);
   if (minutesLeft >= 0 && minutesLeft < 5) { // Nếu còn dưới 5 phút
      this.endTime = this.endTime.plusMinutes(5); // Cộng thêm 5 phút
      System.out.println("⏳ [Anti-Sniping] Phát hiện bắn tỉa! Gia hạn phiên thêm 5 phút. Hết hạn mới: " + this.endTime);
   }

   this.notifyObservers();
   }

   //  Hàm đặt giá chính thức 
   public synchronized void placeBid(Bidder bidder, double amount) throws InvalidBidException, InvalidTransactionException, InsufficientBalanceException {
   // B1: Cho người dùng thực tế đặt giá
   processCoreBid(bidder, amount);
   
   // B2:để robot kiểm tra xem có ai auto-bid đè lên không
   triggerAutoBids(); 
   }

   // Người dùng gọi hàm này để cài đặt mức giá tối đa sh
   public synchronized void registerAutoBid(Bidder bidder, double maxLimit) {
   if (maxLimit >= getMinimumNextBid()) {
      this.autoBidLimits.put(bidder, maxLimit);
      triggerAutoBids(); 
   }

   private synchronized void triggerAutoBids() {
   boolean autoBidPlaced = true;
   
   while (autoBidPlaced) {
      autoBidPlaced = false;
      double nextBid = getMinimumNextBid();
      
      Bidder bestCandidate = null;
      double highestLimit = 0;

      for (Map.Entry<Bidder, Double> entry : autoBidLimits.entrySet()) {
      Bidder candidate = entry.getKey();
      double limit = entry.getValue();

      if ((winner != null && candidate.getId().equals(winner.getId())) || limit < nextBid) {
         continue;
      }

      if (limit > highestLimit && candidate.canAfford(nextBid)) {
         highestLimit = limit;
         bestCandidate = candidate;
      }
      }

      //  tìm được người đủ xiền, robot tự động đặt tiền hộ
      if (bestCandidate != null) {
      try {
         System.out.println("🤖 [Auto-Bid] Hệ thống tự đặt " + nextBid + " thay mặt cho đại gia " + bestCandidate.getId());
         processCoreBid(bestCandidate, nextBid);
         autoBidPlaced = true; 
      } catch (Exception e) {
         autoBidLimits.remove(bestCandidate);
      }
      }
   }
   }

   public void extendEndTime(long extraSeconds){
   this.endTime = this.endTime.plusSeconds(extraSeconds);
   }

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

   public void addObserver(AuctionObserver observer) { observers.add(observer); }
   public void removeObserver(AuctionObserver observer) { observers.remove(observer); }
   private void notifyObservers() {
   for (AuctionObserver observer : observers) { observer.update(this); }
   }

   public AuctionStatus getStatus(){ return this.status; }
   public double getMinIncrement() { return this.minIncrement; }
   public double getMinimumNextBid() { return this.currentMaxPrice + this.minIncrement; }
   public void setStatus(AuctionStatus status){ this.status = status; }
   
   public void restoreState(AuctionStatus status, double currentMaxPrice, Bidder winner) {
   this.status = status;
   this.currentMaxPrice = currentMaxPrice;
   this.winner = winner;
   }

   public Item getItem(){ return this.item; }
   public Seller getSeller(){ return this.seller; }
   public double getCurrentMaxPrice(){ return this.currentMaxPrice; }
   public List<BidTransaction> getHistoryBids(){ return Collections.unmodifiableList(this.historyBids); }
   public Bidder getWinner(){ return this.winner; }
   public LocalDateTime getStartTime(){ return this.startTime; }
   public LocalDateTime getEndTime(){ return this.endTime; }
}
