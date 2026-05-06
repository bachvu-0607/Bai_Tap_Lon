package com.uet.domain.request;

import java.io.Serializable;

public class BidRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final double amount;

    public BidRequest(String auctionId, double amount) {
        this.auctionId = auctionId;
        this.amount = amount;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public double getAmount() {
        return amount;
    }
}
