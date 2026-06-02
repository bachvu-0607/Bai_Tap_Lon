package com.uet.domain.entity.user;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Happy case tests cho lớp Bidder.
 * Kiểm tra các chức năng tài chính: nạp tiền, tạm giữ, hoàn trả, thanh toán, đấu giá tự động.
 */
class BidderTest {

    private Bidder bidder;

    @BeforeEach
    void setUp() {
        // Bidder mới tạo có balance mặc định = 1,000,000
        bidder = new Bidder("CCCD001", "Nguyen Van A", "0901234567", "password123", "Ha Noi");
    }

    // ======================== KHỞI TẠO ========================

    @Test
    @DisplayName("Khởi tạo Bidder mới — số dư mặc định 1,000,000, không có tiền bị tạm giữ")
    void newBidder_Has_Default_Balance() {
        assertEquals(1_000_000, bidder.getBalance());
        assertEquals(0, bidder.getLockedBalance());
        assertEquals(1_000_000, bidder.getAvailableBalance());
        assertFalse(bidder.isAutoBidEnabled());
    }

    @Test
    @DisplayName("Khởi tạo Bidder với ID — thuộc tính cá nhân được gán đúng")
    void newBidder_WithId_Properties_Set_Correctly() {
        Bidder b = new Bidder("USER-123", "CCCD002", "Tran Van B", "0912345678", "pw", "HCM");
        assertEquals("USER-123", b.getId());
        assertEquals("Tran Van B", b.getName());
        assertEquals("CCCD002", b.getCitizenId());
        assertEquals("0912345678", b.getPhoneNumber());
        assertEquals("HCM", b.getAddress());
        assertTrue(b.isActive());
    }

    // ======================== NẠP TIỀN ========================

    @Test
    @DisplayName("Nạp tiền thành công — số dư tăng đúng")
    void deposit_Increases_Balance() throws Exception {
        bidder.deposit(500_000);
        assertEquals(1_500_000, bidder.getBalance());
        assertEquals(1_500_000, bidder.getAvailableBalance());
    }

    @Test
    @DisplayName("Nạp nhiều lần — số dư cộng dồn chính xác")
    void deposit_Multiple_Times_Accumulates() throws Exception {
        bidder.deposit(100_000);
        bidder.deposit(200_000);
        bidder.deposit(300_000);
        assertEquals(1_600_000, bidder.getBalance());
    }

    // ======================== TẠM GIỮ TIỀN ========================

    @Test
    @DisplayName("Tạm giữ tiền thành công — lockedBalance tăng, availableBalance giảm")
    void lockFunds_Reduces_AvailableBalance() throws Exception {
        bidder.lockFunds(300_000);
        assertEquals(1_000_000, bidder.getBalance());       // Tổng không đổi
        assertEquals(300_000, bidder.getLockedBalance());    // Bị giữ 300k
        assertEquals(700_000, bidder.getAvailableBalance()); // Khả dụng giảm
    }

    @Test
    @DisplayName("Tạm giữ nhiều lần — lockedBalance cộng dồn")
    void lockFunds_Multiple_Times_Accumulates() throws Exception {
        bidder.lockFunds(200_000);
        bidder.lockFunds(300_000);
        assertEquals(500_000, bidder.getLockedBalance());
        assertEquals(500_000, bidder.getAvailableBalance());
    }

    // ======================== HOÀN TRẢ TIỀN ========================

    @Test
    @DisplayName("Hoàn trả tiền thành công — lockedBalance giảm, availableBalance tăng")
    void unlockFunds_Restores_AvailableBalance() throws Exception {
        bidder.lockFunds(400_000);
        bidder.unlockFunds(400_000);
        assertEquals(0, bidder.getLockedBalance());
        assertEquals(1_000_000, bidder.getAvailableBalance());
    }

