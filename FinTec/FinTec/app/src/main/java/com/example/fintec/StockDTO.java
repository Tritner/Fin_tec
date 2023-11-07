package com.example.fintec;

import java.io.Serializable;

public class StockDTO implements Serializable  { // Data Transfer Object (DTO) for transferring data between activities
    private String symbol;
    private double price;
    private double changePercent;

    public StockDTO(Stock stock) {
        this.symbol = stock.getSymbol();
        this.price = stock.getPrice();
        this.changePercent = stock.getChangePercent();
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public double getChangePercent() {
        return changePercent;
    }

    // Convert the DTO back to the domain object
    public Stock toDomainObject() {
        return new Stock(symbol, price, changePercent);
    }
}
