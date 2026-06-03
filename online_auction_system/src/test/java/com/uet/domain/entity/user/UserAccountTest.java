package com.uet.domain.entity.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.uet.domain.exceptions.InsufficientBalanceException;
import com.uet.domain.exceptions.InvalidAdminActionException;
import com.uet.domain.exceptions.InvalidDepositException;
import com.uet.domain.exceptions.InvalidTransactionException;

/**
 * Unit tests for user roles and wallet behavior.
 * The goal is to cover the core account logic without touching database, socket, or JavaFX.
 */
class UserAccountTest {

    @Test
    void bidder_Starts_With_Default_Balance_And_No_Locked_Funds() {
        // Bidder accounts receive default bidding money and start with auto-bid disabled.
        Bidder bidder = new Bidder("C1", "Bidder", "0901", "pw", "HN");

        assertEquals(Bidder.DEFAULT_BALANCE, bidder.getBalance());
        assertEquals(0, bidder.getLockedBalance());
        assertEquals(Bidder.DEFAULT_BALANCE, bidder.getAvailableBalance());
        assertFalse(bidder.isAutoBidEnabled());
        assertTrue(bidder.isActive());
    }

    @Test
    void bidder_Deposit_Lock_Unlock_And_CommitPayment_Update_Balances() throws Exception {
        // Full wallet flow: add money, hold money for winning bid, release part, then pay final amount.
        Bidder bidder = new Bidder("C1", "Bidder", "0901", "pw", "HN");

        bidder.deposit(500_000);
        bidder.lockFunds(400_000);
        bidder.unlockFunds(100_000);
        bidder.commitPayment(300_000);

        assertEquals(1_200_000, bidder.getBalance());
        assertEquals(0, bidder.getLockedBalance());
        assertEquals(1_200_000, bidder.getAvailableBalance());
    }

    @Test
    void bidder_Rejects_Invalid_Money_Operations() throws Exception {
        // Negative/oversized money operations should fail instead of corrupting wallet state.
        Bidder bidder = new Bidder("C1", "Bidder", "0901", "pw", "HN");

        assertThrows(InvalidDepositException.class, () -> bidder.deposit(0));
        assertThrows(InvalidTransactionException.class, () -> bidder.lockFunds(0));
        assertThrows(InsufficientBalanceException.class, () -> bidder.lockFunds(Bidder.DEFAULT_BALANCE + 1));
        assertThrows(InvalidTransactionException.class, () -> bidder.unlockFunds(0));
        assertThrows(InsufficientBalanceException.class, () -> bidder.unlockFunds(1));

        bidder.lockFunds(100);
        assertThrows(InsufficientBalanceException.class, () -> bidder.commitPayment(101));
    }

    @Test
    void bidder_RestoreFunds_Clamps_Invalid_Database_Values() {
        // Restore protects runtime state from negative or impossible values loaded from storage.
        Bidder bidder = new Bidder("C1", "Bidder", "0901", "pw", "HN");

        bidder.restoreFunds(-10, 99);
        assertEquals(0, bidder.getBalance());
        assertEquals(0, bidder.getLockedBalance());

        bidder.restoreFunds(500, 700);
        assertEquals(500, bidder.getBalance());
        assertEquals(500, bidder.getLockedBalance());
    }

    @Test
    void bidder_AutoBid_Can_Be_Enabled_And_Disabled() throws Exception {
        // Auto-bid limit is a configuration value; disabling it clears both flag and limit.
        Bidder bidder = new Bidder("C1", "Bidder", "0901", "pw", "HN");

        bidder.enableAutoBid(500_000);
        assertTrue(bidder.isAutoBidEnabled());
        assertEquals(500_000, bidder.getMaxBidLimit());

        bidder.disableAutoBid();
        assertFalse(bidder.isAutoBidEnabled());
        assertEquals(0, bidder.getMaxBidLimit());
    }

    @Test
    void bidder_AutoBid_Rejects_Invalid_Limit() {
        // Auto-bid should not allow zero or a limit larger than the bidder total balance.
        Bidder bidder = new Bidder("C1", "Bidder", "0901", "pw", "HN");

        assertThrows(InvalidTransactionException.class, () -> bidder.enableAutoBid(0));
        assertThrows(InsufficientBalanceException.class, () -> bidder.enableAutoBid(Bidder.DEFAULT_BALANCE + 1));
    }

    @Test
    void seller_Deposit_Uses_Simple_Available_Balance() throws Exception {
        // Seller does not lock bidding funds, so available balance always equals total balance.
        Seller seller = new Seller("C2", "Seller", "0902", "pw", "HN");

        seller.deposit(600_000);

        assertEquals(600_000, seller.getBalance());
        assertEquals(600_000, seller.getAvailableBalance());
        assertThrows(InvalidDepositException.class, () -> seller.deposit(0));
    }

    @Test
    void admin_Can_Ban_Normal_User_But_Not_Admin_Or_Inactive_User() throws Exception {
        // Domain Admin only changes in-memory active flag; server layer handles database and socket effects.
        Admin admin = new Admin("CA", "Admin", "0999", "pw", "HN");
        Bidder bidder = new Bidder("CB", "Bidder", "0901", "pw", "HN");
        Seller seller = new Seller("CS", "Seller", "0902", "pw", "HN");
        Admin anotherAdmin = new Admin("CA2", "Other Admin", "0998", "pw", "HN");

        admin.banUser(bidder);
        admin.banUser(seller);

        assertFalse(bidder.isActive());
        assertFalse(seller.isActive());
        assertThrows(InvalidAdminActionException.class, () -> admin.banUser(anotherAdmin));
        assertThrows(InvalidAdminActionException.class, () -> admin.banUser(bidder));
    }
}
