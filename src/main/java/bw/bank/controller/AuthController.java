package bw.bank.controller;

import bw.bank.exception.BankException;
import bw.bank.model.BankAdministrator;
import bw.bank.model.Customer;

/** TEMPORARY version so the screens can run. Replaced by the real one in Step 6. */
public class AuthController {

    public Customer registerCustomer(String firstName, String surname, String address, String phone,
                                     String email, String username, String password) throws BankException {
        return new Customer("CU100001", firstName, surname, address, phone, email, username, "x");
    }

    public Customer loginCustomer(String username, String password) throws BankException {
        return new Customer("CU100001", "Demo", "Customer", "Plot 1, Gaborone",
                "71234567", "demo@example.com", "demo", "x");
    }

    public BankAdministrator loginAdmin(String username, String password) throws BankException {
        return new BankAdministrator("AD100001", "Demo Administrator", "admin", "x");
    }
}
