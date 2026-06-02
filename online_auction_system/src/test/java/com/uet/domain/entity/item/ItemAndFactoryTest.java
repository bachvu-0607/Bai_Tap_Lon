package com.uet.domain.entity.item;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.uet.domain.enums.ItemStatus;
import com.uet.domain.factory.*;

/**
 * Happy case tests cho các lớp Item (Art, Electronics, Vehicle) và Factory Pattern.
 */
class ItemAndFactoryTest {

    // ======================== ELECTRONICS ========================

    @Test
    @DisplayName("Tạo Electronics — category là 'Điện tử'")
    void electronics_Category_Is_DienTu() {
        Electronics e = new Electronics("Laptop Dell", 5000);
        assertEquals("Điện tử", e.getCategory());
        assertEquals("Laptop Dell", e.getName());
        assertEquals(5000, e.getStartingPrice());
    }

    @Test
    @DisplayName("Tạo Electronics với brand/model — mô tả tự động đúng")
    void electronics_WithBrandModel_Description() {
        Electronics e = new Electronics("MacBook Pro", 30_000_000, "Apple", "M3 Pro");
        assertEquals("Apple M3 Pro", e.getDescription());
        assertEquals("Apple", e.getBrand());
        assertEquals("M3 Pro", e.getModel());
    }

    @Test
    @DisplayName("Tạo Electronics với ID — ID từ ngoài đúng")
    void electronics_WithId() {
        Electronics e = new Electronics("ITEM-001", "iPhone 15", 25_000_000);
        assertEquals("ITEM-001", e.getId());
    }

    // ======================== ART ========================

    @Test
    @DisplayName("Tạo Art — category là 'Nghệ thuật'")
    void art_Category_Is_NgheThuat() {
        Art art = new Art("Mona Lisa", 1_000_000);
        assertEquals("Nghệ thuật", art.getCategory());
    }

    @Test
    @DisplayName("Tạo Art với đầy đủ thông tin — getter trả đúng")
    void art_FullInfo_GettersCorrect() {
        Art art = new Art("Starry Night", 500_000, "Van Gogh", 1889, "Oil");
        assertEquals("Van Gogh", art.getArtist());
        assertEquals(1889, art.getYearCreated());
        assertEquals("Oil", art.getMedium());
    }

    @Test
    @DisplayName("Art không có custom description — tự tạo description từ thuộc tính")
    void art_AutoDescription() {
        Art art = new Art("Bức tranh", 100, "Hoạ Sĩ X", 2024, "Acrylic");
        String desc = art.getDescription();
        assertTrue(desc.contains("Hoạ Sĩ X"));
        assertTrue(desc.contains("2024"));
        assertTrue(desc.contains("Acrylic"));
    }

    // ======================== VEHICLE ========================

    @Test
    @DisplayName("Tạo Vehicle — category là 'Phương tiện'")
    void vehicle_Category_Is_PhuongTien() {
        Vehicle v = new Vehicle("Honda SH", 80_000_000);
        assertEquals("Phương tiện", v.getCategory());
    }

    @Test
    @DisplayName("Tạo Vehicle với đầy đủ thông tin — getter trả đúng")
    void vehicle_FullInfo_GettersCorrect() {
        Vehicle v = new Vehicle("Toyota Camry", 800_000_000, "Toyota", 2024);
        assertEquals("Toyota", v.getManufacturer());
        assertEquals(2024, v.getYearMade());
    }

    // ======================== ITEM CHUNG ========================

    @Test
    @DisplayName("Item mới — trạng thái mặc định là AVAILABLE")
    void newItem_Status_Is_Available() {
        Electronics e = new Electronics("Test Item", 100);
        assertEquals(ItemStatus.AVAILABLE, e.getStatus());
    }

    @Test
    @DisplayName("Set trạng thái Item — thay đổi đúng")
    void setStatus_Changes_Item_Status() {
        Electronics e = new Electronics("Test Item", 100);
        e.setStatus(ItemStatus.IN_AUCTION);
        assertEquals(ItemStatus.IN_AUCTION, e.getStatus());
    }

    @Test
    @DisplayName("Item isValid — trả true khi có đủ thông tin")
    void item_IsValid_ReturnsTrue() {
        Electronics e = new Electronics("Valid Item", 100);
        assertTrue(e.isValid());
    }

    @Test
    @DisplayName("Set custom description — ghi đè mô tả mặc định")
    void item_CustomDescription_Overrides_Default() {
        Electronics e = new Electronics("Laptop", 1000, "Dell", "XPS");
        e.setDescription("Laptop cao cấp, mỏng nhẹ");
        assertEquals("Laptop cao cấp, mỏng nhẹ", e.getDescription());
    }

    @Test
    @DisplayName("Set imageLink — null chuyển thành chuỗi rỗng")
    void setImageLink_Null_Becomes_Empty() {
        Electronics e = new Electronics("Test", 100);
        e.setImageLink(null);
        assertEquals("", e.getImageLink());
    }

    @Test
    @DisplayName("Set imageLink — giá trị bình thường giữ nguyên")
    void setImageLink_Normal_Value() {
        Electronics e = new Electronics("Test", 100);
        e.setImageLink("https://example.com/photo.jpg");
        assertEquals("https://example.com/photo.jpg", e.getImageLink());
    }

    // ======================== FACTORY PATTERN ========================

    @Test
    @DisplayName("ElectronicsFactory — tạo đúng Electronics")
    void electronicsFactory_Creates_Electronics() {
        ItemFactory factory = new ElectronicsFactory();
        Item item = factory.createItem("Keyboard", 500);
        assertTrue(item instanceof Electronics);
        assertEquals("Điện tử", item.getCategory());
        assertEquals("Keyboard", item.getName());
        assertEquals(500, item.getStartingPrice());
    }

    @Test
    @DisplayName("ArtFactory — tạo đúng Art")
    void artFactory_Creates_Art() {
        ItemFactory factory = new ArtFactory();
        Item item = factory.createItem("Painting", 10000);
        assertTrue(item instanceof Art);
        assertEquals("Nghệ thuật", item.getCategory());
    }

    @Test
    @DisplayName("VehicleFactory — tạo đúng Vehicle")
    void vehicleFactory_Creates_Vehicle() {
        ItemFactory factory = new VehicleFactory();
        Item item = factory.createItem("Xe đạp", 3000);
        assertTrue(item instanceof Vehicle);
        assertEquals("Phương tiện", item.getCategory());
    }

    @Test
    @DisplayName("Factory createItembyId — tạo item với ID chỉ định")
    void factory_CreateItemById_Uses_Given_Id() {
        ItemFactory factory = new ElectronicsFactory();
        Item item = factory.createItembyId("CUSTOM-ID-123", "Mouse", 200);
        assertEquals("CUSTOM-ID-123", item.getId());
        assertEquals("Mouse", item.getName());
    }
}
