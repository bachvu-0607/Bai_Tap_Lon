package com.uet.domain.request;

import java.io.Serializable;

public class AuctionRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RequestType {
        SIGN_IN,
        REGISTER,
        SIGN_OUT,
        BID,
        GET_LIST,
        GET_USERS,
        GET_ONLINE_USERS,
        GET_BID_HISTORY,
        GET_PENDING_AUCTIONS,
        GET_SELLER_PRODUCTS,
        APPROVE_AUCTION,
        REJECT_AUCTION,
        POST_PRODUCT,
        SET_AUTO_BID,
        DISABLE_AUTO_BID,
        DISCONNECT,
        BAN_USER
    }

    private RequestType type;
    private Object data;

    public AuctionRequest(RequestType type, Object data) {
        this.type = type;
        this.data = data;
    }

    public RequestType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }
}
