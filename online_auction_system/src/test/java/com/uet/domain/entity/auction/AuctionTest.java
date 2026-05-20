package com.uet.domain.entity.auction;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.uet.domain.entity.item.Electronics;
import com.uet.domain.entity.item.Item;
import com.uet.domain.entity.user.Bidder;
import com.uet.domain.entity.user.Seller;
import com.uet.domain.enums.AuctionStatus;
import com.uet.domain.enums.BidStatus;
import com.uet.domain.enums.ItemStatus;
import com.uet.domain.exceptions.InvalidBidException;

class AuctionTest {

    @Test
    void placeBid_Rejects_Amount_Below_Minimum_Increment() throws Exception {
        Auction auction = runningAuction(10);
        Bidder bidder = bidder("B1", 1_000);

        assertThrows(InvalidBidException.class, () -> auction.placeBid(bidder, 105));
    }

    @Test
    void placeBid_Accepts_Minimum_Bid_And_NotifiesObservers() throws Exception {
        Auction auction = runningAuction(10);
        Bidder bidder = bidder("B1", 1_000);
        AtomicInteger notificationCount = new AtomicInteger();
        auction.addObserver(updatedAuction -> notificationCount.incrementAndGet());

        auction.placeBid(bidder, 110);

        assertEquals(110, auction.getCurrentMaxPrice());
        assertEquals(bidder, auction.getWinner());
        assertEquals(1, notificationCount.get());
    }

    @Test
    void newerBid_Marks_PreviousBid_As_Outbid() throws Exception {
        Auction auction = runningAuction(10);
        Bidder firstBidder  = bidder("B1", 1_000);
        Bidder secondBidder = bidder("B2", 1_000);

        auction.placeBid(firstBidder, 110);
        auction.placeBid(secondBidder, 130);

        assertEquals(BidStatus.OUTBID,  auction.getHistoryBids().get(0).getStatus());
        assertEquals(BidStatus.WINNING, auction.getHistoryBids().get(1).getStatus());
        assertEquals(secondBidder, auction.getWinner());
        assertEquals(130, auction.getCurrentMaxPrice());
    }

    @Test
    void confirm_Payment_Marks_Item_As_Sold() throws Exception {
        Auction auction = runningAuction(10);
        Bidder bidder = bidder("B1", 1_000);
        auction.placeBid(bidder, 110);
        auction.setStatus(AuctionStatus.FINISHED);

        auction.confirmPayment();

        assertEquals(AuctionStatus.PAID, auction.getStatus());
        assertEquals(BidStatus.PAID, auction.getHistoryBids().get(0).getStatus());
        assertEquals(ItemStatus.SOLD, auction.getItem().getStatus());
        assertEquals(890, bidder.getBalance());
        assertEquals(0, bidder.getLockedBalance());
    }

    @Test
    void placeBid_Near_EndTime_Extends_Auction_Duration() throws Exception {
        Item item = new Electronics("I2", "Điện thoại", 100);
        Seller seller = new Seller("S1", "C1", "Seller", "0901", "pw", "HN");
        LocalDateTime endingSoon = LocalDateTime.now().plusMinutes(3);

        Auction auction = new Auction(
            "A2", item, seller,
            LocalDateTime.now().minusMinutes(1), endingSoon, 10
        );
        auction.updateStatus();

        Bidder bidder = bidder("B1", 1_000);
        auction.placeBid(bidder, 150);

        assertEquals(endingSoon.plusMinutes(5), auction.getEndTime());
    }

    @Test
    void placeBid_NotNear_EndTime_Does_Not_Extend_Duration() throws Exception {
        Auction auction = runningAuction(10);
        LocalDateTime originalEndTime = auction.getEndTime();
        Bidder bidder = bidder("B1", 1_000);

        auction.placeBid(bidder, 110);

        assertEquals(originalEndTime, auction.getEndTime());
    }

    @Test
    void registerAutoBid_Automatically_Outbids_Rival() throws Exception {
        Auction auction = runningAuction(10);
        Bidder daiGiaA = bidder("A1", 5_000);
        Bidder khachB  = bidder("B1", 2_000);

        auction.registerAutoBid(daiGiaA, 1_000);
        assertEquals(110, auction.getCurrentMaxPrice());
        assertEquals(daiGiaA, auction.getWinner());

        auction.placeBid(khachB, 200);
        assertEquals(210, auction.getCurrentMaxPrice());
        assertEquals(daiGiaA, auction.getWinner());
        assertEquals(0, khachB.getLockedBalance());
        assertEquals(210, daiGiaA.getLockedBalance());
    }

    @Test
    void autoBid_Stops_At_MaxLimit_When_Rival_Exceeds_It() throws Exception {
        Auction auction = runningAuction(10);
        Bidder daiGiaA = bidder("A1", 5_000);
        Bidder khachB  = bidder("B1", 5_000);

        auction.registerAutoBid(daiGiaA, 300);
        auction.placeBid(khachB, 350);

        assertEquals(350, auction.getCurrentMaxPrice());
        assertEquals(khachB, auction.getWinner());
    }

    @Test
    void registerAutoBid_Throws_When_Auction_Not_Running() throws Exception {
        Item item = new Electronics("I3", "Máy tính", 100);
        Seller seller = new Seller("S1", "C1", "Seller", "0901", "pw", "HN");
        Auction auction = new Auction(
            "A3", item, seller,
            LocalDateTime.now().plusMinutes(10),
            LocalDateTime.now().plusMinutes(60), 10
        );
        Bidder bidder = bidder("B1", 1_000);

        assertThrows(InvalidBidException.class,
            () -> auction.registerAutoBid(bidder, 500));
    }

    @Test
    void multipleAutoBidders_HighestLimit_Wins() throws Exception {
        Auction auction = runningAuction(10);
        Bidder daiGiaA = bidder("A1", 5_000);
        Bidder daiGiaB = bidder("B1", 5_000);

        auction.registerAutoBid(daiGiaA, 500);
        auction.registerAutoBid(daiGiaB, 800);

        assertEquals(daiGiaB, auction.getWinner());
    }

    private Auction runningAuction(double minIncrement) {
        Item item = new Electronics("I1", "Laptop", 100);
        Seller seller = new Seller("S1", "C1", "Seller", "0901", "pw", "HN");
        Auction auction = new Auction(
                "A1", item, seller,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(10),
                minIncrement);
        auction.updateStatus();
        return auction;
    }

    private Bidder bidder(String id, double balance) throws Exception {
        Bidder bidder = new Bidder(id, "C" + id, "Bidder " + id, "09" + id, "pw", "HN");
        bidder.deposit(balance);
        return bidder;
    }
}