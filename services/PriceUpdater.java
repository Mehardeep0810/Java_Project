package services;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import model.Portfolio;
import model.Stock;

public class PriceUpdater implements Runnable {
    private final Portfolio portfolio;
    private final FileService fileService;
    private volatile boolean running = true;
    private final Random random = new Random();

    public PriceUpdater(Portfolio portfolio, FileService fileService) {
        this.portfolio = portfolio;
        this.fileService = fileService;
    }

    // Stop the updater gracefully
    public void stop() { running = false; }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(5000); // update every 5 seconds
                synchronized (portfolio) {
                    for (Stock s : portfolio.getHoldings()) {
                        double cp = s.getCurrentPrice();
                        // Random delta between -1% and +1%
                        double deltaPercent = (random.nextDouble() * 2 - 1) / 100.0;
                        double newPrice = cp + (cp * deltaPercent);
                        s.setCurrentPrice(Math.max(0.01, round2(newPrice)));
                    }
                    // Save updated portfolio to CSV
                    fileService.savePortfolio(portfolio.getUsername(), portfolio.getHoldings());
                }
                // Log update with timestamp + prices
                logUpdate();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }

    // Round to 2 decimal places
    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // Log each stock’s current price with timestamp
    private void logUpdate() {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (FileWriter fw = new FileWriter("updater.log", true)) {
            fw.write("Update at " + timestamp + System.lineSeparator());
            for (Stock s : portfolio.getHoldings()) {
                fw.write(s.getStockName() + " -> Current Price: " + s.getCurrentPrice() + System.lineSeparator());
            }
            fw.write(System.lineSeparator());
        } catch (IOException e) {
            // Fail silently to avoid disturbing user
        }
    }
}
