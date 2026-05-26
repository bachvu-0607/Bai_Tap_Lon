package com.uet.domain.entity.auction;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        Bidder firstBidder = bidder("B1", 1_000);
        Bidder secondBidder = bidder("B2", 1_000);

        auction.placeBid(firstBidder, 110);
        auction.placeBid(secondBidder, 130);

        assertEquals(BidStatus.OUTBID, auction.getHistoryBids().get(0).getStatus());
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
        double balanceBeforePayment = bidder.getBalance();
        double winningPrice = auction.getCurrentMaxPrice();

        auction.confirmPayment();

        assertEquals(AuctionStatus.PAID, auction.getStatus());
        assertEquals(BidStatus.PAID, auction.getHistoryBids().get(0).getStatus());
        assertEquals(ItemStatus.SOLD, auction.getItem().getStatus());
        assertEquals(balanceBeforePayment - winningPrice, bidder.getBalance());
        assertEquals(0, bidder.getLockedBalance());
    }

    private Auction runningAuction(double minIncrement) {
        Item item = new Electronics("I1", "Laptop", 100);
        Seller seller = new Seller("S1", "C1", "Seller", "0901", "pw", "HN");
        Auction auction = new Auction(
                "A1",
                item,
                seller,
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
    @Test
    void testAntiSnipingExtends() throws Exception {
        Item item = new Electronics("I1", "Laptop", 100);
        Seller seller = new Seller("S1", "C1", "Seller", "09", "pw", "HN");
        Auction auction = new Auction("A1", item, seller,
            LocalDateTime.now().minusMinutes(1),
            LocalDateTime.now().plusSeconds(30),
            10);
        auction.updateStatusQuietly();

        LocalDateTime originalEnd = auction.getEndTime();
        Bidder bidder = new Bidder("B1", "C2", "Bidder", "08", "pw", "HN");
        bidder.deposit(500);

        auction.placeBid(bidder, 110.0);
        boolean extended = auction.checkAndExtendIfSniped();

        assertTrue(extended, "Phải gia hạn vì bid rơi vào 60s cuối");
        assertTrue(auction.getEndTime().isAfter(originalEnd), "endTime phải tăng lên");
    }

    @Test
    void testNoExtensionOutsideSnipeWindow() throws Exception {
        Auction auction = new Auction("A2", new Electronics("I2", "Phone", 50),
            new Seller("S2", "C3", "Seller", "07", "pw", "HN"),
            LocalDateTime.now().minusMinutes(1),
            LocalDateTime.now().plusMinutes(5),
            10);
        auction.updateStatusQuietly();

        LocalDateTime originalEnd = auction.getEndTime();
        Bidder bidder = new Bidder("B2", "C4", "Bidder", "06", "pw", "HN");
        bidder.deposit(500);
        auction.placeBid(bidder, 60.0);

        boolean extended = auction.checkAndExtendIfSniped();
        assertFalse(extended, "Không gia hạn vì còn nhiều thời gian");
        assertEquals(originalEnd, auction.getEndTime());
    }
}
