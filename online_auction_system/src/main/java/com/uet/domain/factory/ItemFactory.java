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
                return new Electronics(id, name, startingPrice);
            case ART:
                return new Art(id, name, startingPrice);
            case VEHICLE:
                return new Vehicle(id, name, startingPrice);
            default:
                throw new IllegalArgumentException("Loại sản phẩm không hợp lệ!");
        }
    }
}