package com.uet.domain.entity.auction;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void restoredAuction_Allows_NewBid_When_PreviousWinnerHasNoLockedFunds() throws Exception {
        Auction auction = runningAuction(10);
        Bidder restoredWinner = bidder("B1", 1_000);
        Bidder newBidder = bidder("B2", 1_000);
        auction.restoreState(AuctionStatus.RUNNING, 110, restoredWinner);

        auction.placeBid(newBidder, 130);

        assertEquals(newBidder, auction.getWinner());
        assertEquals(130, auction.getCurrentMaxPrice());
        assertEquals(0, restoredWinner.getLockedBalance());
        assertEquals(130, newBidder.getLockedBalance());
        assertEquals(1, auction.getHistoryBids().size());
        assertEquals(BidStatus.WINNING, auction.getHistoryBids().get(0).getStatus());
    }

    @Test
    void currentWinner_Can_Rebid_Using_PreviouslyLockedFunds() throws Exception {
        Auction auction = runningAuction(10);
        Bidder bidder = bidder("B1", 0);
        auction.placeBid(bidder, Bidder.DEFAULT_BALANCE - 20);

        auction.placeBid(bidder, Bidder.DEFAULT_BALANCE - 10);

        assertEquals(Bidder.DEFAULT_BALANCE - 10, auction.getCurrentMaxPrice());
        assertEquals(Bidder.DEFAULT_BALANCE - 10, bidder.getLockedBalance());
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

    @Test
    void cancel_Bidder_Participation_Removes_Winner_And_Unlocks_Funds() throws Exception {
        Auction auction = runningAuction(10);
        Bidder bidder = bidder("B1", 1_000);
        auction.placeBid(bidder, 110);

        boolean changed = auction.cancelBidderParticipation(bidder.getId());

        assertTrue(changed);
        assertEquals(null, auction.getWinner());
        assertEquals(100, auction.getCurrentMaxPrice());
        assertEquals(0, bidder.getLockedBalance());
        assertEquals(BidStatus.CANCELED, auction.getHistoryBids().get(0).getStatus());
    }

    @Test
    void cancel_Current_Winner_Promotes_Previous_Valid_Bid() throws Exception {
        Auction auction = runningAuction(10);
        Bidder firstBidder = bidder("B1", 1_000);
        Bidder bannedWinner = bidder("B2", 1_000);
        auction.placeBid(firstBidder, 110);
        auction.placeBid(bannedWinner, 130);

        boolean changed = auction.cancelBidderParticipation(bannedWinner.getId());

        assertTrue(changed);
        assertEquals(firstBidder, auction.getWinner());
        assertEquals(110, auction.getCurrentMaxPrice());
        assertEquals(110, firstBidder.getLockedBalance());
        assertEquals(0, bannedWinner.getLockedBalance());
        assertEquals(BidStatus.WINNING, auction.getHistoryBids().get(0).getStatus());
        assertEquals(BidStatus.CANCELED, auction.getHistoryBids().get(1).getStatus());
    }

    @Test
    void cancel_Seller_Auction_Cancels_Auction_And_Unlocks_Winner_Funds() throws Exception {
        Auction auction = runningAuction(10);
        Bidder bidder = bidder("B1", 1_000);
        auction.placeBid(bidder, 110);

        boolean changed = auction.cancelBecauseSellerBanned();

        assertTrue(changed);
        assertEquals(AuctionStatus.CANCELED, auction.getStatus());
        assertEquals(ItemStatus.REMOVED, auction.getItem().getStatus());
        assertEquals(null, auction.getWinner());
        assertEquals(100, auction.getCurrentMaxPrice());
        assertEquals(0, bidder.getLockedBalance());
        assertEquals(BidStatus.CANCELED, auction.getHistoryBids().get(0).getStatus());
    }

    @Test
    void extend_End_Time_When_Bid_Is_Close_To_End() {
        Auction auction = auctionEndingInMinutes(2);
        LocalDateTime oldEndTime = auction.getEndTime();

        boolean extended = auction.extendEndTimeIfCloseToEnd(5 * 60, 5 * 60);

        assertTrue(extended);
        assertEquals(oldEndTime.plusMinutes(5), auction.getEndTime());
    }

    @Test
    void does_Not_Extend_End_Time_When_Bid_Is_Not_Close_To_End() {
        Auction auction = auctionEndingInMinutes(10);
        LocalDateTime oldEndTime = auction.getEndTime();

        boolean extended = auction.extendEndTimeIfCloseToEnd(5 * 60, 5 * 60);

        assertEquals(false, extended);
        assertEquals(oldEndTime, auction.getEndTime());
    }

    private Auction runningAuction(double minIncrement) {
        return auctionEndingInMinutes(10, minIncrement);
    }

    private Auction auctionEndingInMinutes(long minutesUntilEnd) {
        return auctionEndingInMinutes(minutesUntilEnd, 10);
    }

    private Auction auctionEndingInMinutes(long minutesUntilEnd, double minIncrement) {
        Item item = new Electronics("I1", "Laptop", 100);
        Seller seller = new Seller("S1", "C1", "Seller", "0901", "pw", "HN");
        Auction auction = new Auction(
                "A1",
                item,
                seller,
                LocalDateTime.now().minusMinutes(1),
                LocalDateTime.now().plusMinutes(minutesUntilEnd),
                minIncrement);
        auction.updateStatus();
        return auction;
    }

    private Bidder bidder(String id, double balance) throws Exception {
        Bidder bidder = new Bidder(id, "C" + id, "Bidder " + id, "09" + id, "pw", "HN");
        if (balance > 0) {
            bidder.deposit(balance);
        }
        return bidder;
    }
}
