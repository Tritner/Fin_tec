package com.example.fintec;

public class Stock {
    private String symbol;
    private double price;
    private double changePercent;

    public Stock(String symbol, double price, double changePercent) {
        this.symbol = symbol;
        this.price = price;
        this.changePercent = changePercent;
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
}

