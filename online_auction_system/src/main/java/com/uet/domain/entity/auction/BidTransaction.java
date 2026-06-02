package com.uet.domain.entity.auction;

import java.time.LocalDateTime;
import com.uet.domain.entity.Entity;
import com.uet.domain.enums.BidStatus;
import com.uet.domain.entity.user.Bidder;

/**
 * Lớp đại diện cho một giao dịch đặt giá (lượt đấu giá) của người dùng.
 * Kế thừa Entity để có định danh và thời gian. Lưu trữ thông tin về người đặt,
 * số tiền đặt và trạng thái hiện tại của lượt đặt giá đó.
 */
public class BidTransaction extends Entity {
    
    /** Người tham gia đặt giá */
    private Bidder bidder;
    
    /** Số tiền được đặt trong lượt này */
    private double bidAmount;
    
    /** Thời điểm chính xác thực hiện đặt giá */
    private LocalDateTime time;
    
    /** Trạng thái của lượt đặt giá (ví dụ: đang dẫn đầu, đã bị vượt mặt, v.v.) */
    private BidStatus status;

    /**
     * Khởi tạo giao dịch đặt giá mới, mặc định trạng thái là Đang dẫn đầu (WINNING).
     * 
     * @param bidder Người dùng thực hiện đặt giá.
     * @param bidAmount Số tiền đặt giá.
     */
    public BidTransaction(Bidder bidder, double bidAmount) {
        this(bidder, bidAmount, BidStatus.WINNING);
    }

    /**
     * Khởi tạo giao dịch đặt giá với trạng thái tùy chỉnh.
     * 
     * @param bidder Người đặt giá.
     * @param bidAmount Số tiền.
     * @param status Trạng thái khởi tạo.
     */
    public BidTransaction(Bidder bidder, double bidAmount, BidStatus status) {
        super();
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.time = LocalDateTime.now();
        this.status = status;
    }

    /**
     * Khởi tạo giao dịch đặt giá từ dữ liệu cũ có sẵn ID (Thường dùng khi khôi phục từ DB).
     * 
     * @param id Mã định danh giao dịch.
     * @param bidder Người đặt giá.
     * @param bidAmount Số tiền đặt.
     * @param status Trạng thái hiện tại.
     */
    public BidTransaction(String id, Bidder bidder, double bidAmount, BidStatus status) {
        super(id);
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.time = LocalDateTime.now();
        this.status = status;
    }

    /**
     * Kiểm tra tính hợp lệ của giao dịch đặt giá (có người đặt, tiền hợp lệ, thời gian và trạng thái rõ ràng).
     * 
     * @return true nếu dữ liệu đầy đủ và đúng đắn, false nếu có sai sót.
     */
    public boolean isValid() {
        return bidder != null && bidAmount > 0 && time != null && status != null;
    }

    /**
     * Lấy thông tin người dùng đã thực hiện giao dịch đặt giá này.
     * 
     * @return Đối tượng người mua (Bidder).
     */
    public Bidder getBidder() {
        return bidder;
    }

    /**
     * Lấy số tiền đã đặt trong lượt này.
     * 
     * @return Số tiền đặt giá.
     */
    public double getBidAmount() {
        return bidAmount;
    }

    /**
     * Lấy thời điểm giao dịch đặt giá được xác nhận.
     * 
     * @return Thời điểm đặt giá.
     */
    public LocalDateTime getTime() {
        return time;
    }

    /**
     * Lấy trạng thái hiện tại của giao dịch.
     * 
     * @return Trạng thái đặt giá (BidStatus).
     */
    public BidStatus getStatus() {
        return status;
    }

    /**
     * Cập nhật trạng thái của giao dịch đặt giá.
     * (Ví dụ: Từ WINNING sang OUTBID nếu bị người khác trả cao hơn).
     * 
     * @param status Trạng thái mới.
     */
    public void setStatus(BidStatus status) {
        this.status = status;
    }
}
