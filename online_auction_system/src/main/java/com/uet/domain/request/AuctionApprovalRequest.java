package com.uet.domain.request;

import java.io.Serializable;

/**
 * Lớp DTO (Data Transfer Object) dùng để gửi yêu cầu (request) từ Client lên Server 
 * liên quan đến việc phê duyệt một phiên đấu giá.
 */
public class AuctionApprovalRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Mã định danh của phiên đấu giá cần được duyệt */
    private final String auctionId;

    /**
     * Khởi tạo một yêu cầu phê duyệt phiên đấu giá mới.
     * 
     * @param auctionId ID của phiên đấu giá mà Admin muốn phê duyệt hoặc từ chối.
     */
    public AuctionApprovalRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    /**
     * Lấy ID của phiên đấu giá đang được yêu cầu xử lý.
     * 
     * @return Mã định danh phiên đấu giá.
     */
    public String getAuctionId() {
        return auctionId;
    }
}
