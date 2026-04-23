package com.uet.models;

import java.io.Serializable;

public class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { ACTIVE, CLOSED, PENDING }

    private int id;
    private String name;
    private double currentPrice;
    private double startingPrice;
    private String description;
    private String timeLeft;
    private Status status;
    private String highestBidder;

    public Item() {}

    public Item(int id, String name, double startingPrice,
                String description, String timeLeft, Status status) {
        this.id = id;
        this.name = name;
        this.startingPrice = startingPrice;
        this.currentPrice = startingPrice;
        this.description = description;
        this.timeLeft = timeLeft;
        this.status = status;
        this.highestBidder = null;
    }

    // Getters
    public int getId()             { return id; }
    public String getName()        { return name; }
    public double getCurrentPrice(){ return currentPrice; }
    public double getStartingPrice(){ return startingPrice; }
    public String getDescription() { return description; }
    public String getTimeLeft()    { return timeLeft; }
    public Status getStatus()      { return status; }
    public String getHighestBidder(){ return highestBidder; }

    // Setters
    public void setId(int id)                      { this.id = id; }
    public void setName(String name)               { this.name = name; }
    public void setCurrentPrice(double currentPrice){ this.currentPrice = currentPrice; }
    public void setStartingPrice(double startingPrice){ this.startingPrice = startingPrice; }
    public void setDescription(String description) { this.description = description; }
    public void setTimeLeft(String timeLeft)       { this.timeLeft = timeLeft; }
    public void setStatus(Status status)           { this.status = status; }
    public void setHighestBidder(String highestBidder){ this.highestBidder = highestBidder; }

    public void placeBid(double newPrice, String bidderName) {
        if (status != Status.ACTIVE) return;
        if (newPrice <= currentPrice) return;
        this.currentPrice = newPrice;
        this.highestBidder = bidderName;
    }

    @Override
    public String toString() {
        return "Item{id=" + id + ", name='" + name + "', startingPrice=" + startingPrice +
               ", currentPrice=" + currentPrice + ", status=" + status +
               ", highestBidder='" + highestBidder + "', timeLeft='" + timeLeft + "'}";
    }
}
