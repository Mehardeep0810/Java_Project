package model;

import java.util.ArrayList;
import java.util.List;

public class Portfolio {
    private String username;
    private List<Stock> holdings;

    public Portfolio(String username) {
        this.username = username;
        this.holdings = new ArrayList<>();
    }

    public String getUsername() { return username; }
    public List<Stock> getHoldings() { return holdings; }

    public void addStock(Stock stock) { holdings.add(stock); }

    public boolean removeStockByName(String stockName) {
        return holdings.removeIf(s -> s.getStockName().equalsIgnoreCase(stockName));
    }

    public Stock findStockByName(String stockName) {
        for (Stock s : holdings) {
            if (s.getStockName().equalsIgnoreCase(stockName)) return s;
        }
        return null;
    }
}
