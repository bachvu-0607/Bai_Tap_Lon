package com.uet.domain.request;

import java.io.Serializable;

public class AuctionApprovalRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;

    public AuctionApprovalRequest(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getAuctionId() {
        return auctionId;
    }
}
