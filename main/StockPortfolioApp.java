package main;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.Portfolio;
import model.Stock;
import model.User;
import services.*;

public class StockPortfolioApp {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        FileService fileService = new FileService();

        while (true) {
            System.out.println("=== Stock Portfolio Monitoring System ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1":
                    register(sc, fileService);
                    break;
                case "2":
                    User user = login(sc, fileService);
                    if (user != null) {
                        runPortfolioMenu(sc, user, fileService);
                    } else {
                        System.out.println("Invalid credentials.");
                    }
                    break;
                case "3":
                    System.out.println("Goodbye!");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // Login flow
    private static User login(Scanner sc, FileService fileService) {
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        return fileService.login(username, password);
    }

    // Registration flow with regex validation for username, password, email, and phone
    private static void register(Scanner sc, FileService fileService) {
        System.out.print("Choose a username: ");
        String username = sc.nextLine().trim();

        // Username validation: only letters, digits, underscores, 3–15 characters
        Pattern userPattern = Pattern.compile("^[A-Za-z0-9_]{3,15}$");
        Matcher userMatcher = userPattern.matcher(username);
        if (!userMatcher.matches()) {
            System.out.println("Invalid username. Use 3 to 15 letters, digits, or underscores.");
            return;
        }

        System.out.print("Choose a password (numbers only, 6 to 12 digits): ");
        String password = sc.nextLine().trim();

        // Password validation: digits only, length 6–12
        Pattern passPattern = Pattern.compile("^[0-9]{6,12}$");
        Matcher passMatcher = passPattern.matcher(password);
        if (!passMatcher.matches()) {
            System.out.println("Invalid password. Must be 6 to 12 digits (e.g., 123456).");
            return;
        }

        System.out.print("Full name: ");
        String name = sc.nextLine().trim();
        System.out.print("Address: ");
        String address = sc.nextLine().trim();

        System.out.print("Phone (10 digits): ");
        String phone = sc.nextLine().trim();
        Pattern phonePattern = Pattern.compile("^[0-9]{10}$");
        Matcher phoneMatcher = phonePattern.matcher(phone);
        if (!phoneMatcher.matches()) {
            System.out.println("Invalid phone number. Must be exactly 10 digits.");
            return;
        }

        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        Matcher emailMatcher = emailPattern.matcher(email);
        if (!emailMatcher.matches()) {
            System.out.println("Invalid email format. Example: user@example.com");
            return;
        }

        boolean ok = fileService.registerUser(username, password, name, address, phone, email);
        if (ok) System.out.println("Registration successful.");
    }

    // Portfolio menu
    private static void runPortfolioMenu(Scanner sc, User user, FileService fileService) {
        PortfolioService portfolioService = new PortfolioService(fileService);
        Portfolio portfolio = portfolioService.loadPortfolio(user.getUsername());

        PriceUpdater updater = new PriceUpdater(portfolio, fileService);
        AlertService alertService = new AlertService(portfolio);

        Thread updaterThread = new Thread(updater, "PriceUpdater");
        Thread alertThread = new Thread(alertService, "AlertService");
        updaterThread.start();
        alertThread.start();

        while (true) {
            System.out.println("\n=== Portfolio Menu (" + user.getUsername() + ") ===");
            System.out.println("1. Add Stock");
            System.out.println("2. View Stocks");
            System.out.println("3. Edit Stock");
            System.out.println("4. Delete Stock");
            System.out.println("5. Generate Report");
            System.out.println("6. Show Live Prices");
            System.out.println("7. Logout");
            System.out.print("Choose: ");
            String ch = sc.nextLine().trim();

            switch (ch) {
                case "1":
                    addStockFlow(sc, portfolioService, portfolio);
                    break;
                case "2":
                    portfolioService.viewStocks(portfolio);
                    break;
                case "3":
                    portfolioService.editStock(portfolio, sc);
                    break;
                case "4":
                    System.out.print("Enter stock name to delete: ");
                    String name = sc.nextLine().trim();
                    portfolioService.deleteStock(portfolio, name);
                    break;
                case "5":
                    String outFile = "report_" + user.getUsername() + ".csv";
                    portfolioService.reportSummary(portfolio, outFile);
                    break;
                case "6":
                    showLivePrices(sc, portfolio);
                    break;
                case "7":
                    System.out.println("Logging out...");
                    updater.stop();
                    alertService.stop();
                    updaterThread.interrupt();
                    alertThread.interrupt();
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // Add stock flow (auto-generates current price)
    private static void addStockFlow(Scanner sc, PortfolioService portfolioService, Portfolio portfolio) {
        try {
            System.out.print("Stock name: ");
            String name = sc.nextLine().trim();
            System.out.print("Quantity: ");
            int qty = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Buy price: ");
            double buy = Double.parseDouble(sc.nextLine().trim());
            System.out.print("Alert price: ");
            double alert = Double.parseDouble(sc.nextLine().trim());

            double current = buy + (buy * ((Math.random() * 2 - 1) / 100.0)); // ±1%
            Stock s = new Stock(name, qty, buy, current, alert);
            portfolioService.addStock(portfolio, s);
            System.out.println("Stock added with auto-generated current price: " + current);
        } catch (NumberFormatException e) {
            System.out.println("Invalid numeric input. Try again.");
        }
    }

    // Live price view
    private static void showLivePrices(Scanner sc, Portfolio portfolio) {
        System.out.println("Press Enter to stop live price view.");
        try {
            while (true) {
                System.out.println("\n--- Live Prices ---");
                for (Stock s : portfolio.getHoldings()) {
                    System.out.printf("%s -> %.2f%n", s.getStockName(), s.getCurrentPrice());
                }
                Thread.sleep(5000);

                if (System.in.available() > 0) {
                    sc.nextLine(); // consume Enter
                    System.out.println("Stopped live price view.");
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Live price view interrupted.");
        }
    }
}
