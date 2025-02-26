import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static List<BankCustomer> customers = new ArrayList<>();
    // Updated DB_URL to allow public key retrieval
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bankdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "rootpass";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== Main Menu ===");
            System.out.println("1. Create New Customer");
            System.out.println("2. Customer Login");
            System.out.println("3. Admin Dashboard");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

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

    private static void createNewCustomer(Scanner scanner) {
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();
        System.out.print("Enter customer ID: ");
        String customerID = scanner.nextLine();
        System.out.println("Enter customer email");
        String email = scanner.nextLine();
        System.out.println("Enter customer Phone number");
        String phoneNumber = scanner.nextLine();
        System.out.print("Set a PIN for security: ");
        String pin = scanner.nextLine();

        // Create customer and save to database via constructor
        BankCustomer customer = new BankCustomer(name, customerID, pin, email, phoneNumber);
        customers.add(customer); // Keep in memory for this session

        System.out.print("How many accounts do you want to create? ");
        int numAccounts = scanner.nextInt();
        scanner.nextLine();

        for (int i = 0; i < numAccounts; i++) {
            System.out.print("Enter account number for Account " + (i + 1) + ": ");
            String accountNumber = scanner.nextLine();
            System.out.print("Enter initial balance: ");
            double balance = scanner.nextDouble();
            scanner.nextLine();
            BankAccount account = new BankAccount(accountNumber, balance, pin);
            customer.addAccount(account);
        }
        System.out.println("Customer created successfully!");
    }

    private static void customerLogin(Scanner scanner) {
        System.out.print("Enter Customer ID: ");
        String customerID = scanner.nextLine();
        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine();

        // Load customer from database instead of in-memory list
        BankCustomer customer = loadCustomerFromDatabase(customerID);
        if (customer != null && customer.validatePin(pin)) {
            if (!customers.contains(customer)) customers.add(customer); // Add to session list
            performCustomerOperations(scanner, customer);
        } else {
            System.out.println("Invalid Customer ID or PIN.");
        }
    }

    private static void adminDashboard(Scanner scanner) {
        System.out.print("Enter Admin Password: ");
        String password = scanner.nextLine();
        if ("admin123".equals(password)) {
            AdminDashboard adminDashboard = new AdminDashboard(customers);
            adminDashboard.displayAdminMenu();
        } else {
            System.out.println("Invalid admin password.");
        }
    }

    private static BankCustomer loadCustomerFromDatabase(String customerID) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT name, hashed_pin, email, phone_number FROM customers WHERE customer_id = ?")) {
            stmt.setString(1, customerID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String hashedPin = rs.getString("hashed_pin");
                String email = rs.getString("email");
                String phoneNumber = rs.getString("phone_number");
                // Create a BankCustomer instance, overriding hashPin to use the stored hash
                BankCustomer customer = new BankCustomer(name, customerID, "", email, phoneNumber) {
                    @Override
                    protected String hashPin(String pin) {
                        return hashedPin; // Use stored hash instead of re-hashing
                    }
                };
                return customer;
            }
        } catch (SQLException e) {
            System.err.println("Error loading customer from database: " + e.getMessage());
        }
        return null;
    }

    private static void performCustomerOperations(Scanner scanner, BankCustomer customer) {
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
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice >= 1 && choice <= 5) {
                System.out.print("Enter your PIN: ");
                String enteredPin = scanner.nextLine();
                if (!customer.validatePin(enteredPin)) {
                    pinAttempts++;
                    System.out.println("Invalid PIN. Attempts remaining: " + (MAX_PIN_ATTEMPTS - pinAttempts));
                    if (pinAttempts >= MAX_PIN_ATTEMPTS) {
                        System.out.println("Maximum PIN attempts reached. Aborting...");
                        running = false;
                        break;
                    }
                    continue;
                } else {
                    pinAttempts = 0;
                }
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter account number: ");
                    String depositAccount = scanner.nextLine();
                    System.out.print("Enter amount to deposit: ");
                    double depositAmount = scanner.nextDouble();
                    scanner.nextLine();
                    for (BankAccount acc : customer.getAccounts()) {
                        if (acc.getAccountNumber().equals(depositAccount)) {
                            acc.deposit(depositAmount);
                        }
                    }
                    break;

                case 2:
                    System.out.print("Enter account number: ");
                    String withdrawAccount = scanner.nextLine();
                    System.out.print("Enter amount to withdraw: ");
                    double withdrawAmount = scanner.nextDouble();
                    scanner.nextLine();
                    for (BankAccount acc : customer.getAccounts()) {
                        if (acc.getAccountNumber().equals(withdrawAccount)) {
                            acc.withdraw(withdrawAmount);
                        }
                    }
                    break;

                case 3:
                    customer.displayCustomerInfo();
                    break;

                case 4:
                    customer.displayTransactionHistory();
                    break;

                case 5:
                    System.out.print("Enter source account number: ");
                    String fromAccount = scanner.nextLine();
                    System.out.print("Enter destination account number: ");
                    String toAccount = scanner.nextLine();
                    System.out.print("Enter amount to transfer: ");
                    double transferAmount = scanner.nextDouble();
                    scanner.nextLine();
                    customer.transfer(fromAccount, toAccount, transferAmount);
                    break;

                case 6:
                    running = false;
                    System.out.println("Exiting... Thank you!");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}