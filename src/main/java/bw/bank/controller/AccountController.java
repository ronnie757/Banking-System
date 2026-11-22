package bw.bank.controller;

import bw.bank.dao.AccountDAO;
import bw.bank.dao.CustomerDAO;
import bw.bank.dao.TransactionDAO;
import bw.bank.db.Database;
import bw.bank.exception.BankException;
import bw.bank.model.Account;
import bw.bank.model.Bank;
import bw.bank.model.ChequeAccount;
import bw.bank.model.InterestBearing;
import bw.bank.model.InvestmentAccount;
import bw.bank.model.SavingsAccount;
import bw.bank.model.Transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/** Mediates between the account screens and the core model + DAOs. */
public class AccountController {
    private final Bank bank = new Bank("B001", "BAC Bank", "Gaborone, Botswana", "Main Branch");
    private final AccountDAO accountDAO = new AccountDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public Account openAccount(String customerId, String type, String initialDepositText,
                               String employerName, String companyAddress) throws BankException {
        if (customerDAO.findById(customerId) == null) {
            throw new BankException("Customer not found.");
        }
        if (type == null || type.isBlank()) {
            throw new BankException("Choose an account type.");
        }
        BigDecimal initial = (initialDepositText == null || initialDepositText.isBlank())
                ? BigDecimal.ZERO : parseAmount(initialDepositText);

        String number = Database.newId("AC", "accounts", "account_number");
        LocalDate today = LocalDate.now();
        String branch = bank.getDefaultBranch();
        Account account;

        switch (type.trim().toUpperCase()) {
            case "SAVINGS":
                account = new SavingsAccount(number, customerId, branch, today, BigDecimal.ZERO);
                break;
            case "INVESTMENT":
                InvestmentAccount.validateInitialDeposit(initial);
                account = new InvestmentAccount(number, customerId, branch, today, BigDecimal.ZERO);
                break;
            case "CHEQUE":
                account = new ChequeAccount(number, customerId, branch, today, BigDecimal.ZERO,
                        employerName, companyAddress);
                break;
            default:
                throw new BankException("Unknown account type: " + type);
        }

        accountDAO.create(account);
        if (initial.signum() > 0) {
            account.deposit(initial, "Initial deposit");
            accountDAO.applyTransaction(account, account.getLastTransaction());
        }
        return account;
    }

    public Account deposit(String accountNumber, String amountText) throws BankException {
        Account account = load(accountNumber);
        account.deposit(parseAmount(amountText));
        accountDAO.applyTransaction(account, account.getLastTransaction());
        return account;
    }

    public Account withdraw(String accountNumber, String amountText) throws BankException {
        Account account = load(accountNumber);
        account.withdraw(parseAmount(amountText));   // polymorphism: rules differ per account type
        accountDAO.applyTransaction(account, account.getLastTransaction());
        return account;
    }

    public List<Account> getAccounts(String customerId) {
        return accountDAO.findByCustomer(customerId);
    }

    public Account getAccount(String accountNumber) throws BankException {
        return load(accountNumber);
    }

    public List<Transaction> getHistory(String accountNumber) {
        return transactionDAO.findByAccount(accountNumber);
    }

    /** Pays one month of interest to every active interest-bearing account. */
    public int payMonthlyInterest() throws BankException {
        int paid = 0;
        for (Account account : accountDAO.findAll()) {
            if (account.isActive() && account instanceof InterestBearing) {
                BigDecimal interest = ((InterestBearing) account).calculateInterest();
                if (interest.signum() > 0) {
                    account.creditInterest(interest);
                    accountDAO.applyTransaction(account, account.getLastTransaction());
                    paid++;
                }
            }
        }
        return paid;
    }

    public void setAccountActive(String accountNumber, boolean active) throws BankException {
        load(accountNumber);
        accountDAO.updateStatus(accountNumber, active);
    }

    private Account load(String accountNumber) throws BankException {
        Account a = accountDAO.findByNumber(accountNumber);
        if (a == null) {
            throw new BankException("Account not found.");
        }
        return a;
    }

    private static BigDecimal parseAmount(String text) throws BankException {
        if (text == null || text.isBlank()) {
            throw new BankException("Enter an amount.");
        }
        try {
            BigDecimal value = new BigDecimal(text.trim().replace(",", ""));
            if (value.signum() <= 0) {
                throw new BankException("Amount must be greater than zero.");
            }
            return value.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new BankException("Please enter a valid number for the amount.");
        }
    }
}
