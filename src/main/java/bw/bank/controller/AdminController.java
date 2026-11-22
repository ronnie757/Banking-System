package bw.bank.controller;

import bw.bank.dao.CustomerDAO;
import bw.bank.exception.BankException;
import bw.bank.model.Account;
import bw.bank.model.Customer;
import bw.bank.model.Transaction;

import java.util.List;

/** Administrator functions: search customers, update details, interest, open/close accounts. */
public class AdminController {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AccountController accountController = new AccountController();

    public List<Customer> searchCustomers(String term) {
        if (term == null || term.isBlank()) {
            return customerDAO.findAll();
        }
        return customerDAO.search(term);
    }

    public void updateCustomer(String customerId, String address, String phone, String email)
            throws BankException {
        Customer c = customerDAO.findById(customerId);
        if (c == null) {
            throw new BankException("Customer not found.");
        }
        if (address == null || address.isBlank()) {
            throw new BankException("Address is required.");
        }
        if (!phone.trim().matches("[0-9+ ]{7,15}")) {
            throw new BankException("Enter a valid phone number (7-15 digits).");
        }
        if (!email.trim().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new BankException("Enter a valid email address.");
        }
        c.updateDetails(address.trim(), phone.trim(), email.trim());
        customerDAO.update(c);
    }

    public List<Account> getAccounts(String customerId) {
        return accountController.getAccounts(customerId);
    }

    public List<Transaction> getHistory(String accountNumber) {
        return accountController.getHistory(accountNumber);
    }

    public int payMonthlyInterest() throws BankException {
        return accountController.payMonthlyInterest();
    }

    public void closeAccount(String accountNumber) throws BankException {
        accountController.setAccountActive(accountNumber, false);
    }

    public void reopenAccount(String accountNumber) throws BankException {
        accountController.setAccountActive(accountNumber, true);
    }
}
