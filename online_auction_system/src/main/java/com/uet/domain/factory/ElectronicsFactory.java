package com.uet.domain.factory;

import com.uet.domain.entity.item.Electronics;
import com.uet.domain.entity.item.Item;

public class ElectronicsFactory extends ItemFactory{
    @Override
    public Item createItem(String name, double startingPrice){
        return new Electronics(name, startingPrice);
    }
    @Override
    public Item createItembyId(String id, String name, double startingPrice){
        return new Electronics(id, name, startingPrice);
    }
}