    @Test
    @DisplayName("Hoàn trả một phần — chỉ giảm đúng số tiền hoàn")
    void unlockFunds_Partial_Amount() throws Exception {
        bidder.lockFunds(500_000);
        bidder.unlockFunds(200_000);
        assertEquals(300_000, bidder.getLockedBalance());
        assertEquals(700_000, bidder.getAvailableBalance());
    }

    // ======================== THANH TOÁN ========================

    @Test
    @DisplayName("Thanh toán thành công — balance và lockedBalance cùng giảm")
    void commitPayment_Deducts_Balance_And_LockedBalance() throws Exception {
        bidder.lockFunds(300_000);
        bidder.commitPayment(300_000);
        assertEquals(700_000, bidder.getBalance());
        assertEquals(0, bidder.getLockedBalance());
        assertEquals(700_000, bidder.getAvailableBalance());
    }

    @Test
    @DisplayName("Thanh toán một phần — số dư giảm đúng phần thanh toán")
    void commitPayment_Partial_Amount() throws Exception {
        bidder.lockFunds(500_000);
        bidder.commitPayment(200_000);
        assertEquals(800_000, bidder.getBalance());
        assertEquals(300_000, bidder.getLockedBalance());
        assertEquals(500_000, bidder.getAvailableBalance());
    }

    // ======================== KIỂM TRA KHẢ NĂNG CHI TRẢ ========================

    @Test
    @DisplayName("canAfford — true khi số dư khả dụng đủ")
    void canAfford_Returns_True_When_Enough() {
        assertTrue(bidder.canAfford(1_000_000));
        assertTrue(bidder.canAfford(500_000));
        assertTrue(bidder.canAfford(1));
    }

    @Test
    @DisplayName("canAfford — false khi vượt quá số dư khả dụng")
    void canAfford_Returns_False_When_Not_Enough() {
        assertFalse(bidder.canAfford(1_000_001));
    }

    @Test
    @DisplayName("canAfford — tính theo availableBalance, không tính tiền bị tạm giữ")
    void canAfford_Considers_LockedBalance() throws Exception {
        bidder.lockFunds(800_000);
        assertTrue(bidder.canAfford(200_000));
        assertFalse(bidder.canAfford(200_001));
    }

    // ======================== ĐẤU GIÁ TỰ ĐỘNG ========================

    @Test
    @DisplayName("Bật đấu giá tự động thành công")
    void enableAutoBid_Sets_Limit_And_Flag() throws Exception {
        bidder.enableAutoBid(500_000);
        assertTrue(bidder.isAutoBidEnabled());
        assertEquals(500_000, bidder.getMaxBidLimit());
    }

    @Test
    @DisplayName("Tắt đấu giá tự động — reset maxBidLimit về 0")
    void disableAutoBid_Resets_State() throws Exception {
        bidder.enableAutoBid(500_000);
        bidder.disableAutoBid();
        assertFalse(bidder.isAutoBidEnabled());
        assertEquals(0, bidder.getMaxBidLimit());
    }

    // ======================== LUỒNG ĐẦY ĐỦ ========================

    @Test
    @DisplayName("Luồng đầy đủ: nạp → tạm giữ → hoàn trả → tạm giữ lại → thanh toán")
    void fullFlow_Deposit_Lock_Unlock_Lock_Commit() throws Exception {
        // Nạp thêm tiền
        bidder.deposit(500_000);
        assertEquals(1_500_000, bidder.getBalance());

        // Đặt giá phiên 1 (tạm giữ 200k)
        bidder.lockFunds(200_000);
        assertEquals(1_300_000, bidder.getAvailableBalance());

        // Bị outbid → hoàn trả
        bidder.unlockFunds(200_000);
        assertEquals(1_500_000, bidder.getAvailableBalance());

        // Đặt giá phiên 2 (tạm giữ 600k)
        bidder.lockFunds(600_000);
        assertEquals(900_000, bidder.getAvailableBalance());

        // Thắng → thanh toán
        bidder.commitPayment(600_000);
        assertEquals(900_000, bidder.getBalance());
        assertEquals(0, bidder.getLockedBalance());
    }
}
