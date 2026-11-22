package bw.bank;

import bw.bank.controller.AccountController;
import bw.bank.controller.AdminController;
import bw.bank.controller.AuthController;
import bw.bank.dao.CustomerDAO;
import bw.bank.db.Database;
import bw.bank.exception.BankException;
import bw.bank.model.Account;
import bw.bank.model.Customer;
import bw.bank.model.Transaction;
import bw.bank.model.TransactionType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests: controller -> core model -> DAO -> real SQLite file.
 * The JavaFX views only call these same controller methods, so this covers the same path as the GUI.
 */
class BankingIntegrationTest {
    @TempDir
    static Path tmp;

    static AuthController auth;
    static AccountController accounts;
    static AdminController admin;

    @BeforeAll
    static void setUp() {
        Database.setPath(tmp.resolve("test-bank.db").toString());
        Database.init();
        auth = new AuthController();
        accounts = new AccountController();
        admin = new AdminController();
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    private static Account find(List<Account> list, String type) {
        return list.stream().filter(a -> a.getAccountType().equals(type)).findFirst().orElseThrow();
    }

    @Test
    void registrationLoginAndPasswordHashing() throws BankException {
        Customer c = auth.registerCustomer("Alice", "Banda", "Plot 1, Gaborone", "71234567",
                "alice@example.com", "alice01", "Secret123");
        Customer loggedIn = auth.loginCustomer("alice01", "Secret123");
        assertEquals(c.getCustomerId(), loggedIn.getCustomerId());

        assertThrows(BankException.class, () -> auth.loginCustomer("alice01", "wrong"));
        assertThrows(BankException.class, () -> auth.registerCustomer("Al", "B", "x", "71234567",
                "a@b.co", "alice01", "Secret123"));                       // duplicate username
        assertNotEquals("Secret123", new CustomerDAO().findByUsername("alice01").getPasswordHash());
    }

    @Test
    void fullCustomerFlowWithAllThreeAccountTypes() throws BankException {
        Customer c = auth.registerCustomer("Bob", "Tau", "Plot 2, Gaborone", "72345678",
                "bob@example.com", "bobtau", "Secret123");
        String id = auth.loginCustomer("bobtau", "Secret123").getCustomerId();

        Account savings = accounts.openAccount(id, "Savings", "1000", null, null);
        accounts.deposit(savings.getAccountNumber(), "500");
        assertThrows(BankException.class, () -> accounts.withdraw(savings.getAccountNumber(), "10"));

        assertThrows(BankException.class, () -> accounts.openAccount(id, "Investment", "100", null, null));
        Account invest = accounts.openAccount(id, "Investment", "1000", null, null);
        accounts.withdraw(invest.getAccountNumber(), "200");

        assertThrows(BankException.class, () -> accounts.openAccount(id, "Cheque", "0", "", ""));
        Account cheque = accounts.openAccount(id, "Cheque", "", "Debswana", "Orapa");
        accounts.withdraw(cheque.getAccountNumber(), "500");               // overdraft
        assertThrows(BankException.class, () -> accounts.withdraw(cheque.getAccountNumber(), "600"));

        // a customer holds multiple accounts, reloaded from the database
        List<Account> reloaded = accounts.getAccounts(id);
        assertEquals(3, reloaded.size());
        assertMoney("1500.00", find(reloaded, "Savings").getBalance());
        assertMoney("800.00", find(reloaded, "Investment").getBalance());
        assertMoney("-500.00", find(reloaded, "Cheque").getBalance());

        // monthly interest: 0.05% of 1500 = 0.75 ; 5% of 800 = 40.00 ; cheque earns none
        admin.payMonthlyInterest();
        reloaded = accounts.getAccounts(id);
        assertMoney("1500.75", find(reloaded, "Savings").getBalance());
        assertMoney("840.00", find(reloaded, "Investment").getBalance());
        assertMoney("-500.00", find(reloaded, "Cheque").getBalance());

        // transaction history persisted in the database
        List<Transaction> history = accounts.getHistory(savings.getAccountNumber());
        assertEquals(3, history.size());                                    // initial, deposit, interest
        assertEquals(TransactionType.INTEREST, history.get(0).getType());  // newest first
    }

    @Test
    void invalidAmountsAreRejected() throws BankException {
        Customer c = auth.registerCustomer("Carl", "Dube", "Plot 3", "73456789",
                "carl@example.com", "carldube", "Secret123");
        Account a = accounts.openAccount(c.getCustomerId(), "Savings", "100", null, null);
        assertThrows(BankException.class, () -> accounts.deposit(a.getAccountNumber(), "abc"));
        assertThrows(BankException.class, () -> accounts.deposit(a.getAccountNumber(), "-50"));
        assertThrows(BankException.class, () -> accounts.deposit(a.getAccountNumber(), "0"));
        assertMoney("100.00", accounts.getAccount(a.getAccountNumber()).getBalance());
    }

    @Test
    void administratorFlow() throws BankException {
        assertThrows(BankException.class, () -> auth.loginAdmin("admin", "bad"));
        assertEquals("admin", auth.loginAdmin("admin", "Admin@123").getUsername());

        Customer c = auth.registerCustomer("Dina", "Khama", "Plot 4", "74567890",
                "dina@example.com", "dinakh", "Secret123");
        Account a = accounts.openAccount(c.getCustomerId(), "Savings", "100", null, null);

        assertTrue(admin.searchCustomers("Khama").stream()
                .anyMatch(x -> x.getCustomerId().equals(c.getCustomerId())));
        assertTrue(admin.searchCustomers(a.getAccountNumber()).stream()
                .anyMatch(x -> x.getCustomerId().equals(c.getCustomerId())));

        admin.updateCustomer(c.getCustomerId(), "New Address", "75000000", "dina2@example.com");
        assertEquals("New Address", new CustomerDAO().findById(c.getCustomerId()).getAddress());

        admin.closeAccount(a.getAccountNumber());
        assertThrows(BankException.class, () -> accounts.deposit(a.getAccountNumber(), "10"));
        assertFalse(accounts.getAccount(a.getAccountNumber()).isActive());

        admin.reopenAccount(a.getAccountNumber());
        accounts.deposit(a.getAccountNumber(), "10");
        assertMoney("110.00", accounts.getAccount(a.getAccountNumber()).getBalance());
    }
}
