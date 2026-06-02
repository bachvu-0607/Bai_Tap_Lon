package com.uet.domain.entity.user;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/**
 * Happy case tests cho lớp Admin.
 * Kiểm tra quyền khóa tài khoản người dùng.
 */
class AdminTest {

    @Test
    @DisplayName("Khởi tạo Admin — thông tin cá nhân đúng")
    void newAdmin_Properties_Set_Correctly() {
        Admin admin = new Admin("A-001", "CCCD_ADMIN", "Admin Name", "0909999999", "pw", "HN");
        assertEquals("A-001", admin.getId());
        assertEquals("Admin Name", admin.getName());
        assertEquals("CCCD_ADMIN", admin.getCitizenId());
        assertTrue(admin.isActive());
    }

    @Test
    @DisplayName("Admin khóa Bidder thành công — tài khoản bị deactivate")
    void banUser_Bidder_Successfully() throws Exception {
        Admin admin = new Admin("CCCD_ADMIN", "Admin", "0909999999", "pw", "HN");
        Bidder bidder = new Bidder("CCCD_BIDDER", "Bidder", "0901111111", "pw", "HCM");

        assertTrue(bidder.isActive());
        admin.banUser(bidder);
        assertFalse(bidder.isActive());
    }

    @Test
    @DisplayName("Admin khóa Seller thành công — tài khoản bị deactivate")
    void banUser_Seller_Successfully() throws Exception {
        Admin admin = new Admin("CCCD_ADMIN", "Admin", "0909999999", "pw", "HN");
        Seller seller = new Seller("CCCD_SELLER", "Seller", "0902222222", "pw", "DN");

        assertTrue(seller.isActive());
        admin.banUser(seller);
        assertFalse(seller.isActive());
    }
}
