package bw.bank.controller;

import bw.bank.exception.BankException;
import bw.bank.model.Account;
import bw.bank.model.Customer;
import bw.bank.model.InterestBearing;
import bw.bank.model.Transaction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** TEMPORARY in-memory version so the screens can run. Replaced by the real one in Step 6. */
public class AdminController {
    private final AccountController accounts = new AccountController();

    public List<Customer> searchCustomers(String term) {
        List<Customer> list = new ArrayList<>();
        list.add(new Customer("CU100001", "Demo", "Customer", "Plot 1, Gaborone",
                "71234567", "demo@example.com", "demo", "x"));
        return list;
    }

    public void updateCustomer(String customerId, String address, String phone, String email)
            throws BankException {
        // temporary: nothing is saved yet
    }

    public List<Account> getAccounts(String customerId) {
        return accounts.getAccounts(customerId);
    }

    public List<Transaction> getHistory(String accountNumber) {
        return accounts.getHistory(accountNumber);
    }

    public int payMonthlyInterest() throws BankException {
        int paid = 0;
        for (Account a : AccountController.STORE) {
            if (a.isActive() && a instanceof InterestBearing) {
                BigDecimal interest = ((InterestBearing) a).calculateInterest();
                if (interest.signum() > 0) {
                    a.creditInterest(interest);
                    paid++;
                }
            }
        }
        return paid;
    }

    public void closeAccount(String accountNumber) throws BankException {
        accounts.find(accountNumber).setActive(false);
    }

    public void reopenAccount(String accountNumber) throws BankException {
        accounts.find(accountNumber).setActive(true);
    }
}
