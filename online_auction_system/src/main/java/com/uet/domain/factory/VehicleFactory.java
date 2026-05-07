package com.uet.domain.factory;

import com.uet.domain.entity.item.Item;
import com.uet.domain.entity.item.Vehicle;

public class VehicleFactory extends ItemFactory{
    @Override
    public Item createItem(String name, double startingPrice){
        return new Vehicle(name, startingPrice);
    }
    @Override
    public Item createItembyId(String id, String name, double startingPrice){
        return new Vehicle(id, name, startingPrice);
    }

}