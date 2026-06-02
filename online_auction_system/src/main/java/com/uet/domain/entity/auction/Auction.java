package com.uet.domain.entity.auction;

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

/**
 * Lớp đóng vai trò trung tâm (Controller/Manager) quản lý một phiên đấu giá.
 * Kế thừa Entity, lưu giữ thông tin sản phẩm, người bán, người mua, danh sách lịch sử đặt giá
 * và các logic như kiểm tra điều kiện đặt giá, thay đổi trạng thái, thanh toán, cũng như 
 * ứng dụng Pattern Observer để thông báo khi có sự thay đổi.
 */
public class Auction extends Entity{
    
    /** Vật phẩm được mang ra đấu giá */
    private Item item; 
    
    /** Người dùng đăng bán sản phẩm (Seller) */
    private Seller seller; 
    
    /** Thời điểm chính thức bắt đầu cho phép đặt giá */
    private LocalDateTime startTime; 
    
    /** Thời điểm kết thúc phiên đấu giá */
    private LocalDateTime endTime; 
    
    /** Giá cao nhất tính tới thời điểm hiện tại */
    private double currentMaxPrice; 
    
    /** Danh sách lưu toàn bộ các lượt đặt giá hợp lệ */
    private List<BidTransaction> historyBids; 
    
    /** Người đang trả giá cao nhất (Winner tạm thời hoặc Winner chính thức) */
    private Bidder winner; 
    
    /** Bước giá tối thiểu bắt buộc khi muốn đặt cao hơn giá hiện tại */
    private double minIncrement;
    
    /** Danh sách những Observer đang theo dõi diễn biến của phiên đấu giá này */
    private List<AuctionObserver> observers = new ArrayList<>();

    /** Trạng thái của phiên đấu giá (Ví dụ: đang mở, đang chạy, đã kết thúc) */
    private AuctionStatus status;

