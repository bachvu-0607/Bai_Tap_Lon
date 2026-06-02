package com.uet.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.uet.domain.entity.auction.BidTransaction;

/**
 * Lớp đại diện cho một mốc lịch sử đặt giá (Data Transfer Object - DTO).
 * Được sử dụng để truyền tải thông tin về một lượt đặt giá cụ thể trong một phiên đấu giá
 * từ Server về Client để hiển thị trên giao diện lịch sử đấu giá.
 */
public class BidHistoryPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String bidderName;
    private final double amount;
    private final LocalDateTime bidTime;
    private final String status;

    /**
     * Khởi tạo một đối tượng BidHistoryPoint với các thông tin trực tiếp.
     * Thường được sử dụng khi truy xuất dữ liệu từ cơ sở dữ liệu.
     * 
     * @param bidderName Tên của người tham gia đặt giá.
     * @param amount Số tiền đặt giá.
     * @param bidTime Thời điểm thực hiện đặt giá.
     * @param status Trạng thái của lượt đặt giá (ví dụ: "WINNING", "OUTBID").
     */
    public BidHistoryPoint(String bidderName, double amount, LocalDateTime bidTime, String status) {
        this.bidderName = bidderName;
        this.amount = amount;
        this.bidTime = bidTime;
        this.status = status;
    }

    /**
     * Khởi tạo một đối tượng BidHistoryPoint từ một giao dịch đặt giá (BidTransaction).
     * Thường được sử dụng để chuyển đổi dữ liệu từ entity sang DTO.
     * 
     * @param bidTransaction Đối tượng giao dịch đặt giá gốc chứa thông tin.
     */
    public BidHistoryPoint(BidTransaction bidTransaction) {
        this.bidderName = bidTransaction.getBidder().getName();
        this.amount = bidTransaction.getBidAmount();
        this.bidTime = bidTransaction.getTime();
        this.status = bidTransaction.getStatus().name();
    }

    /**
     * Lấy tên của người đặt giá (Bidder).
     * 
     * @return Tên người đặt giá.
     */
    public String getBidderName() {
        return bidderName;
    }

    /**
     * Lấy số tiền đã đặt trong lượt giá này.
     * 
     * @return Số tiền đặt giá.
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Lấy thời điểm chính xác mà lượt đặt giá được thực hiện.
     * 
     * @return Thời điểm đặt giá.
     */
    public LocalDateTime getBidTime() {
        return bidTime;
    }

    /**
     * Lấy trạng thái của lượt đặt giá.
     * (Ví dụ: "WINNING" nếu đang dẫn đầu, "OUTBID" nếu đã bị vượt qua).
     * 
     * @return Chuỗi đại diện cho trạng thái của lượt đặt.
     */
    public String getStatus() {
        return status;
    }
}
