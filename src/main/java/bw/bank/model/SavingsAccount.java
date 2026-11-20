package bw.bank.model;

import bw.bank.exception.BankException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class SavingsAccount extends Account implements InterestBearing {
    public static final double MONTHLY_RATE = 0.0005; // 0.05%

    public SavingsAccount(String accountNumber, String customerId, String branch,
                          LocalDate openingDate, BigDecimal balance) {
        super(accountNumber, customerId, branch, openingDate, balance);
    }

    @Override
    public void withdraw(BigDecimal amount) throws BankException {
        throw new BankException("Withdrawals are not allowed from a Savings account.");
    }

    @Override
    public BigDecimal calculateInterest() {
        return getBalance().multiply(BigDecimal.valueOf(getInterestRate()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public double getInterestRate() { return MONTHLY_RATE; }

    @Override
    public String getAccountType() { return "Savings"; }
}
