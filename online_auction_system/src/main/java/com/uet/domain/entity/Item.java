package com.uet.domain.entity;

public class Item extends Entity {

    private String name;
    private double startingPrice;


    public Item() {}
    public Item(String id, String name, double startingPrice) {
        super(id);
        this.name = name;
        this.startingPrice = startingPrice;
    }
    

    public String getName(){return this.name;}
    public void setName(String Name){this.name = Name;}
    public double getStartingPrice(){return this.startingPrice;}
    

    @Override
    public String toString() {
        return "Sản phẩm: " + name + " | Giá khởi điểm: " + startingPrice + "$";
    }
}