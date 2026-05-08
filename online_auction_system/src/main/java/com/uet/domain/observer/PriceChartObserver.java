package com.uet.domain.observer;

import com.uet.domain.entity.auction.Auction;

public class PriceChartObserver implements AuctionObserver {
    @Override
    public void update(Auction auction) {
        System.out.println("Price chart updated: " + auction.getCurrentMaxPrice());
    }
}
