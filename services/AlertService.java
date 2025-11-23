package services;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import model.Portfolio;
import model.Stock;

public class AlertService implements Runnable {
    private final Portfolio portfolio;
    private volatile boolean running = true;

    // Keep a simple list of triggered alerts to avoid spamming
    private final List<String> triggered = new CopyOnWriteArrayList<>();

    public AlertService(Portfolio portfolio) {
        this.portfolio = portfolio;
    }

    public void stop() { running = false; }

    @Override
    public void run() {
        while (running) {
            try {
                // Check alerts every 2 seconds
                Thread.sleep(2000);
                for (Stock s : portfolio.getHoldings()) {
                    double cp = s.getCurrentPrice();
                    double ap = s.getAlertPrice();
                    double bp = s.getBuyPrice();
                    String key = s.getStockName() + ":" + ap;

                    // Trigger only when threshold is crossed in the correct direction
                    if (bp < ap && cp >= ap && !triggered.contains(key)) {
                        System.out.printf("[ALERT] %s crossed above %.2f (current: %.2f)%n",
                                s.getStockName(), ap, cp);
                        triggered.add(key);
                    } else if (bp > ap && cp <= ap && !triggered.contains(key)) {
                        System.out.printf("[ALERT] %s dropped below %.2f (current: %.2f)%n",
                                s.getStockName(), ap, cp);
                        triggered.add(key);
                    }
                }
            } catch (InterruptedException e) {
                // Graceful stop
                Thread.currentThread().interrupt();
                running = false;
            }
        }
    }
}
