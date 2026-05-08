package com.uet.domain.observer;

import com.uet.domain.entity.auction.Auction;

public class BidNotificationService implements AuctionObserver {
    @Override
    public void update(Auction auction) {
        System.out.println("New bid: " + auction.getCurrentMaxPrice());
    }
}
