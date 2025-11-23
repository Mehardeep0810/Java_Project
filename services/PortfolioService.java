package services;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import model.Portfolio;
import model.Stock;

public class PortfolioService {
    private final FileService fileService;

    public PortfolioService(FileService fileService) {
        this.fileService = fileService;
    }

    // Load portfolio from CSV
    public Portfolio loadPortfolio(String username) {
        Portfolio p = new Portfolio(username);
        List<Stock> stocks = fileService.loadPortfolio(username);
        for (Stock s : stocks) p.addStock(s);
        return p;
    }

    // Add new stock
    public void addStock(Portfolio portfolio, Stock stock) {
        portfolio.addStock(stock);
        fileService.savePortfolio(portfolio.getUsername(), portfolio.getHoldings());
        System.out.println("Stock added.");
    }

    // Edit existing stock
    public void editStock(Portfolio portfolio, Scanner sc) {
        System.out.print("Enter stock name to edit: ");
        String name = sc.nextLine().trim();
        Stock s = portfolio.findStockByName(name);
        if (s == null) {
            System.out.println("Stock not found.");
            return;
        }
        System.out.print("New quantity (" + s.getQuantity() + "): ");
        s.setQuantity(parseIntOrDefault(sc.nextLine(), s.getQuantity()));

        System.out.print("New buy price (" + s.getBuyPrice() + "): ");
        s.setBuyPrice(parseDoubleOrDefault(sc.nextLine(), s.getBuyPrice()));

        System.out.print("New alert price (" + s.getAlertPrice() + "): ");
        s.setAlertPrice(parseDoubleOrDefault(sc.nextLine(), s.getAlertPrice()));

        fileService.savePortfolio(portfolio.getUsername(), portfolio.getHoldings());
        System.out.println("Stock updated.");
    }

    // Delete stock
    public void deleteStock(Portfolio portfolio, String stockName) {
        boolean removed = portfolio.removeStockByName(stockName);
        if (removed) {
            fileService.savePortfolio(portfolio.getUsername(), portfolio.getHoldings());
            System.out.println("Stock removed.");
        } else {
            System.out.println("Stock not found.");
        }
    }

    // View stocks in a bordered table with Profit and Loss columns
    public void viewStocks(Portfolio portfolio) {
        List<Stock> sorted = portfolio.getHoldings().stream()
                .sorted(Comparator.comparing(Stock::getStockName))
                .collect(Collectors.toList());

        if (sorted.isEmpty()) {
            System.out.println("No stocks in portfolio.");
            return;
        }

        String border = "+------------+-----+----------+--------------+------------+-----------+-----------+";
        System.out.println(border);
        System.out.printf("| %-10s | %-3s | %-8s | %-12s | %-10s | %-9s | %-9s |%n",
                "StockName", "Qty", "BuyPrice", "CurrentPrice", "AlertPrice", "Profit", "Loss");
        System.out.println(border);

        for (Stock s : sorted) {
            double pl = s.getProfitLoss();
            double profit = pl > 0 ? pl : 0;
            double loss = pl < 0 ? Math.abs(pl) : 0;

            System.out.printf("| %-10s | %-3d | %-8.2f | %-12.2f | %-10.2f | %-9.2f | %-9.2f |%n",
                    s.getStockName(), s.getQuantity(), s.getBuyPrice(),
                    s.getCurrentPrice(), s.getAlertPrice(), profit, loss);
        }

        System.out.println(border);
    }

    // Generate report with totals (separate Profit and Loss)
    public void reportSummary(Portfolio portfolio, String outputFile) {
        List<Stock> list = portfolio.getHoldings();
        double totalInvestment = list.stream().mapToDouble(Stock::getInvestment).sum();
        double totalProfit = list.stream().mapToDouble(s -> Math.max(s.getProfitLoss(), 0)).sum();
        double totalLoss = list.stream().mapToDouble(s -> Math.max(-s.getProfitLoss(), 0)).sum();

        StringBuilder sb = new StringBuilder();
        sb.append("Stock,Qty,BuyPrice,CurrentPrice,AlertPrice,Investment,Profit,Loss\n");
        list.forEach(s -> {
            double pl = s.getProfitLoss();
            double profit = pl > 0 ? pl : 0;
            double loss = pl < 0 ? Math.abs(pl) : 0;
            sb.append(String.format("%s,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                    s.getStockName(), s.getQuantity(), s.getBuyPrice(), s.getCurrentPrice(),
                    s.getAlertPrice(), s.getInvestment(), profit, loss));
        });

        // ✅ Fixed: TOTAL row without extra commas
        sb.append(String.format("TOTAL,%.2f,%.2f,%.2f%n", totalInvestment, totalProfit, totalLoss));

        util.CSVWriter.writeAll(outputFile, java.util.Arrays.asList(sb.toString().split("\n")));
        System.out.printf("Report saved to %s%n", outputFile);
        System.out.printf("Total Investment: %.2f | Total Profit: %.2f | Total Loss: %.2f%n",
                totalInvestment, totalProfit, totalLoss);
    }

    // Helpers for safe parsing
    private int parseIntOrDefault(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return def; }
    }
    private double parseDoubleOrDefault(String s, double def) {
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return def; }
    }
}
