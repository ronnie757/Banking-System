package bw.bank;

import bw.bank.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Demo {
    public static void main(String[] args) throws Exception {
        Account savings = new SavingsAccount("AC1", "CU1", "Main Branch", LocalDate.now(), BigDecimal.ZERO);
        savings.deposit(new BigDecimal("1500"));
        System.out.println(savings);
        System.out.println("Interest due: " + ((InterestBearing) savings).calculateInterest());

        try {
            savings.withdraw(new BigDecimal("100"));
        } catch (Exception e) {
            System.out.println("Blocked: " + e.getMessage());
        }

        Account cheque = new ChequeAccount("AC2", "CU1", "Main Branch", LocalDate.now(),
                BigDecimal.ZERO, "Debswana", "Orapa");
        cheque.withdraw(new BigDecimal("500"));
        System.out.println(cheque);
    }
}
