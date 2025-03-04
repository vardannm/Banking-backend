package com.bank;

import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class BankService {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bankdbfirst?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    public BankCustomer login(String customerID, String pin) {
        BankCustomer customer = loadCustomer(customerID);
        if (customer != null && customer.validatePin(pin)) {
            return customer;
        }
        return null;
    }

    public BankCustomer loadCustomer(String customerID) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT name, hashed_pin, email, phone_number FROM customers WHERE customer_id = ?");
            stmt.setString(1, customerID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String hashedPin = rs.getString("hashed_pin");
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phone_number");
                BankCustomer customer = new BankCustomer(name, customerID, "", email, phoneNumber);
                customer.hashedPin = hashedPin;
                loadAccounts(conn, customer, customerID);
                return customer;
            }
        } catch (SQLException e) {
            System.err.println("Error loading customer: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private void loadAccounts(Connection conn, BankCustomer customer, String customerID) throws SQLException {
        PreparedStatement accountStmt = conn.prepareStatement(
                "SELECT account_number, balance FROM accounts WHERE customer_id = ?");
        accountStmt.setString(1, customerID);
        ResultSet accountRs = accountStmt.executeQuery();
        while (accountRs.next()) {
            String accountNumber = accountRs.getString("account_number");
            double balance = accountRs.getDouble("balance");
            BankAccount account = new BankAccount(accountNumber, balance, "");
            customer.addAccount(account);
        }
    }

    public void updateBalance(String accountNumber, double newBalance) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE accounts SET balance = ? WHERE account_number = ?");
            stmt.setDouble(1, newBalance);
            stmt.setString(2, accountNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating balance: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void addAccount(String customerID, String accountNumber, double initialBalance, String pin) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO accounts (account_number, customer_id, balance, hashed_pin) VALUES (?, ?, ?, ?)");
            stmt.setString(1, accountNumber);
            stmt.setString(2, customerID);
            stmt.setDouble(3, initialBalance);
            stmt.setString(4, new BankCustomer("", "", pin, "", "").hashPin(pin)); // Temp object for hash
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding account: " + e.getMessage());
            e.printStackTrace();
        }
    }
}