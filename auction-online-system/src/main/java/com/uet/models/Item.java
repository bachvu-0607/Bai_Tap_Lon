package com.uet.models;

public class Item{
    static private int sid = 0;
    private String name;
    private int ID;
    private double price;
    private double current_price;

    public Item() {}
    public Item(String name, double price) {
        this.name = name;
        this.ID = ++this.sid;
        this.price = price;
    }
    public Item(Item x) {
        this(x.name, x.price);
        this.ID = ++this.sid;
    }
    

    public String getName(){return this.name;}
    public int getID(){return this.ID;}
    public double getPrice(){return this.price;}
    public double getFinal_Price(){return this.current_price;}

    public void setName(String Name){this.name = Name;}
    public void setFinal_Price(double Final_Price){this.current_price = Final_Price;}

    @Override
    public String toString() {
        return "Sản phẩm: " + name + " | Giá hiện tại: " + current_price + "$";
    }
}