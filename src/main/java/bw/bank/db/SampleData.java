package bw.bank.db;

import bw.bank.controller.AccountController;
import bw.bank.controller.AuthController;
import bw.bank.dao.CustomerDAO;
import bw.bank.model.Customer;

/** Inserts 10 sample customers (demo1..demo10, password Password1) the first time the app runs. */
public final class SampleData {
    private SampleData() { }

    public static void seedIfEmpty() {
        if (new CustomerDAO().count() > 0) {
            return;
        }
        String[][] people = {
                {"Kabo", "Molefe"}, {"Neo", "Sebego"}, {"Thato", "Kgosi"}, {"Lorato", "Tau"},
                {"Mpho", "Dube"}, {"Tebogo", "Seretse"}, {"Boitumelo", "Motsumi"},
                {"Keabetswe", "Ramotswe"}, {"Onkabetse", "Mosweu"}, {"Gorata", "Masire"}
        };
        AuthController auth = new AuthController();
        AccountController accounts = new AccountController();
        try {
            for (int i = 0; i < people.length; i++) {
                int n = i + 1;
                Customer c = auth.registerCustomer(people[i][0], people[i][1], "Plot " + (100 + n) + ", Gaborone",
                        "7123" + String.format("%04d", n), "demo" + n + "@example.com",
                        "demo" + n, "Password1");
                accounts.openAccount(c.getCustomerId(), "Savings", String.valueOf(1000 + n * 250), null, null);
                if (n % 2 == 0) {
                    accounts.openAccount(c.getCustomerId(), "Investment", String.valueOf(500 + n * 100), null, null);
                }
                if (n % 3 == 0) {
                    String cheque = accounts.openAccount(c.getCustomerId(), "Cheque", "2000",
                            "Debswana", "Orapa Mine, Botswana").getAccountNumber();
                    accounts.withdraw(cheque, "350");
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not insert sample data: " + e.getMessage(), e);
        }
    }
}
