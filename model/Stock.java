package model;

import java.util.Objects;

public class Stock {
    private String stockName;
    private int quantity;
    private double buyPrice;
    private double currentPrice;
    private double alertPrice;

    public Stock() {}

    public Stock(String stockName, int quantity, double buyPrice, double currentPrice, double alertPrice) {
        this.stockName = stockName;
        this.quantity = quantity;
        this.buyPrice = buyPrice;
        this.currentPrice = currentPrice;
        this.alertPrice = alertPrice;
    }

    public String getStockName() { return stockName; }
    public void setStockName(String stockName) { this.stockName = stockName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getBuyPrice() { return buyPrice; }
    public void setBuyPrice(double buyPrice) { this.buyPrice = buyPrice; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double currentPrice) { this.currentPrice = currentPrice; }
    public double getAlertPrice() { return alertPrice; }
    public void setAlertPrice(double alertPrice) { this.alertPrice = alertPrice; }

    public double getInvestment() { return buyPrice * quantity; }
    public double getProfitLoss() { return (currentPrice - buyPrice) * quantity; }

    @Override
    public String toString() {
        return stockName + "," + quantity + "," + buyPrice + "," + currentPrice + "," + alertPrice;
    }

    public static Stock fromCSV(String line) {
        String[] parts = line.split(",");
        if (parts.length < 5) return null;
        return new Stock(
            parts[0].trim(),
            Integer.parseInt(parts[1].trim()),
            Double.parseDouble(parts[2].trim()),
            Double.parseDouble(parts[3].trim()),
            Double.parseDouble(parts[4].trim())
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Stock)) return false;
        Stock stock = (Stock) o;
        return Objects.equals(stockName, stock.stockName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stockName);
    }
}
