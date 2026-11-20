package bw.bank.model;

public class Bank {
    private final String bankId;
    private final String name;
    private final String address;
    private final String defaultBranch;

    public Bank(String bankId, String name, String address, String defaultBranch) {
        this.bankId = bankId;
        this.name = name;
        this.address = address;
        this.defaultBranch = defaultBranch;
    }

    public String getBankId() { return bankId; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDefaultBranch() { return defaultBranch; }
}
