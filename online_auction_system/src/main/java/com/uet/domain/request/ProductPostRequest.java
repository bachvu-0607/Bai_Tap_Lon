package com.uet.domain.request;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ProductPostRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String productType;
    private final String productName;
    private final String description;
    private final double openingPrice;
    private final double minIncrement;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String imageLink;

    public ProductPostRequest(String productType, String productName, String description, double openingPrice, double minIncrement,
                              LocalDateTime startTime, LocalDateTime endTime, String imageLink) {
        this.productType = productType;
        this.productName = productName;
        this.description = description;
        this.openingPrice = openingPrice;
        this.minIncrement = minIncrement;
        this.startTime = startTime;
        this.endTime = endTime;
        this.imageLink = imageLink;
    }

    public String getProductType() {
        return productType;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public double getOpeningPrice() {
        return openingPrice;
    }

    public double getMinIncrement() {
        return minIncrement;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getImageLink() {
        return imageLink;
    }
}
