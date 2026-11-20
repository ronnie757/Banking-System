package bw.bank.model;

import bw.bank.exception.BankException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ChequeAccount extends Account {
    public static final BigDecimal OVERDRAFT_LIMIT = new BigDecimal("1000.00");

    private final String employerName;
    private final String companyAddress;

    public ChequeAccount(String accountNumber, String customerId, String branch,
                         LocalDate openingDate, BigDecimal balance,
                         String employerName, String companyAddress) throws BankException {
        super(accountNumber, customerId, branch, openingDate, balance);
        if (employerName == null || employerName.isBlank()
                || companyAddress == null || companyAddress.isBlank()) {
            throw new BankException("A Cheque account needs the employer name and company address.");
        }
        this.employerName = employerName.trim();
        this.companyAddress = companyAddress.trim();
    }

    public String getEmployerName() { return employerName; }
    public String getCompanyAddress() { return companyAddress; }

    @Override
    public void withdraw(BigDecimal amount) throws BankException {
        requireActive();
        requirePositive(amount);
        if (amount.compareTo(getBalance().add(OVERDRAFT_LIMIT)) > 0) {
            throw new BankException("Withdrawal exceeds your balance plus the BWP 1000.00 overdraft limit.");
        }
        debit(amount, "Withdrawal");
    }

    @Override
    public String getAccountType() { return "Cheque"; }
}
