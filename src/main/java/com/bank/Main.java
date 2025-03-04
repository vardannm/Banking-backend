package com.bank;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Configuration
public class Main implements CommandLineRunner {
    private static List<BankCustomer> customers = new ArrayList<>();
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bankdbfirst?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "1234";

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load JDBC Driver: " + e.getMessage());
            return;
        }

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Create New Customer");
            System.out.println("2. Customer Login");
            System.out.println("3. Admin Dashboard");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Invalid choice. Try again.");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    createNewCustomer(scanner);
                    break;
                case 2:
                    customerLogin(scanner);
                    break;
                case 3:
                    adminDashboard(scanner);
                    break;
                case 4:
                    running = false;
                    System.out.println("Exiting... Thank you!");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
        scanner.close();
    }

    private void createNewCustomer(Scanner scanner) {
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();
        System.out.print("Enter customer ID: ");
        String customerID = scanner.nextLine();
        System.out.print("Enter customer email: ");
        String email = scanner.nextLine();
        System.out.print("Enter customer phone number: ");
        String phoneNumber = scanner.nextLine();
        System.out.print("Set a PIN for security: ");
        String pin = scanner.nextLine();

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
                System.out.println("Customer saved to database successfully!");
                System.out.print("How many accounts do you want to create? ");
                int numAccounts = getValidInt(scanner);
                for (int i = 0; i < numAccounts; i++) {
                    System.out.print("Enter account number for Account " + (i + 1) + ": ");
                    String accountNumber = scanner.nextLine();
                    System.out.print("Enter initial balance: ");
                    double balance = getValidDouble(scanner);
                    BankAccount account = new BankAccount(accountNumber, balance, pin);
                    customer.addAccount(account);

                    PreparedStatement accountStmt = conn.prepareStatement(
                            "INSERT INTO accounts (account_number, customer_id, balance, hashed_pin) VALUES (?, ?, ?, ?)");
                    accountStmt.setString(1, accountNumber);
                    accountStmt.setString(2, customerID);
                    accountStmt.setDouble(3, balance);
                    accountStmt.setString(4, customer.hashPin(pin));
                    accountStmt.executeUpdate();
                }
                conn.commit();
                System.out.println("Customer and accounts created successfully!");
                customers.add(customer);
            } catch (SQLException e) {
                conn.rollback();
                if (e.getSQLState().equals("23000")) {
                    System.err.println("Error: ID '" + customerID + "' or account number already exists.");
                } else {
                    System.err.println("Database error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void customerLogin(Scanner scanner) {
        System.out.print("Enter Customer ID: ");
        String customerID = scanner.nextLine();
        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine();

        System.out.println("Attempting login with customerID: " + customerID + ", pin: " + pin);
        BankCustomer customer = loadCustomerFromDatabase(customerID);
        if (customer == null) {
            System.out.println("Customer not found for ID: " + customerID);
        } else {
            System.out.println("Customer found: " + customer.getName() + ", validating PIN...");
            if (customer.validatePin(pin)) {
                System.out.println("PIN valid, proceeding...");
                if (!customers.contains(customer)) customers.add(customer);
                performCustomerOperations(scanner, customer);
            } else {
                System.out.println("PIN validation failed for " + customerID);
            }
        }
    }

    private void adminDashboard(Scanner scanner) {
        System.out.print("Enter Admin Password: ");
        String password = scanner.nextLine();
        if ("admin123".equals(password)) {
            AdminDashboard adminDashboard = new AdminDashboard(customers);
            adminDashboard.displayAdminMenu();
        } else {
            System.out.println("Invalid admin password.");
        }
    }

    private BankCustomer loadCustomerFromDatabase(String customerID) {
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

                PreparedStatement accountStmt = conn.prepareStatement(
                        "SELECT account_number, balance, hashed_pin FROM accounts WHERE customer_id = ?");
                accountStmt.setString(1, customerID);
                ResultSet accountRs = accountStmt.executeQuery();
                while (accountRs.next()) {
                    String accountNumber = accountRs.getString("account_number");
                    double balance = accountRs.getDouble("balance");
                    BankAccount account = new BankAccount(accountNumber, balance, "");
                    customer.addAccount(account);
                }
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

    private void performCustomerOperations(Scanner scanner, BankCustomer customer) {
        boolean running = true;
        int pinAttempts = 0;
        final int MAX_PIN_ATTEMPTS = 3;

        while (running) {
            System.out.println("\nChoose an operation:");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. View Customer Details");
            System.out.println("4. View Transaction History");
            System.out.println("5. Transfer Between Accounts");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Invalid choice.");
                scanner.nextLine();
                continue;
            }

            if (choice >= 1 && choice <= 5) {
                System.out.print("Enter your PIN: ");
                String enteredPin = scanner.nextLine();
                if (!customer.validatePin(enteredPin)) {
                    pinAttempts++;
                    System.out.println("Invalid PIN. Attempts remaining: " + (MAX_PIN_ATTEMPTS - pinAttempts));
                    if (pinAttempts >= MAX_PIN_ATTEMPTS) {
                        System.out.println("Maximum PIN attempts reached. Aborting...");
                        running = false;
                    }
                    continue;
                }
                pinAttempts = 0;
            }

            switch (choice) {
                case 1:
                    handleDeposit(scanner, customer);
                    break;
                case 2:
                    handleWithdrawal(scanner, customer);
                    break;
                case 3:
                    customer.displayCustomerInfo();
                    break;
                case 4:
                    displayTransactionHistory(customer);
                    break;
                case 5:
                    handleTransfer(scanner, customer);
                    break;
                case 6:
                    running = false;
                    System.out.println("Exiting... Thank you!");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void handleDeposit(Scanner scanner, BankCustomer customer) {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        System.out.print("Enter amount to deposit: ");
        double amount = getValidDouble(scanner);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);
            for (BankAccount acc : customer.getAccounts()) {
                if (acc.getAccountNumber().equals(accountNumber)) {
                    acc.deposit(amount);
                    updateBalance(conn, accountNumber, acc.getBalance());
                    logTransaction(conn, accountNumber, "DEPOSIT", amount);
                    System.out.println("Deposit successful!");
                    conn.commit();
                    return;
                }
            }
            System.out.println("Account not found.");
            conn.rollback();
        } catch (SQLException e) {
            System.err.println("Error during deposit: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleWithdrawal(Scanner scanner, BankCustomer customer) {
        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();
        System.out.print("Enter amount to withdraw: ");
        double amount = getValidDouble(scanner);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);
            for (BankAccount acc : customer.getAccounts()) {
                if (acc.getAccountNumber().equals(accountNumber)) {
                    acc.withdraw(amount);
                    updateBalance(conn, accountNumber, acc.getBalance());
                    logTransaction(conn, accountNumber, "WITHDRAWAL", amount);
                    System.out.println("Withdrawal successful!");
                    conn.commit();
                    return;
                }
            }
            System.out.println("Account not found.");
            conn.rollback();
        } catch (SQLException e) {
            System.err.println("Error during withdrawal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleTransfer(Scanner scanner, BankCustomer customer) {
        System.out.print("Enter source account number: ");
        String fromAccount = scanner.nextLine();
        System.out.print("Enter destination account number: ");
        String toAccount = scanner.nextLine();
        System.out.print("Enter amount to transfer: ");
        double amount = getValidDouble(scanner);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);
            BankAccount fromAcc = null, toAcc = null;
            for (BankAccount acc : customer.getAccounts()) {
                if (acc.getAccountNumber().equals(fromAccount)) fromAcc = acc;
                if (acc.getAccountNumber().equals(toAccount)) toAcc = acc;
            }
            if (fromAcc != null && toAcc != null) {
                customer.transfer(fromAccount, toAccount, amount);
                updateBalance(conn, fromAccount, fromAcc.getBalance());
                updateBalance(conn, toAccount, toAcc.getBalance());
                logTransaction(conn, fromAccount, "TRANSFER_SENT", amount);
                logTransaction(conn, toAccount, "TRANSFER_RECEIVED", amount);
                System.out.println("Transfer successful!");
                conn.commit();
            } else {
                System.out.println("One or both accounts not found.");
                conn.rollback();
            }
        } catch (SQLException e) {
            System.err.println("Error during transfer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateBalance(Connection conn, String accountNumber, double balance) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "UPDATE accounts SET balance = ? WHERE account_number = ?");
        stmt.setDouble(1, balance);
        stmt.setString(2, accountNumber);
        stmt.executeUpdate();
    }

    private void logTransaction(Connection conn, String accountNumber, String type, double amount) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO transactions (account_number, transaction_type, amount) VALUES (?, ?, ?)");
        stmt.setString(1, accountNumber);
        stmt.setString(2, type);
        stmt.setDouble(3, amount);
        stmt.executeUpdate();
    }

    private void displayTransactionHistory(BankCustomer customer) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            for (BankAccount acc : customer.getAccounts()) {
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT transaction_type, amount, transaction_date FROM transactions WHERE account_number = ?");
                stmt.setString(1, acc.getAccountNumber());
                ResultSet rs = stmt.executeQuery();
                System.out.println("\nTransaction History for Account: " + acc.getAccountNumber());
                while (rs.next()) {
                    String type = rs.getString("transaction_type");
                    double amount = rs.getDouble("amount");
                    Timestamp date = rs.getTimestamp("transaction_date");
                    System.out.printf("Type: %s, Amount: %.2f, Date: %s%n", type, amount, date);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transaction history: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int getValidInt(Scanner scanner) {
        try {
            int value = scanner.nextInt();
            scanner.nextLine();
            return value;
        } catch (Exception e) {
            System.out.println("Invalid input. Defaulting to 0.");
            scanner.nextLine();
            return 0;
        }
    }

    private double getValidDouble(Scanner scanner) {
        try {
            double value = scanner.nextDouble();
            scanner.nextLine();
            return value;
        } catch (Exception e) {
            System.out.println("Invalid input. Defaulting to 0.0.");
            scanner.nextLine();
            return 0.0;
        }
    }
}