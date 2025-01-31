import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static List<BankCustomer> customers = new ArrayList<>();
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

        BankCustomer customer = new BankCustomer(name, customerID,pin,email,phoneNumber);


        System.out.print("How many accounts do you want to create? ");
        int numAccounts = scanner.nextInt();
        scanner.nextLine();

        for (int i = 0; i < numAccounts; i++) {
            System.out.print("Enter account number for Account " + (i + 1) + ": ");
            String accountNumber = scanner.nextLine();
            System.out.print("Enter initial balance: ");
            double balance = scanner.nextDouble();
            scanner.nextLine();
            BankAccount account = new BankAccount(accountNumber, balance,pin);
            customer.addAccount(account);
        }
        customers.add(customer);
        System.out.println("Customer created successfully!");
    }
    private static void customerLogin(Scanner scanner) {
        System.out.print("Enter Customer ID: ");
        String customerID = scanner.nextLine();
        System.out.print("Enter PIN: ");
        String pin = scanner.nextLine();
        BankCustomer customer = findCustomer(customerID);
        if (customer != null && customer.validatePin(pin)) {
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

    private static BankCustomer findCustomer(String customerID) {
        for (BankCustomer customer : customers) {
            if (customer.getCustomerID().equals(customerID)) {
                return customer;
            }
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
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice == 1 || choice == 2 || choice == 3) {
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

                    // Find account and withdraw
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
                    running = false;
                    System.out.println("Exiting... Thank you!");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}