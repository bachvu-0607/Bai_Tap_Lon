package com.uet.domain.entity.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.uet.domain.enums.ItemStatus;
import com.uet.domain.factory.ArtFactory;
import com.uet.domain.factory.ElectronicsFactory;
import com.uet.domain.factory.ItemFactory;
import com.uet.domain.factory.VehicleFactory;

/**
 * Unit tests for item inheritance and factory creation.
 * These tests keep the OOP requirements visible: shared Item state, category polymorphism,
 * custom descriptions, image links, and Factory Method output.
 */
class ItemAndFactoryTest {

    @Test
    void electronics_Uses_Category_And_Auto_Description_When_No_Custom_Description() {
        // Electronics should expose its own category and build a description from brand/model.
        Electronics item = new Electronics("MacBook Pro", 30_000_000, "Apple", "M3 Pro");

        assertEquals("Điện tử", item.getCategory());
        assertEquals("Apple M3 Pro", item.getDescription());
        assertEquals("Apple", item.getBrand());
        assertEquals("M3 Pro", item.getModel());
    }

    @Test
    void art_Uses_Category_And_Auto_Description_From_Art_Metadata() {
        // Art description should include artist, year, and medium when no custom description exists.
        Art item = new Art("Starry Night", 500_000, "Van Gogh", 1889, "Oil");

        assertEquals("Nghệ thuật", item.getCategory());
        assertTrue(item.getDescription().contains("Van Gogh"));
        assertTrue(item.getDescription().contains("1889"));
        assertTrue(item.getDescription().contains("Oil"));
    }

    @Test
    void vehicle_Uses_Category_And_Auto_Description_From_Vehicle_Metadata() {
        // Vehicle description should reflect manufacturer and manufacture year.
        Vehicle item = new Vehicle("Camry", 800_000_000, "Toyota", 2024);

        assertEquals("Phương tiện", item.getCategory());
        assertEquals("Toyota (2024)", item.getDescription());
        assertEquals("Toyota", item.getManufacturer());
        assertEquals(2024, item.getYearMade());
    }

    @Test
    void item_Shared_State_Can_Be_Updated_Through_Base_Class_Methods() {
        // Base Item fields should work the same no matter what concrete subclass is used.
        Electronics item = new Electronics("Laptop", 1_000, "Dell", "XPS");

        item.setName("Laptop XPS");
        item.setDescription("Thin business laptop");
        item.setImageLink(null);
        item.setStatus(ItemStatus.IN_AUCTION);

        assertEquals("Laptop XPS", item.getName());
        assertEquals("Thin business laptop", item.getDescription());
        assertEquals("", item.getImageLink());
        assertEquals(ItemStatus.IN_AUCTION, item.getStatus());
        assertTrue(item.isValid());
    }

    @Test
    void factory_Creates_Correct_Item_Subclasses() {
        // Each concrete factory must return the matching item subclass.
        ItemFactory electronicsFactory = new ElectronicsFactory();
        ItemFactory artFactory = new ArtFactory();
        ItemFactory vehicleFactory = new VehicleFactory();

        assertInstanceOf(Electronics.class, electronicsFactory.createItem("Keyboard", 500));
        assertInstanceOf(Art.class, artFactory.createItem("Painting", 10_000));
        assertInstanceOf(Vehicle.class, vehicleFactory.createItem("Bike", 3_000));
    }

    @Test
    void factory_CreateItemById_Keeps_Restored_Id() {
        // Loading from database needs constructors/factories that keep the stored id.
        ItemFactory factory = new ElectronicsFactory();
        Item item = factory.createItembyId("ITEM-123", "Mouse", 200);

        assertEquals("ITEM-123", item.getId());
        assertEquals("Mouse", item.getName());
        assertEquals(200, item.getStartingPrice());
    }
}
