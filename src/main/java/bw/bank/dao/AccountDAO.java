package bw.bank.dao;

import bw.bank.db.Database;
import bw.bank.exception.BankException;
import bw.bank.exception.DataAccessException;
import bw.bank.model.Account;
import bw.bank.model.ChequeAccount;
import bw.bank.model.InvestmentAccount;
import bw.bank.model.SavingsAccount;
import bw.bank.model.Transaction;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** CRUD operations for the accounts table. Rebuilds the right Account subclass when reading. */
public class AccountDAO {
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public void create(Account a) {
        String sql = "INSERT INTO accounts(account_number, customer_id, account_type, branch, opening_date, "
                + "balance, status, employer_name, company_address) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, a.getAccountNumber());
            ps.setString(2, a.getCustomerId());
            ps.setString(3, a.getAccountType().toUpperCase());
            ps.setString(4, a.getBranch());
            ps.setString(5, a.getOpeningDate().toString());
            ps.setDouble(6, a.getBalance().doubleValue());
            ps.setString(7, a.isActive() ? "ACTIVE" : "CLOSED");
            if (a instanceof ChequeAccount) {
                ChequeAccount c = (ChequeAccount) a;
                ps.setString(8, c.getEmployerName());
                ps.setString(9, c.getCompanyAddress());
            } else {
                ps.setNull(8, java.sql.Types.VARCHAR);
                ps.setNull(9, java.sql.Types.VARCHAR);
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save account", e);
        }
    }

    public Account findByNumber(String accountNumber) {
        List<Account> list = query("SELECT * FROM accounts WHERE account_number = ?", accountNumber);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Account> findByCustomer(String customerId) {
        return query("SELECT * FROM accounts WHERE customer_id = ? ORDER BY opening_date, account_number",
                customerId);
    }

    public List<Account> findAll() {
        return query("SELECT * FROM accounts ORDER BY account_number", null);
    }

    /** Saves the new balance AND the transaction together (all or nothing). */
    public void applyTransaction(Account a, Transaction t) {
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE accounts SET balance = ? WHERE account_number = ?")) {
                    ps.setDouble(1, a.getBalance().doubleValue());
                    ps.setString(2, a.getAccountNumber());
                    ps.executeUpdate();
                }
                transactionDAO.insert(con, t);
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not save transaction", e);
        }
    }

    public void updateStatus(String accountNumber, boolean active) {
        String sql = "UPDATE accounts SET status = ? WHERE account_number = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, active ? "ACTIVE" : "CLOSED");
            ps.setString(2, accountNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update account status", e);
        }
    }

    public void delete(String accountNumber) {
        String sql = "DELETE FROM accounts WHERE account_number = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete account", e);
        }
    }

    private List<Account> query(String sql, String param) {
        List<Account> list = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read accounts", e);
        }
        return list;
    }

    private Account map(ResultSet rs) throws SQLException {
        String number = rs.getString("account_number");
        String customerId = rs.getString("customer_id");
        String branch = rs.getString("branch");
        LocalDate opened = LocalDate.parse(rs.getString("opening_date"));
        BigDecimal balance = TransactionDAO.money(rs.getDouble("balance"));
        Account account;
        try {
            switch (rs.getString("account_type")) {
                case "SAVINGS":
                    account = new SavingsAccount(number, customerId, branch, opened, balance);
                    break;
                case "INVESTMENT":
                    account = new InvestmentAccount(number, customerId, branch, opened, balance);
                    break;
                default:
                    account = new ChequeAccount(number, customerId, branch, opened, balance,
                            rs.getString("employer_name"), rs.getString("company_address"));
            }
        } catch (BankException e) {
            throw new SQLException("Corrupt account row " + number + ": " + e.getMessage(), e);
        }
        account.setActive("ACTIVE".equals(rs.getString("status")));
        return account;
    }
}
