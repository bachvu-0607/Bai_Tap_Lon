package com.uet.domain.factory;

import com.uet.domain.entity.*;
import com.uet.domain.enums.ItemType;

/**
 * Factory Method để tạo ra các loại Item khác nhau.
 */
public class ItemFactory {
    
    public static Item createItem(ItemType type, String id, String name, double startingPrice) {
        switch (type) {
            case ELECTRONICS:
                // Giả sử Electronics có thêm tham số bảo hành
                return new Electronics(); 
            case ART:
                return new Art();
            case VEHICLE:
                return new Vehicle();
            default:
                throw new IllegalArgumentException("Loại sản phẩm không hợp lệ!");
        }
    }
}