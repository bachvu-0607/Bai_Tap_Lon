package com.uet.domain.request;

import java.io.Serializable;

/**
 * Lớp đại diện cho một yêu cầu (Request) chung được gửi từ Client lên Server thông qua Socket.
 * Đây là một Wrapper (lớp bao bọc) chứa loại yêu cầu và dữ liệu đính kèm thực tế.
 */
public class AuctionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Liệt kê các loại hành động/yêu cầu mà Client có thể yêu cầu Server thực hiện.
     */
    public enum RequestType {
        SIGN_IN,
        REGISTER,
        SIGN_OUT,
        BID,
        GET_LIST,
        GET_ONLINE_USERS,
        GET_BID_HISTORY,
        GET_PENDING_AUCTIONS,
        GET_SELLER_PRODUCTS,
        APPROVE_AUCTION,
        REJECT_AUCTION,
        POST_PRODUCT,
        DISCONNECT
    }

    /** Kiểu của yêu cầu này */
    private RequestType type;
    
    /** Dữ liệu chi tiết gửi kèm theo yêu cầu (Có thể là String, hoặc các DTO request khác) */
    private Object data;

    /**
     * Khởi tạo một Wrapper yêu cầu đóng gói để gửi qua mạng.
     * 
     * @param type Kiểu yêu cầu (Ví dụ: SIGN_IN, BID, ...).
     * @param data Dữ liệu đính kèm (Ví dụ: SignInRequest, BidRequest, ...).
     */
    public AuctionRequest(RequestType type, Object data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Lấy loại yêu cầu mà Client đang muốn thực thi.
     * 
     * @return Đối tượng RequestType.
     */
    public RequestType getType() {
        return type;
    }

    /**
     * Lấy phần dữ liệu payload đính kèm theo yêu cầu.
     * 
     * @return Dữ liệu dạng Object (cần được ép kiểu ở phía Server).
     */
    public Object getData() {
        return data;
    }
}
