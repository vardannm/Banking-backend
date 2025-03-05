package com.bank;

import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class BankService {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bankdbfirst?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    public BankCustomer login(String customerID, String pin) {
        System.out.println("Login attempt: customerID=" + customerID + ", pin=" + pin);
        BankCustomer customer = loadCustomer(customerID);
        if (customer == null) {
            System.out.println("Customer not found for ID: " + customerID);
        } else {
            System.out.println("Found customer: " + customer.getName() + ", checking PIN...");
            if (customer.validatePin(pin)) {
                System.out.println("PIN valid. Login successful for " + customerID);
                return customer;
            } else {
                System.out.println("PIN validation failed for customerID: " + customerID);
            }
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
            } else {
                System.out.println("No customer found with ID: " + customerID);
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
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Balance updated for account: " + accountNumber);
            } else {
                System.out.println("No account found with number: " + accountNumber);
            }
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
            stmt.setString(4, new BankCustomer("", "", pin, "", "").hashPin(pin));
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Account " + accountNumber + " added for customerID: " + customerID);
            }
        } catch (SQLException e) {
            if (e.getSQLState().equals("23000")) {
                System.err.println("Error: Account number '" + accountNumber + "' already exists.");
            } else {
                System.err.println("Error adding account: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void logTransaction(String accountNumber, String type, double amount) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO transactions (account_number, transaction_type, amount) VALUES (?, ?, ?)");
            stmt.setString(1, accountNumber);
            stmt.setString(2, type);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
            System.out.println("Transaction logged: " + type + " for " + accountNumber);
        } catch (SQLException e) {
            System.err.println("Error logging transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<BankAccount.Transaction> getTransactions(String customerID) {
        List<BankAccount.Transaction> transactions = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT t.transaction_type, t.amount, t.transaction_date " +
                            "FROM transactions t " +
                            "JOIN accounts a ON t.account_number = a.account_number " +
                            "WHERE a.customer_id = ?");
            stmt.setString(1, customerID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String type = rs.getString("transaction_type");
                double amount = rs.getDouble("amount");
                String dateTime = rs.getTimestamp("transaction_date").toString();
                transactions.add(new BankAccount.Transaction(type, amount) {
                    @Override
                    public String toString() {
                        return dateTime + " | " + type + " | $" + amount;
                    }
                    @Override
                    public String getDateTime() {
                        return dateTime;
                    }
                });
            }
            System.out.println("Fetched " + transactions.size() + " transactions for customerID: " + customerID);
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
            e.printStackTrace();
        }
        return transactions;
    }

    public void transfer(String customerID, String fromAccountNumber, String toAccountNumber, double amount) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);
            try {
                BankCustomer customer = loadCustomer(customerID);
                if (customer == null) throw new IllegalArgumentException("Customer not found");

                customer.transfer(fromAccountNumber, toAccountNumber, amount);

                for (BankAccount acc : customer.getAccounts()) {
                    if (acc.getAccountNumber().equals(fromAccountNumber)) {
                        updateBalance(fromAccountNumber, acc.getBalance());
                        logTransaction(fromAccountNumber, "TRANSFER_SENT", amount);
                    }
                    if (acc.getAccountNumber().equals(toAccountNumber)) {
                        updateBalance(toAccountNumber, acc.getBalance());
                        logTransaction(toAccountNumber, "TRANSFER_RECEIVED", amount);
                    }
                }
                conn.commit();
                System.out.println("Transfer successful from " + fromAccountNumber + " to " + toAccountNumber);
            } catch (Exception e) {
                conn.rollback();
                System.err.println("Transfer failed: " + e.getMessage());
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Database error during transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public BankCustomer createNewCustomer(String name, String customerID, String email, String phoneNumber, String pin, List<AccountInfo> accounts) {
        BankCustomer customer = new BankCustomer(name, customerID, pin, email, phoneNumber);
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);
            try {
                PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO customers (customer_id, name, hashed_pin, email, phone_number) VALUES (?, ?, ?, ?, ?)");
                stmt.setString(1, customerID);
                stmt.setString(2, name);
                stmt.setString(3, customer.hashPin(pin));
                stmt.setString(4, email);
                stmt.setString(5, phoneNumber);
                stmt.executeUpdate();

                for (AccountInfo account : accounts) {
                    BankAccount bankAccount = new BankAccount(account.accountNumber(), account.initialBalance(), pin);
                    customer.addAccount(bankAccount);
                    PreparedStatement accountStmt = conn.prepareStatement(
                            "INSERT INTO accounts (account_number, customer_id, balance, hashed_pin) VALUES (?, ?, ?, ?)");
                    accountStmt.setString(1, account.accountNumber());
                    accountStmt.setString(2, customerID);
                    accountStmt.setDouble(3, account.initialBalance());
                    accountStmt.setString(4, customer.hashPin(pin));
                    accountStmt.executeUpdate();
                }
                conn.commit();
                System.out.println("Customer " + customerID + " and accounts created successfully!");
                return customer;
            } catch (SQLException e) {
                conn.rollback();
                if (e.getSQLState().equals("23000")) {
                    throw new IllegalArgumentException("Error: ID '" + customerID + "' or account number already exists.");
                } else {
                    System.err.println("Database error: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    // Nested static record for account info
    public static record AccountInfo(String accountNumber, double initialBalance) {}
}