package com.uet.domain.entity;

import java.time.LocalDateTime;

public class BidTransaction extends Entity {
    private Bidder bidder;
    private double bidAmount;
    private LocalDateTime time;

    public BidTransaction(Bidder bidder, double bidAmount) {
        
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.time = LocalDateTime.now();
    }



    public Bidder getBidder() {
        return bidder;
    }

    public double getBidAmount() {
        return bidAmount;
    }

    public LocalDateTime getTime() {
        return time;
    }
}
