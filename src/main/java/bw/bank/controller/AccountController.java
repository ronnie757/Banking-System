package bw.bank.controller;

import bw.bank.exception.BankException;
import bw.bank.model.Account;
import bw.bank.model.ChequeAccount;
import bw.bank.model.InvestmentAccount;
import bw.bank.model.SavingsAccount;
import bw.bank.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** TEMPORARY in-memory version so the screens can run. Replaced by the real one in Step 6. */
public class AccountController {
    static final List<Account> STORE = new ArrayList<>();

    static {
        try {
            Account s = new SavingsAccount("AC100001", "CU100001", "Main Branch", LocalDate.now(), BigDecimal.ZERO);
            s.deposit(new BigDecimal("1500"), "Initial deposit");
            Account i = new InvestmentAccount("AC100002", "CU100001", "Main Branch", LocalDate.now(), BigDecimal.ZERO);
            i.deposit(new BigDecimal("800"), "Initial deposit");
            STORE.add(s);
            STORE.add(i);
        } catch (BankException e) {
            throw new IllegalStateException(e);
        }
    }

    public Account openAccount(String customerId, String type, String initialText,
                               String employer, String companyAddress) throws BankException {
        if (type == null) {
            throw new BankException("Choose an account type.");
        }
        BigDecimal initial = (initialText == null || initialText.isBlank())
                ? BigDecimal.ZERO : parse(initialText);
        String number = "AC" + (100001 + STORE.size());
        Account a;
        switch (type.toUpperCase()) {
            case "SAVINGS":
                a = new SavingsAccount(number, customerId, "Main Branch", LocalDate.now(), BigDecimal.ZERO);
                break;
            case "INVESTMENT":
                InvestmentAccount.validateInitialDeposit(initial);
                a = new InvestmentAccount(number, customerId, "Main Branch", LocalDate.now(), BigDecimal.ZERO);
                break;
            default:
                a = new ChequeAccount(number, customerId, "Main Branch", LocalDate.now(),
                        BigDecimal.ZERO, employer, companyAddress);
        }
        if (initial.signum() > 0) {
            a.deposit(initial, "Initial deposit");
        }
        STORE.add(a);
        return a;
    }

    public Account deposit(String accountNumber, String amountText) throws BankException {
        Account a = find(accountNumber);
        a.deposit(parse(amountText));
        return a;
    }

    public Account withdraw(String accountNumber, String amountText) throws BankException {
        Account a = find(accountNumber);
        a.withdraw(parse(amountText));
        return a;
    }

    public List<Account> getAccounts(String customerId) {
        List<Account> list = new ArrayList<>();
        for (Account a : STORE) {
            if (a.getCustomerId().equals(customerId)) {
                list.add(a);
            }
        }
        return list;
    }

    public List<Transaction> getHistory(String accountNumber) {
        try {
            List<Transaction> list = new ArrayList<>(find(accountNumber).getTransactions());
            Collections.reverse(list);
            return list;
        } catch (BankException e) {
            return new ArrayList<>();
        }
    }

    Account find(String accountNumber) throws BankException {
        for (Account a : STORE) {
            if (a.getAccountNumber().equals(accountNumber)) {
                return a;
            }
        }
        throw new BankException("Account not found.");
    }

    private static BigDecimal parse(String text) throws BankException {
        try {
            return new BigDecimal(text.trim());
        } catch (Exception e) {
            throw new BankException("Please enter a valid number for the amount.");
        }
    }
}
