package com.uet.domain.request;

import java.io.Serializable;

/**
 * Lớp DTO chứa thông tin khi một người dùng gửi yêu cầu đặt giá (Bid) lên hệ thống.
 */
public class BidRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã định danh của phiên đấu giá mà người dùng muốn tham gia đặt giá */
    private final String auctionId;
    
    /** Số tiền mà người dùng đề xuất (phải lớn hơn hoặc bằng mức tối thiểu của hệ thống) */
    private final double amount;

    /**
     * Khởi tạo một yêu cầu đặt giá mới.
     * 
     * @param auctionId Mã ID của phiên đấu giá.
     * @param amount Số tiền đặt giá.
     */
    public BidRequest(String auctionId, double amount) {
        this.auctionId = auctionId;
        this.amount = amount;
    }

    /**
     * Lấy ID của phiên đấu giá cần đặt.
     * 
     * @return Chuỗi ID phiên đấu giá.
     */
    public String getAuctionId() {
        return auctionId;
    }

    /**
     * Lấy số tiền người dùng muốn đặt.
     * 
     * @return Số tiền đấu giá.
     */
    public double getAmount() {
        return amount;
    }
}
