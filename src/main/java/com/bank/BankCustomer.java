package com.bank;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BankCustomer {
    private String name;
    private String customerID;
    String hashedPin;
    private String phoneNumber;
    private String email;
    private List<BankAccount> accounts;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bankdbfirst?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    public BankCustomer(String name, String customerID, String pin, String email, String phoneNumber) {
        this.name = name;
        this.customerID = customerID;
        this.hashedPin = pin.isEmpty() ? null : hashPin(pin);
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.accounts = new ArrayList<>();
        if (!pin.isEmpty()) saveToDatabase();
    }

    protected String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(pin.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing PIN", e);
        }
    }

    private void saveToDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO customers (customer_id, name, hashed_pin, email, phone_number) VALUES (?, ?, ?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE name = ?, hashed_pin = ?, email = ?, phone_number = ?")) {
            stmt.setString(1, customerID);
            stmt.setString(2, name);
            stmt.setString(3, hashedPin);
            stmt.setString(4, email);
            stmt.setString(5, phoneNumber);
            stmt.setString(6, name);
            stmt.setString(7, hashedPin);
            stmt.setString(8, email);
            stmt.setString(9, phoneNumber);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving to database: " + e.getMessage());
        }
    }

    public boolean validatePin(String enteredPin) {
        return hashedPin != null && hashedPin.equals(hashPin(enteredPin));
    }

    public String getCustomerID() { return customerID; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }

    public void addAccount(BankAccount account) {
        accounts.add(account);
        System.out.println("Account " + account.getAccountNumber() + " added for " + name + " Email: " + email + " Phone number: " + phoneNumber);
    }

    public void displayCustomerInfo() {
        System.out.println("\nCustomer ID: " + customerID);
        System.out.println("Customer Name: " + name);
        System.out.println("Phone number: " + phoneNumber);
        System.out.println("Email: " + email);
        System.out.println("Accounts:");
        for (BankAccount account : accounts) {
            account.displayBalance();
        }
    }

    public List<BankAccount> getAccounts() { return accounts; }

    public void transfer(String fromAccountNumber, String toAccountNumber, double amount) {
        BankAccount fromAccount = null;
        BankAccount toAccount = null;
        for (BankAccount account : accounts) {
            if (account.getAccountNumber().equals(fromAccountNumber)) fromAccount = account;
            if (account.getAccountNumber().equals(toAccountNumber)) toAccount = account;
        }
        if (fromAccount == null || toAccount == null) {
            System.out.println("One or both account numbers are invalid.");
        } else if (fromAccount == toAccount) {
            System.out.println("Cannot transfer to the same account.");
        } else if (amount <= 0) {
            System.out.println("Transfer amount must be greater than 0.");
        } else {
            double originalBalance = fromAccount.getBalance();
            fromAccount.withdraw(amount);
            if (fromAccount.getBalance() < originalBalance) {
                toAccount.deposit(amount);
                System.out.println("Transferred $" + amount + " from Account " + fromAccountNumber + " to Account " + toAccountNumber);
            }
        }
    }

    public void displayTransactionHistory() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts found for " + name);
        } else {
            System.out.println("\n=== Transaction History for " + name + " ===");
            for (BankAccount account : accounts) {
                account.displayTransactionHistory();
            }
        }
    }
}