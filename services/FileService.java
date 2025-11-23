package services;

import model.Stock;
import model.User;
import util.CSVReader;
import util.CSVWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileService {
    private static final String USERS_FILE = "users.csv";

    public boolean registerUser(String username, String password, String name, String address, String phone, String email) {
        List<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) {
                System.out.println("Username already exists.");
                return false;
            }
        }
        String id = UUID.randomUUID().toString();
        User user = new User(id, username, password, name, address, phone, email);
        CSVWriter.appendLine(USERS_FILE, user.toString());
        ensurePortfolioFile(username);
        return true;
    }

    public User login(String username, String password) {
        List<User> users = loadUsers();
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public List<User> loadUsers() {
        List<String> lines = CSVReader.readAll(USERS_FILE);
        List<User> users = new ArrayList<>();
        for (String l : lines) {
            User u = User.fromCSV(l);
            if (u != null) users.add(u);
        }
        return users;
    }

    public String getPortfolioFile(String username) {
        return "portfolio_" + username + ".csv";
    }

    public void ensurePortfolioFile(String username) {
        File f = new File(getPortfolioFile(username));
        if (!f.exists()) {
            CSVWriter.writeAll(f.getPath(), new ArrayList<>());
        }
    }

    public List<Stock> loadPortfolio(String username) {
        String file = getPortfolioFile(username);
        List<String> lines = CSVReader.readAll(file);
        List<Stock> list = new ArrayList<>();
        for (String l : lines) {
            Stock s = Stock.fromCSV(l);
            if (s != null) list.add(s);
        }
        return list;
    }

    public void savePortfolio(String username, List<Stock> stocks) {
        String file = getPortfolioFile(username);
        List<String> lines = new ArrayList<>();
        for (Stock s : stocks) {
            lines.add(s.toString());
        }
        CSVWriter.writeAll(file, lines);
    }
}

