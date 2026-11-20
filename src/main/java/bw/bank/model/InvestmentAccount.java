package bw.bank.model;

import bw.bank.exception.BankException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

public class InvestmentAccount extends Account implements InterestBearing {
    public static final double MONTHLY_RATE = 0.05; // 5%
    public static final BigDecimal MINIMUM_DEPOSIT = new BigDecimal("500.00");

    public InvestmentAccount(String accountNumber, String customerId, String branch,
                             LocalDate openingDate, BigDecimal balance) {
        super(accountNumber, customerId, branch, openingDate, balance);
    }

    public static void validateInitialDeposit(BigDecimal deposit) throws BankException {
        if (deposit == null || deposit.compareTo(MINIMUM_DEPOSIT) < 0) {
            throw new BankException("An Investment account needs an initial deposit of at least BWP 500.00.");
        }
    }

    @Override
    public void withdraw(BigDecimal amount) throws BankException {
        requireActive();
        requirePositive(amount);
        if (amount.compareTo(getBalance()) > 0) {
            throw new BankException("Insufficient funds. Available balance: BWP " + getBalance());
        }
        debit(amount, "Withdrawal");
    }

    @Override
    public BigDecimal calculateInterest() {
        return getBalance().multiply(BigDecimal.valueOf(getInterestRate()))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public double getInterestRate() { return MONTHLY_RATE; }

    @Override
    public String getAccountType() { return "Investment"; }
}
