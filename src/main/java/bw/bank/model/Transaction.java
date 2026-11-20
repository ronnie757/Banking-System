package bw.bank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {
    private final String transactionId;
    private final String accountNumber;
    private final LocalDateTime date;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String description;
    private final BigDecimal balanceAfter;

    public Transaction(String transactionId, String accountNumber, LocalDateTime date,
                       TransactionType type, BigDecimal amount, String description,
                       BigDecimal balanceAfter) {
        this.transactionId = transactionId;
        this.accountNumber = accountNumber;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.balanceAfter = balanceAfter;
    }

    public String getTransactionId() { return transactionId; }
    public String getAccountNumber() { return accountNumber; }
    public LocalDateTime getDate() { return date; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }

    public String getTransactionDetails() {
        return transactionId + " | " + date + " | " + type + " | BWP " + amount
                + " | balance BWP " + balanceAfter;
    }
}
