package bw.bank;

import bw.bank.exception.BankException;
import bw.bank.model.Account;
import bw.bank.model.ChequeAccount;
import bw.bank.model.InvestmentAccount;
import bw.bank.model.SavingsAccount;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Unit tests for the core model (no database, no GUI). */
class AccountModelTest {
    private static BigDecimal bd(String s) { return new BigDecimal(s); }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, bd(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    @Test
    void depositIncreasesBalanceAndRecordsTransaction() throws BankException {
        Account a = new SavingsAccount("AC1", "CU1", "Main", LocalDate.now(), BigDecimal.ZERO);
        a.deposit(bd("250.50"));
        assertMoney("250.50", a.getBalance());
        assertEquals(1, a.getTransactions().size());
    }

    @Test
    void depositRejectsZeroAndNegative() {
        Account a = new SavingsAccount("AC1", "CU1", "Main", LocalDate.now(), BigDecimal.ZERO);
        assertThrows(BankException.class, () -> a.deposit(BigDecimal.ZERO));
        assertThrows(BankException.class, () -> a.deposit(bd("-5")));
    }

    @Test
    void savingsDoesNotAllowWithdrawals() {
        Account a = new SavingsAccount("AC1", "CU1", "Main", LocalDate.now(), bd("1000"));
        assertThrows(BankException.class, () -> a.withdraw(bd("10")));
    }

    @Test
    void investmentWithdrawalCannotExceedBalance() throws BankException {
        Account a = new InvestmentAccount("AC2", "CU1", "Main", LocalDate.now(), bd("600"));
        a.withdraw(bd("100"));
        assertMoney("500.00", a.getBalance());
        assertThrows(BankException.class, () -> a.withdraw(bd("500.01")));
    }

    @Test
    void investmentNeedsMinimumInitialDeposit() {
        assertThrows(BankException.class, () -> InvestmentAccount.validateInitialDeposit(bd("499.99")));
    }

    @Test
    void chequeAllowsOverdraftUpToLimit() throws BankException {
        Account a = new ChequeAccount("AC3", "CU1", "Main", LocalDate.now(), bd("100"), "Debswana", "Orapa");
        a.withdraw(bd("1100"));
        assertMoney("-1000.00", a.getBalance());
        assertThrows(BankException.class, () -> a.withdraw(bd("0.01")));
    }

    @Test
    void chequeRequiresEmployerDetails() {
        assertThrows(BankException.class,
                () -> new ChequeAccount("AC3", "CU1", "Main", LocalDate.now(), BigDecimal.ZERO, "", ""));
    }

    @Test
    void interestRatesAreCorrect() {
        SavingsAccount s = new SavingsAccount("AC1", "CU1", "Main", LocalDate.now(), bd("10000"));
        InvestmentAccount i = new InvestmentAccount("AC2", "CU1", "Main", LocalDate.now(), bd("1000"));
        assertMoney("5.00", s.calculateInterest());    // 0.05%
        assertMoney("50.00", i.calculateInterest());   // 5%
    }

    @Test
    void closedAccountRejectsTransactions() {
        Account a = new SavingsAccount("AC1", "CU1", "Main", LocalDate.now(), bd("100"));
        a.setActive(false);
        assertThrows(BankException.class, () -> a.deposit(bd("10")));
    }
}
