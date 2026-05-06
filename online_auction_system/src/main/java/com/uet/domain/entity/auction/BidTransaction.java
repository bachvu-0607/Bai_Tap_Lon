package com.uet.domain.entity.auction;

import java.time.LocalDateTime;
import com.uet.domain.entity.Entity;
import com.uet.domain.enums.BidStatus;
import com.uet.domain.entity.user.Bidder;

public class BidTransaction extends Entity {
    private Bidder bidder;
    private double bidAmount;
    private LocalDateTime time;
    private BidStatus status;

    public BidTransaction(Bidder bidder, double bidAmount) {
        this(bidder, bidAmount, BidStatus.WINNING);
    }

    public BidTransaction(Bidder bidder, double bidAmount, BidStatus status) {
        super();
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.time = LocalDateTime.now();
        this.status = status;
    }

    public BidTransaction(String id, Bidder bidder, double bidAmount, BidStatus status) {
        super(id);
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.time = LocalDateTime.now();
        this.status = status;
    }

    public boolean isValid() {
        return bidder != null && bidAmount > 0 && time != null && status != null;
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

    public BidStatus getStatus() {
        return status;
    }

    public void setStatus(BidStatus status) {
        this.status = status;
    }
}
