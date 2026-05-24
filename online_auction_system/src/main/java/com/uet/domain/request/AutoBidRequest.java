package com.uet.domain.request;

import java.io.Serializable;

/**
 * DTO chứa thông tin yêu cầu đặt auto-bid từ Bidder gửi lên Server.
 *
 * Hai tham số người dùng cần cung cấp:
 *   - maxBid    : Giá tối đa họ chấp nhận trả (hệ thống không tự động vượt qua con số này)
 *   - increment : Bước giá mỗi lần hệ thống tự đặt (bao nhiêu tiền tăng thêm so với giá đang cao nhất)
 */
public class AutoBidRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId; // ID phiên đấu giá muốn auto-bid
    private final double maxBid;    // Giá tối đa bidder sẵn sàng trả
    private final double increment; // Bước giá tự động mỗi lần đặt

    public AutoBidRequest(String auctionId, double maxBid, double increment) {
        this.auctionId = auctionId;
        this.maxBid    = maxBid;
        this.increment = increment;
    }

    public String getAuctionId() { return auctionId; }
    public double getMaxBid()    { return maxBid; }
    public double getIncrement() { return increment; }
}
