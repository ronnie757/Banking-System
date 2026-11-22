package bw.bank.dao;

import bw.bank.db.Database;
import bw.bank.exception.DataAccessException;
import bw.bank.model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** CRUD operations for the customers table. */
public class CustomerDAO {

    public void create(Customer c) {
        String sql = "INSERT INTO customers(customer_id, first_name, surname, address, phone, email, "
                + "username, password_hash) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCustomerId());
            ps.setString(2, c.getFirstName());
            ps.setString(3, c.getSurname());
            ps.setString(4, c.getAddress());
            ps.setString(5, c.getPhoneNumber());
            ps.setString(6, c.getEmail());
            ps.setString(7, c.getUsername());
            ps.setString(8, c.getPasswordHash());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save customer", e);
        }
    }

    public Customer findById(String id) {
        return findOne("SELECT * FROM customers WHERE customer_id = ?", id);
    }

    public Customer findByUsername(String username) {
        return findOne("SELECT * FROM customers WHERE username = ?", username);
    }

    public List<Customer> findAll() {
        List<Customer> list = new ArrayList<>();
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM customers ORDER BY surname, first_name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read customers", e);
        }
        return list;
    }

    /** Search by customer id, name or account number. */
    public List<Customer> search(String term) {
        String sql = "SELECT * FROM customers WHERE customer_id LIKE ? OR first_name LIKE ? "
                + "OR surname LIKE ? OR (first_name || ' ' || surname) LIKE ? "
                + "OR customer_id IN (SELECT customer_id FROM accounts WHERE account_number LIKE ?) "
                + "ORDER BY surname, first_name";
        String pattern = "%" + term.trim() + "%";
        List<Customer> list = new ArrayList<>();
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, pattern);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not search customers", e);
        }
        return list;
    }

    public void update(Customer c) {
        String sql = "UPDATE customers SET address = ?, phone = ?, email = ? WHERE customer_id = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getAddress());
            ps.setString(2, c.getPhoneNumber());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getCustomerId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not update customer", e);
        }
    }

    public void delete(String customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not delete customer", e);
        }
    }

    public int count() {
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM customers");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DataAccessException("Could not count customers", e);
        }
    }

    private Customer findOne(String sql, String param) {
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read customer", e);
        }
    }

    private Customer map(ResultSet rs) throws SQLException {
        return new Customer(rs.getString("customer_id"), rs.getString("first_name"),
                rs.getString("surname"), rs.getString("address"), rs.getString("phone"),
                rs.getString("email"), rs.getString("username"), rs.getString("password_hash"));
    }
}
