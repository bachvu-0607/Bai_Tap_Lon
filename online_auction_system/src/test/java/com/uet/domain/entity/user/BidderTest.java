package com.uet.domain.entity.user;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.uet.domain.exceptions.InsufficientBalanceException;
import com.uet.domain.exceptions.InvalidDepositException;
import com.uet.domain.exceptions.InvalidTransactionException;

class BidderTest {

    private Bidder bidder;

    @BeforeEach
    void setUp() throws Exception {
        bidder = new Bidder("B1", "C1", "Nguyen Van A", "0901", "pw", "HN");
        bidder.deposit(1000);
    }

    // Kiểm tra nạp tiền hợp lệ → số dư tăng đúng
    @Test
    void deposit_ValidAmount_IncreasesBalance() throws Exception {
        bidder.deposit(500);
        assertEquals(1500, bidder.getBalance());
    }

    // Kiểm tra nạp tiền âm hoặc bằng 0 → phải ném ngoại lệ
    @Test
    void deposit_NegativeAmount_ThrowsException() {
        assertThrows(InvalidDepositException.class, () -> bidder.deposit(-100));
        assertThrows(InvalidDepositException.class, () -> bidder.deposit(0));
    }

    // Kiểm tra tạm giữ tiền hợp lệ → lockedBalance tăng, availableBalance giảm
    @Test
    void lockFunds_ValidAmount_UpdatesBalances() throws Exception {
        bidder.lockFunds(300);
        assertEquals(300, bidder.getLockedBalance());
        assertEquals(700, bidder.getAvailableBalance());
        assertEquals(1000, bidder.getBalance()); // balance gốc không đổi
    }

    // Kiểm tra tạm giữ nhiều hơn số dư khả dụng → phải ném ngoại lệ
    @Test
    void lockFunds_ExceedsAvailableBalance_ThrowsException() throws Exception {
        bidder.lockFunds(800); // khóa 800, còn 200 khả dụng
        // cố khóa thêm 500 → vượt số dư khả dụng (200) → exception
        assertThrows(InsufficientBalanceException.class, () -> bidder.lockFunds(500));
    }

    // Kiểm tra mở khóa tiền → lockedBalance giảm, availableBalance tăng lại
    @Test
    void unlockFunds_ValidAmount_RestoresAvailableBalance() throws Exception {
        bidder.lockFunds(400);
        bidder.unlockFunds(400);
        assertEquals(0, bidder.getLockedBalance());
        assertEquals(1000, bidder.getAvailableBalance());
    }

    // Kiểm tra mở khóa nhiều hơn số tiền đang bị giữ → phải ném ngoại lệ
    @Test
    void unlockFunds_ExceedsLockedBalance_ThrowsException() throws Exception {
        bidder.lockFunds(200);
        assertThrows(InsufficientBalanceException.class, () -> bidder.unlockFunds(500));
    }

    // Kiểm tra thanh toán cuối → balance và lockedBalance đều giảm đúng
    @Test
    void commitPayment_ValidAmount_DeductsFromBothBalances() throws Exception {
        bidder.lockFunds(600);
        bidder.commitPayment(600);
        assertEquals(400, bidder.getBalance());
        assertEquals(0, bidder.getLockedBalance());
    }

    // Kiểm tra thanh toán nhiều hơn tiền đang giữ → phải ném ngoại lệ
    @Test
    void commitPayment_ExceedsLockedBalance_ThrowsException() throws Exception {
        bidder.lockFunds(200);
        assertThrows(InsufficientBalanceException.class, () -> bidder.commitPayment(500));
    }

    // Kiểm tra bật auto-bid hợp lệ → trạng thái enabled
    @Test
    void enableAutoBid_ValidLimit_SetsEnabled() throws Exception {
        bidder.enableAutoBid(800);
        assertTrue(bidder.isAutoBidEnabled());
        assertEquals(800, bidder.getMaxBidLimit());
    }

    // Kiểm tra bật auto-bid với limit vượt số dư → phải ném ngoại lệ
    @Test
    void enableAutoBid_ExceedsBalance_ThrowsException() {
        assertThrows(InsufficientBalanceException.class, () -> bidder.enableAutoBid(2000));
    }

    // Kiểm tra tắt auto-bid → reset về trạng thái ban đầu
    @Test
    void disableAutoBid_ResetsState() throws Exception {
        bidder.enableAutoBid(500);
        bidder.disableAutoBid();
        assertFalse(bidder.isAutoBidEnabled());
        assertEquals(0, bidder.getMaxBidLimit());
    }

    // Kiểm tra canAfford đúng theo số dư khả dụng (balance - lockedBalance)
    @Test
    void canAfford_ReflectsAvailableBalance() throws Exception {
        bidder.lockFunds(700); // còn 300 khả dụng
        assertTrue(bidder.canAfford(300));
        assertFalse(bidder.canAfford(301));
    }
}
