package bw.bank.db;

import bw.bank.dao.AdminDAO;
import bw.bank.exception.DataAccessException;
import bw.bank.model.BankAdministrator;
import bw.bank.util.PasswordUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public final class Database {
    private static final Random RANDOM = new Random();
    private static String url = "jdbc:sqlite:" + System.getProperty("bank.db", "banking.db");

    private Database() { }

    public static void setPath(String path) {
        url = "jdbc:sqlite:" + path;
    }

    public static Connection getConnection() throws SQLException {
        Connection con = DriverManager.getConnection(url);
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return con;
    }

    /** Creates the tables and the default administrator (admin / Admin@123). */
    public static void init() {
        try (Connection con = getConnection(); Statement st = con.createStatement();
             InputStream in = Database.class.getResourceAsStream("/schema.sql")) {
            String sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String statement : sql.split(";")) {
                if (!statement.isBlank()) {
                    st.execute(statement);
                }
            }
        } catch (SQLException | IOException e) {
            throw new DataAccessException("Could not initialise the database", e);
        }
        AdminDAO adminDAO = new AdminDAO();
        if (adminDAO.findByUsername("admin") == null) {
            adminDAO.create(new BankAdministrator("AD100001", "System Administrator", "admin",
                    PasswordUtil.hash("Admin@123")));
        }
    }

    /** Generates a unique id such as CU123456 or AC654321. */
    public static String newId(String prefix, String table, String column) {
        try (Connection con = getConnection()) {
            while (true) {
                String id = prefix + (100000 + RANDOM.nextInt(900000));
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT 1 FROM " + table + " WHERE " + column + " = ?")) {
                    ps.setString(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            return id;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Could not generate id", e);
        }
    }
}
