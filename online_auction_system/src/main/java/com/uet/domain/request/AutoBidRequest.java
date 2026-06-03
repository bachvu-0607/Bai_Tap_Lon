package com.uet.domain.request;

import java.io.Serializable;

public class AutoBidRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final double maxBidLimit;

    public AutoBidRequest(String auctionId, double maxBidLimit) {
        this.auctionId = auctionId;
        this.maxBidLimit = maxBidLimit;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public double getMaxBidLimit() {
        return maxBidLimit;
    }
}
