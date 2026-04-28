package com.uet.domain.entity;

public class Electronics extends Item {
    private String brand; // Thương hiệu
    private String model; // Phiên bản

    public Electronics(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
        this.brand = "Không rõ";
        this.model = "Không rõ";
    }

    public Electronics(String id, String name, double startingPrice, String brand, String model) {
        super(id, name, startingPrice);
        this.brand = brand;
        this.model = model;
    }

    @Override
    public String getCategory() {
        return "Điện tử";
    }

    @Override
    public String getDescription() {
        return brand + " " + model;
    }

    @Override
    public String toString() {
        return "Điện tử: " + getName() +
                " (" + brand + " - " + model +
                ") | Giá khởi điểm: " + getStartingPrice() + "$";
    }

    public String getBrand() { return brand; }
    public String getModel() { return model; }
}
