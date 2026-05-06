package com.uet.domain.observer;

import com.uet.domain.entity.auction.Auction;

public interface AuctionObserver {
    void update(Auction auction);
}
