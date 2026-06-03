package com.uet.domain.summary;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.uet.domain.entity.auction.BidTransaction;

public class BidHistoryPoint implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String bidderName;
    private final double amount;
    private final LocalDateTime bidTime;
    private final String status;

    public BidHistoryPoint(String bidderName, double amount, LocalDateTime bidTime, String status) {
        this.bidderName = bidderName;
        this.amount = amount;
        this.bidTime = bidTime;
        this.status = status;
    }

    public BidHistoryPoint(BidTransaction bidTransaction) {
        this.bidderName = bidTransaction.getBidder().getName();
        this.amount = bidTransaction.getBidAmount();
        this.bidTime = bidTransaction.getTime();
        this.status = bidTransaction.getStatus().name();
    }

    public String getBidderName() {
        return bidderName;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getBidTime() {
        return bidTime;
    }

    public String getStatus() {
        return status;
    }
}
