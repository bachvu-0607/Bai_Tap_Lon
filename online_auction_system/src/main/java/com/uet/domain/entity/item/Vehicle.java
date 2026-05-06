package com.uet.domain.entity.item;

public class Vehicle extends Item {
    private String manufacturer; // Hãng sản xuất
    private int yearMade; // Năm sản xuất

    public Vehicle(String name, double startingPrice) {
        super(name, startingPrice);
        this.manufacturer = "Không rõ";
        this.yearMade = 0;
    }

    public Vehicle(String name, double startingPrice, String manufacturer, int yearMade) {
        super(name, startingPrice);
        this.manufacturer = manufacturer;
        this.yearMade = yearMade;
    }

    public Vehicle(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
        this.manufacturer = "Không rõ";
        this.yearMade = 0;
    }

    public Vehicle(String id, String name, double startingPrice, String manufacturer, int yearMade) {
        super(id, name, startingPrice);
        this.manufacturer = manufacturer;
        this.yearMade = yearMade;
    }

    @Override
    public String getCategory() {
        return "Phương tiện";
    }

    @Override
    public String getDescription() {
        if (super.getDescription() != null && !super.getDescription().isBlank()) {
            return super.getDescription();
        }
        return manufacturer + " (" + yearMade + ")";
    }

    @Override
    public String toString() {
        return "Phương tiện: " + getName() +
                " (" + manufacturer + " - " + yearMade +
                ") | Giá khởi điểm: " + getStartingPrice() + "$";
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public int getYearMade() {
        return yearMade;
    }
}
