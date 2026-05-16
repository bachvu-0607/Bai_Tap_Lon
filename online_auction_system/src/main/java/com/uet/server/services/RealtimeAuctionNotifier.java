package com.uet.server.services;

import com.uet.domain.AuctionSummary;
import com.uet.domain.entity.auction.Auction;
import com.uet.domain.event.ServerEvent;
import com.uet.domain.event.ServerEventType;
import com.uet.domain.observer.AuctionObserver;

public class RealtimeAuctionNotifier implements AuctionObserver{
    private final AuctionManager auctionManager;

    public RealtimeAuctionNotifier(AuctionManager auctionManager){
        this.auctionManager = auctionManager;
    }

    @Override
    public void update(Auction auction){
        ServerEvent event = new ServerEvent(ServerEventType.AUCTION_UPDATED, new AuctionSummary(auction));
        auctionManager.broadcast(event);
    }
}
