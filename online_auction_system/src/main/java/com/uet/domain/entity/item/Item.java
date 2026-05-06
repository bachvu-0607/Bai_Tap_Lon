package com.uet.domain.entity.item;

import com.uet.domain.entity.Entity;
import com.uet.domain.enums.ItemStatus;

public abstract class Item extends Entity {

    private String name;
    private String description;
    private double startingPrice;
    private ItemStatus status;


    public Item() {
        super();
        this.status = ItemStatus.AVAILABLE;
    }

    public Item(String name, double startingPrice) {
        super();
        this.name = name;
        this.description = "";
        this.startingPrice = startingPrice;
        this.status = ItemStatus.AVAILABLE;
    }

    public Item(String id, String name, double startingPrice) {
        this(id, name, "", startingPrice);
    }

    public Item(String id, String name, String description, double startingPrice) {
        super(id);
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.status = ItemStatus.AVAILABLE;
    }

    public abstract String getCategory();

    public String getDescription() { return this.description; }

    public boolean isValid() {
        return getId() != null
                && !getId().isBlank()
                && name != null
                && !name.isBlank()
                && startingPrice > 0
                && status != null;
    }

    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public double getStartingPrice() { return this.startingPrice; }
    public ItemStatus getStatus() { return this.status; }
    public void setStatus(ItemStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "Sản phẩm: " + name + " | Giá khởi điểm: " + startingPrice + "$";
    }
}
