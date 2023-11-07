package com.example.fintec;

public class StockFactory {  // Factory class for creating Stock objects
    public Stock createStock(String symbol, double price, double changePercent) {
        return new Stock(symbol, price, changePercent);
    }
}

