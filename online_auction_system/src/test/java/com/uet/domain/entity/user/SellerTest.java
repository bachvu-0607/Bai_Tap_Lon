package com.uet.domain.entity.user;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Happy case tests cho lớp Seller.
 * Kiểm tra khởi tạo, nạp tiền, và truy vấn số dư.
 */
class SellerTest {

    @Test
    @DisplayName("Khởi tạo Seller mới — số dư bằng 0")
    void newSeller_Has_Zero_Balance() {
        Seller seller = new Seller("CCCD001", "Nguyen Seller", "0901111111", "pw", "HN");
        assertEquals(0, seller.getBalance());
        assertEquals(0, seller.getAvailableBalance());
    }

    @Test
    @DisplayName("Khởi tạo Seller với ID — thông tin cá nhân đúng")
    void newSeller_WithId_Properties_Set_Correctly() {
        Seller seller = new Seller("S-001", "CCCD002", "Tran Seller", "0902222222", "pw", "HCM");
        assertEquals("S-001", seller.getId());
        assertEquals("Tran Seller", seller.getName());
        assertEquals("CCCD002", seller.getCitizenId());
        assertTrue(seller.isActive());
    }

    @Test
    @DisplayName("Nạp tiền thành công — số dư tăng đúng")
    void deposit_Increases_Balance() throws Exception {
        Seller seller = new Seller("CCCD001", "Nguyen Seller", "0901111111", "pw", "HN");
        seller.deposit(500_000);
        assertEquals(500_000, seller.getBalance());
        assertEquals(500_000, seller.getAvailableBalance());
    }

    @Test
    @DisplayName("Nạp tiền nhiều lần — cộng dồn chính xác")
    void deposit_Multiple_Times_Accumulates() throws Exception {
        Seller seller = new Seller("CCCD001", "Nguyen Seller", "0901111111", "pw", "HN");
        seller.deposit(100_000);
        seller.deposit(200_000);
        seller.deposit(300_000);
        assertEquals(600_000, seller.getBalance());
    }

    @Test
    @DisplayName("Seller availableBalance luôn bằng balance (không có tiền bị tạm giữ)")
    void availableBalance_Always_Equals_Balance() throws Exception {
        Seller seller = new Seller("CCCD001", "Nguyen Seller", "0901111111", "pw", "HN");
        seller.deposit(1_000_000);
        assertEquals(seller.getBalance(), seller.getAvailableBalance());
    }
}
