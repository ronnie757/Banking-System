package bw.bank.model;

public class BankAdministrator {
    private final String adminId;
    private final String name;
    private final String username;
    private final String passwordHash;

    public BankAdministrator(String adminId, String name, String username, String passwordHash) {
        this.adminId = adminId;
        this.name = name;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getAdminId() { return adminId; }
    public String getName() { return name; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
}
