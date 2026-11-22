package bw.bank.dao;

import bw.bank.db.Database;
import bw.bank.exception.DataAccessException;
import bw.bank.model.Transaction;
import bw.bank.model.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Create and read for the transactions table. */
public class TransactionDAO {

    /** Uses an existing connection so it can share a database transaction. */
    public void insert(Connection con, Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions(transaction_id, account_number, txn_date, type, amount, "
                + "description, balance_after) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getTransactionId());
            ps.setString(2, t.getAccountNumber());
            ps.setString(3, t.getDate().toString());
            ps.setString(4, t.getType().name());
            ps.setDouble(5, t.getAmount().doubleValue());
            ps.setString(6, t.getDescription());
            ps.setDouble(7, t.getBalanceAfter().doubleValue());
            ps.executeUpdate();
        }
    }

    public void create(Transaction t) {
        try (Connection con = Database.getConnection()) {
            insert(con, t);
        } catch (SQLException e) {
            throw new DataAccessException("Could not save transaction", e);
        }
    }

    /** Newest first. */
    public List<Transaction> findByAccount(String accountNumber) {
        String sql = "SELECT * FROM transactions WHERE account_number = ? ORDER BY txn_date DESC, rowid DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Transaction(rs.getString("transaction_id"),
                            rs.getString("account_number"),
                            LocalDateTime.parse(rs.getString("txn_date")),
                            TransactionType.valueOf(rs.getString("type")),
                            money(rs.getDouble("amount")),
                            rs.getString("description"),
                            money(rs.getDouble("balance_after"))));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read transactions", e);
        }
        return list;
    }

    static BigDecimal money(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
