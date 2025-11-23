package model;

public class User {
    private String id;
    private String username;
    private String password;
    private String name;
    private String address;
    private String phone;
    private String email;

    public User() {}

    public User(String id, String username, String password, String name, String address, String phone, String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    @Override
    public String toString() {
        return id + "," + username + "," + password + "," + name + "," + address + "," + phone + "," + email;
    }

    public static User fromCSV(String line) {
        String[] p = line.split(",");
        if (p.length < 7) return null;
        return new User(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), p[4].trim(), p[5].trim(), p[6].trim());
    }
}
