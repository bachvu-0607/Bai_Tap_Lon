package com.uet.domain.entity;

public class Art extends Item {
    private String artist;
    private int yearCreated;
    private String medium; // Sơn dầu, Acrylic, Màu nước...

    public Art(String id, String name, double startingPrice) {
        super(id, name, startingPrice);
        this.artist = "Không rõ";
        this.yearCreated = 0;
        this.medium = "Không rõ";
    }

    public Art(String id, String name, double startingPrice, String artist, int yearCreated, String medium) {
        super(id, name, startingPrice);
        this.artist = artist;
        this.yearCreated = yearCreated;
        this.medium = medium;
    }

    @Override
    public String getCategory() {
        return "Nghệ thuật";
    }

    @Override
    public String getDescription() {
        return "Tác phẩm '" + getName() + "' của " + artist + " (" + yearCreated + ") - " + medium;
    }

    @Override
    public String toString() {
        return getDescription() + " | Giá khởi điểm: " + getStartingPrice() + "$";
    }

    // Getter
    public String getArtist() { return artist; }
    public int getYearCreated() { return yearCreated; }
    public String getMedium() { return medium; }
}
