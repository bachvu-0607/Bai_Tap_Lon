package com.uet.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.uet.domain.entity.auction.Auction;

public class AuctionSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String auctionId;
    private final String itemName;
    private final String category;
    private final String sellerName;
    private final String currentWinnerName;
    private final double currentPrice;
    private final double minIncrement;
    private final double minimumNextBid;
    private final String status;
    private final LocalDateTime endTime;

    public AuctionSummary(Auction auction) {
        this.auctionId = auction.getId();
        this.itemName = auction.getItem().getName();
        this.category = auction.getItem().getCategory();
        this.sellerName = auction.getSeller().getName();
        this.currentWinnerName = auction.getWinner() == null ? "-" : auction.getWinner().getName();
        this.currentPrice = auction.getCurrentMaxPrice();
        this.minIncrement = auction.getMinIncrement();
        this.minimumNextBid = auction.getMinimumNextBid();
        this.status = auction.getStatus().name();
        this.endTime = auction.getEndTime();
    }

    public String getAuctionId() { return auctionId; }
    public String getItemName() { return itemName; }
    public String getCategory() { return category; }
    public String getSellerName() { return sellerName; }
    public String getCurrentWinnerName() { return currentWinnerName; }
    public double getCurrentPrice() { return currentPrice; }
    public double getMinIncrement() { return minIncrement; }
    public double getMinimumNextBid() { return minimumNextBid; }
    public String getStatus() { return status; }
    public LocalDateTime getEndTime() { return endTime; }
}