    /**
     * Khởi tạo một phiên đấu giá mới với bước giá mặc định là 1$.
     * 
     * @param item Vật phẩm đem ra đấu.
     * @param seller Người bán.
     * @param startTime Thời gian bắt đầu.
     * @param endTime Thời gian kết thúc.
     */
    public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime) {
        this(item, seller, startTime, endTime, 1);
    }

    /**
     * Khởi tạo một phiên đấu giá mới với thông số đầy đủ.
     * 
     * @param item Vật phẩm đấu giá.
     * @param seller Người đăng bán.
     * @param startTime Thời điểm mở cửa.
     * @param endTime Thời điểm đóng cửa.
     * @param minIncrement Bước giá nhảy tối thiểu.
     */
    public Auction(Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime, double minIncrement) {
        super();
        initializeAuction(item, seller, startTime, endTime, minIncrement);
    }

    /**
     * Khởi tạo phiên đấu giá từ dữ liệu cũ (Dùng khi lấy từ DB) với bước giá mặc định.
     * 
     * @param id ID của phiên đấu giá.
     * @param item Vật phẩm đấu giá.
     * @param seller Người bán.
     * @param startTime Thời gian bắt đầu.
     * @param endTime Thời gian kết thúc.
     */
    public Auction(String id, Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime) {
        this(id, item, seller, startTime, endTime, 1);
    }

    /**
     * Khởi tạo phiên đấu giá với đầy đủ thông số bằng ID có sẵn (Khôi phục từ DB).
     * 
     * @param id Mã định danh phiên.
     * @param item Vật phẩm đấu giá.
     * @param seller Người đăng bán.
     * @param startTime Thời điểm mở cửa.
     * @param endTime Thời điểm đóng cửa.
     * @param minIncrement Bước giá tối thiểu.
     */
    public Auction(String id, Item item, Seller seller, LocalDateTime startTime, LocalDateTime endTime, double minIncrement) {
        super(id);
        initializeAuction(item, seller, startTime, endTime, minIncrement);
    }

    /**
     * Hàm hỗ trợ gán các giá trị khởi tạo chung cho đối tượng Auction.
     * Đồng thời, khởi tạo danh sách lịch sử và thiết lập trạng thái mặc định của Item là IN_AUCTION.
     * 
     * @param item Sản phẩm.
     * @param seller Người bán.
     * @param startTime Bắt đầu.
     * @param endTime Kết thúc.
     * @param minIncrement Bước giá.
     * @throws IllegalArgumentException Nếu bước giá <= 0.
     */
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

    /**
     * Cập nhật trạng thái của phiên đấu giá dựa theo thời gian thực (hiện tại).
     * 
     * @return true nếu có sự thay đổi trạng thái, false nếu không.
     */
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

    /**
     * Kiểm tra tính hợp lệ của số tiền mà người dùng định đặt.
     * Xem phiên có đang mở hay không và số tiền có đủ lớn hơn giá tối thiểu yêu cầu không.
     * 
     * @param amount Số tiền định đặt.
     * @throws InvalidBidException Nếu phiên chưa mở hoặc số tiền nhỏ hơn yêu cầu.
     */
    public void validateBid(double amount) throws InvalidBidException{
        if (status != AuctionStatus.RUNNING) {
            throw new InvalidBidException("Phiên đấu giá hiện không diễn ra!");
        }

        double minimumBid = getMinimumNextBid();
        if (amount < minimumBid) {
            throw new InvalidBidException("Giá đặt tối thiểu là " + minimumBid);
        }
    }

    /**
     * Thực hiện ghi nhận một lượt đặt giá hợp lệ.
     * Phương thức này có đồng bộ hóa (synchronized) để chống hiện tượng Race Condition
     * khi nhiều luồng (clients) truy cập đặt giá cùng một lúc.
     * 
     * @param bidder Người đặt giá.
     * @param amount Số tiền.
     * @throws InvalidBidException Nếu giá trị đặt vi phạm luật, hoặc không đủ tiền.
     * @throws InvalidTransactionException Lỗi trong quá trình tạm giữ/hoàn tiền.
     * @throws InsufficientBalanceException Nếu không đủ số dư để khóa (lockFunds).
     */
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

    /**
     * Gia hạn thêm thời gian cho phiên đấu giá (Thường dùng cho luật chống bắn tỉa - Anti-Sniper).
     * 
     * @param extraSeconds Số giây muốn cộng thêm.
     */
    public void extendEndTime(long extraSeconds){
        this.endTime = this.endTime.plusSeconds(extraSeconds);
        this.notifyObservers();
    }

    /**
     * Xác nhận thanh toán cuối cùng (Chuyển trạng thái sang PAID).
     * Hệ thống sẽ trừ tiền vĩnh viễn của người chiến thắng và chuyển trạng thái sản phẩm sang Đã Bán.
     * 
     * @throws InvalidTransactionException Nếu phiên chưa kết thúc hoặc chưa có ai thắng cuộc.
     * @throws InsufficientBalanceException Nếu xảy ra lỗi không đủ số dư khi commit thanh toán.
     */
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

    /**
     * Đăng ký một Observer để theo dõi phiên.
     * 
     * @param observer Đối tượng Observer.
     */
    public void addObserver(AuctionObserver observer) {
        observers.add(observer);
    }

    /**
     * Hủy đăng ký theo dõi phiên đối với một Observer.
     * 
     * @param observer Đối tượng Observer.
     */
    public void removeObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    /**
     * Công khai phương thức thông báo các thay đổi thủ công tới toàn bộ Observer.
     */
    public void notifyUpdated() {
        notifyObservers();
    }

    /**
     * Cập nhật và phát sự kiện xuống các Observer (như Database, Socket Server) mỗi khi có 
     * sự kiện mới như: thay đổi giá, đổi trạng thái.
     */
    private void notifyObservers() {
        for (AuctionObserver observer : observers) {
            observer.update(this);
        }
    }

    /**
     * Lấy trạng thái hiện tại của phiên đấu giá.
     * 
     * @return Trạng thái AuctionStatus.
     */
    public AuctionStatus getStatus(){
        return this.status;
    }

    /**
     * Lấy giá trị quy định cho bước giá nhảy tối thiểu.
     * 
     * @return Bước giá.
     */
    public double getMinIncrement() {
        return this.minIncrement;
    }

    /**
     * Lấy giá trị hợp lệ tối thiểu mà người tiếp theo phải đặt.
     * (Bằng giá cao nhất hiện hành + bước giá nhảy tối thiểu).
     * 
     * @return Giá tối thiểu yêu cầu cho lượt tiếp theo.
     */
    public double getMinimumNextBid() {
        return this.currentMaxPrice + this.minIncrement;
    }

    /**
     * Cập nhật trạng thái thủ công cho phiên đấu giá và phát sự kiện thông báo.
     * 
     * @param status Trạng thái mới.
     */
    public synchronized void setStatus(AuctionStatus status){
        if (this.status == status) {
            return;
        }
        this.status = status;
        this.notifyObservers();
    }

    /**
     * Khôi phục trạng thái cho phiên đấu giá từ dữ liệu cũ (Dùng khi load Database).
     * 
     * @param status Trạng thái.
     * @param currentMaxPrice Mức giá cao nhất tại thời điểm lưu.
     * @param winner Người giữ kỷ lục đặt giá tại thời điểm đó.
     */
    public void restoreState(AuctionStatus status, double currentMaxPrice, Bidder winner) {
        this.status = status;
        this.currentMaxPrice = currentMaxPrice;
        this.winner = winner;
    }

    /**
     * Lấy sản phẩm đang đấu giá.
     * 
     * @return Đối tượng Item.
     */
    public Item getItem(){
        return this.item;
    }
    
    /**
     * Lấy người đăng bán sản phẩm.
     * 
     * @return Đối tượng Seller.
     */
    public Seller getSeller(){
        return this.seller;
    }

    /**
     * Lấy mức giá cao nhất đang được ghi nhận.
     * 
     * @return Giá cao nhất.
     */
    public double getCurrentMaxPrice(){
        return this.currentMaxPrice;
    }
    
    /**
     * Lấy danh sách toàn bộ các lịch sử giao dịch trả giá đã diễn ra,
     * dữ liệu được đóng gói không thể sửa đổi (unmodifiable).
     * 
     * @return Danh sách BidTransaction (Read-only).
     */
    public List<BidTransaction> getHistoryBids(){
        return Collections.unmodifiableList(this.historyBids);
    }
    
    /**
     * Lấy người đang giữ giá cao nhất (Hoặc người chiến thắng chung cuộc).
     * 
     * @return Đối tượng Bidder.
     */
    public Bidder getWinner(){
        return this.winner;
    }
    
    /**
     * Lấy mốc thời gian bắt đầu chính thức của phiên đấu giá.
     * 
     * @return Thời điểm bắt đầu.
     */
    public LocalDateTime getStartTime(){
        return this.startTime;
    }
    
    /**
     * Lấy mốc thời gian dự kiến kết thúc của phiên đấu giá.
     * 
     * @return Thời điểm kết thúc.
     */
    public LocalDateTime getEndTime(){
        return this.endTime;
    }
}
