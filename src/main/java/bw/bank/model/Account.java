package bw.bank.model;

import bw.bank.exception.BankException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public abstract class Account {
    private final String accountNumber;
    private final String customerId;
    private final String branch;
    private final LocalDate openingDate;
    private BigDecimal balance;
    private boolean active = true;
    private final List<Transaction> transactions = new ArrayList<>();

    protected Account(String accountNumber, String customerId, String branch,
                      LocalDate openingDate, BigDecimal balance) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.branch = branch;
        this.openingDate = openingDate;
        this.balance = balance.setScale(2, RoundingMode.HALF_UP);
    }

    public String getAccountNumber() { return accountNumber; }
    public String getCustomerId() { return customerId; }
    public String getBranch() { return branch; }
    public LocalDate getOpeningDate() { return openingDate; }
    public BigDecimal getBalance() { return balance; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public Transaction getLastTransaction() {
        return transactions.isEmpty() ? null : transactions.get(transactions.size() - 1);
    }

    // Overloading: two versions of deposit
    public void deposit(BigDecimal amount) throws BankException {
        deposit(amount, "Deposit");
    }

    public void deposit(BigDecimal amount, String description) throws BankException {
        requireActive();
        requirePositive(amount);
        credit(amount, TransactionType.DEPOSIT, description);
    }

    public void creditInterest(BigDecimal interest) throws BankException {
        requireActive();
        requirePositive(interest);
        credit(interest, TransactionType.INTEREST, "Monthly interest");
    }

    // Each subclass overrides this with its own rules (polymorphism)
    public abstract void withdraw(BigDecimal amount) throws BankException;

    public abstract String getAccountType();

    public String getAccountDetails() {
        return getAccountType() + " account " + accountNumber + " | branch " + branch
                + " | opened " + openingDate + " | balance BWP " + balance
                + " | " + (active ? "ACTIVE" : "CLOSED");
    }

    @Override
    public String toString() {
        return getAccountDetails();
    }

    protected void requireActive() throws BankException {
        if (!active) {
            throw new BankException("This account is closed.");
        }
    }

    protected void requirePositive(BigDecimal amount) throws BankException {
        if (amount == null || amount.setScale(2, RoundingMode.HALF_UP).signum() <= 0) {
            throw new BankException("Amount must be greater than zero.");
        }
    }

    protected void debit(BigDecimal amount, String description) {
        BigDecimal value = amount.setScale(2, RoundingMode.HALF_UP);
        balance = balance.subtract(value);
        record(TransactionType.WITHDRAWAL, value, description);
    }

    private void credit(BigDecimal amount, TransactionType type, String description) {
        BigDecimal value = amount.setScale(2, RoundingMode.HALF_UP);
        balance = balance.add(value);
        record(type, value, description);
    }

    private void record(TransactionType type, BigDecimal amount, String description) {
        String id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        transactions.add(new Transaction(id, accountNumber, LocalDateTime.now(),
                type, amount, description, balance));
    }
}
