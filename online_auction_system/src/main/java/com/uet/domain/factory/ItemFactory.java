package com.uet.domain.factory;

import com.uet.domain.entity.item.Item;

/**
 * Factory Method để tạo ra các loại Item khác nhau.
 */

public abstract class ItemFactory {
    public abstract Item createItem(String name, double startingPrice);
    public abstract Item createItembyId(String id, String name, double startingPrice);
}