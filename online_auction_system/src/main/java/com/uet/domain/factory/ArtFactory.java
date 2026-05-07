package com.uet.domain.factory;

import com.uet.domain.entity.item.Art;
import com.uet.domain.entity.item.Item;

public class ArtFactory extends ItemFactory{
    @Override
    public Item createItem(String name, double startingPrice){
        return new Art(name, startingPrice);
    }

    @Override
    public Item createItembyId(String id, String name, double startingPrice){
        return new Art(id, name, startingPrice);
    }

}