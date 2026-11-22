package bw.bank.controller;

import bw.bank.dao.AdminDAO;
import bw.bank.dao.CustomerDAO;
import bw.bank.db.Database;
import bw.bank.exception.BankException;
import bw.bank.model.BankAdministrator;
import bw.bank.model.Customer;
import bw.bank.util.PasswordUtil;

/** Handles registration and login. Validates input, then uses the DAOs and model. */
public class AuthController {
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final AdminDAO adminDAO = new AdminDAO();

    public Customer registerCustomer(String firstName, String surname, String address, String phone,
                                     String email, String username, String password) throws BankException {
        firstName = clean(firstName);
        surname = clean(surname);
        address = clean(address);
        phone = clean(phone);
        email = clean(email);
        username = clean(username).toLowerCase();

        if (firstName.isEmpty() || surname.isEmpty() || address.isEmpty()) {
            throw new BankException("First name, surname and address are required.");
        }
        if (!phone.matches("[0-9+ ]{7,15}")) {
            throw new BankException("Enter a valid phone number (7-15 digits).");
        }
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new BankException("Enter a valid email address.");
        }
        if (username.length() < 4 || username.contains(" ")) {
            throw new BankException("Username must be at least 4 characters with no spaces.");
        }
        if (password == null || password.length() < 6) {
            throw new BankException("Password must be at least 6 characters.");
        }
        if (customerDAO.findByUsername(username) != null) {
            throw new BankException("That username is already taken.");
        }

        Customer customer = new Customer(Database.newId("CU", "customers", "customer_id"),
                firstName, surname, address, phone, email, username, PasswordUtil.hash(password));
        customerDAO.create(customer);
        return customer;
    }

    public Customer loginCustomer(String username, String password) throws BankException {
        Customer c = customerDAO.findByUsername(clean(username).toLowerCase());
        if (c == null || !PasswordUtil.verify(password, c.getPasswordHash())) {
            throw new BankException("Invalid username or password.");
        }
        return c;
    }

    public BankAdministrator loginAdmin(String username, String password) throws BankException {
        BankAdministrator a = adminDAO.findByUsername(clean(username).toLowerCase());
        if (a == null || !PasswordUtil.verify(password, a.getPasswordHash())) {
            throw new BankException("Invalid administrator username or password.");
        }
        return a;
    }

    private static String clean(String s) {
        return s == null ? "" : s.trim();
    }
}
