package com.uet.domain.entity.auction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.uet.domain.entity.user.Bidder;
import com.uet.domain.enums.BidStatus;

/**
 * Unit tests for BidTransaction data integrity.
 * Bid history is used by charts, fallback winner promotion, and database reload.
 */
class BidTransactionTest {

    @Test
    void newBidTransaction_Defaults_To_Winning_And_Current_Time() {
        // New bid records created during live bidding should start as WINNING.
        Bidder bidder = bidder("B1");
        BidTransaction bid = new BidTransaction(bidder, 1_000);

        assertTrue(bid.isValid());
        assertEquals(bidder, bid.getBidder());
        assertEquals(1_000, bid.getBidAmount());
        assertEquals(BidStatus.WINNING, bid.getStatus());
        assertTrue(bid.getTime().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void restoredBidTransaction_Keeps_Id_Time_And_Status() {
        // Database reload needs to restore exact bid id, time, and status.
        Bidder bidder = bidder("B1");
        LocalDateTime bidTime = LocalDateTime.of(2026, 6, 3, 9, 0);
        BidTransaction bid = new BidTransaction("BID-1", bidder, 2_000, bidTime, BidStatus.OUTBID);

        assertEquals("BID-1", bid.getId());
        assertEquals(bidTime, bid.getTime());
        assertEquals(BidStatus.OUTBID, bid.getStatus());
        assertTrue(bid.isValid());
    }

    @Test
    void bidTransaction_Is_Invalid_When_Core_Data_Is_Missing() {
        // A bid without bidder or positive amount should not be considered valid domain data.
        BidTransaction missingBidder = new BidTransaction(null, 1_000);
        BidTransaction invalidAmount = new BidTransaction(bidder("B1"), 0);

        assertFalse(missingBidder.isValid());
        assertFalse(invalidAmount.isValid());
    }

    private Bidder bidder(String id) {
        return new Bidder(id, "C" + id, "Bidder " + id, "09" + id, "pw", "HN");
    }
}
