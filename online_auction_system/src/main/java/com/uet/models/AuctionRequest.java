package com.uet.models;

import java.io.Serializable;

public class AuctionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RequestType {
        SIGN_IN,      // Client muốn đăng nhập
        REGISTER,
        DISCONNECT,
        SIGN_OUT,
        BID,        // Client muốn trả giá
        GET_LIST    // Client muốn lấy danh sách đồ đấu giá
    }

    private RequestType type;
    private Object data; // Chỗ này để chứa thông tin đi kèm (ví dụ: tên User hoặc giá tiền)

    //construtor
    public AuctionRequest(RequestType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public RequestType getType() { return type; }
    public Object getData() { return data; }
}