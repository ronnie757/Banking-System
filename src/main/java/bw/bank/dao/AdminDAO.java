package bw.bank.dao;

import bw.bank.db.Database;
import bw.bank.exception.DataAccessException;
import bw.bank.model.BankAdministrator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {

    public void create(BankAdministrator a) {
        String sql = "INSERT INTO admins(admin_id, name, username, password_hash) VALUES (?,?,?,?)";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, a.getAdminId());
            ps.setString(2, a.getName());
            ps.setString(3, a.getUsername());
            ps.setString(4, a.getPasswordHash());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Could not save administrator", e);
        }
    }

    public BankAdministrator findByUsername(String username) {
        String sql = "SELECT * FROM admins WHERE username = ?";
        try (Connection con = Database.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new BankAdministrator(rs.getString("admin_id"), rs.getString("name"),
                            rs.getString("username"), rs.getString("password_hash"));
                }
                return null;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not read administrator", e);
        }
    }
}
