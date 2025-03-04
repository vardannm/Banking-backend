package com.bank;

import java.util.List;
import java.util.Scanner;

public class AdminDashboard {
    private List<BankCustomer> customers;

    public AdminDashboard(List<BankCustomer> customers) {
        this.customers = customers;
    }

    public void displayAdminMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n=== Admin Dashboard ===");
            System.out.println("1. View All Customers");
            System.out.println("2. View Customer Accounts");
            System.out.println("3. Delete a Customer");
            System.out.println("4. View Customer Transaction History");
            System.out.println("5. Transfer Funds Between Customer Accounts");
            System.out.println("6. Exit Admin Dashboard");
            System.out.print("Enter choice: ");
            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    viewAllCustomers();
                    break;
                case 2:
                    System.out.print("Enter Customer ID: ");
                    String customerID = scanner.nextLine();
                    viewCustomerAccounts(customerID);
                    break;
                case 3:
                    System.out.print("Enter Customer ID to delete: ");
                    String deleteCustomerID = scanner.nextLine();
                    deleteCustomer(deleteCustomerID);
                    break;
                case 4:
                    System.out.print("Enter Customer ID: ");
                    String transCustomerID = scanner.nextLine();
                    viewCustomerTransactionHistory(transCustomerID);
                    break;
                case 5:
                    System.out.print("Enter Customer ID: ");
                    String transferCustomerID = scanner.nextLine();
                    performTransferForCustomer(transferCustomerID, scanner);
                    break;
                case 6:
                    running = false;
                    System.out.println("Exiting Admin Dashboard...");
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
        // Scanner closed by caller (e.g., Main.java)
    }

    private void viewAllCustomers() {
        if (customers.isEmpty()) {
            System.out.println("No customers found.");
        } else {
            System.out.println("\n=== All Customers ===");
            for (BankCustomer customer : customers) {
                System.out.println("Customer ID: " + customer.getCustomerID());
                System.out.println("Customer Name: " + customer.getName());
                System.out.println("Number of Accounts: " + customer.getAccounts().size());
                System.out.println("-------------------------");
            }
        }
    }

    private void viewCustomerAccounts(String customerID) {
        for (BankCustomer customer : customers) {
            if (customer.getCustomerID().equals(customerID)) {
                customer.displayCustomerInfo();
                return;
            }
        }
        System.out.println("Customer with ID " + customerID + " not found.");
    }

    private void deleteCustomer(String customerID) {
        for (BankCustomer customer : customers) {
            if (customer.getCustomerID().equals(customerID)) {
                customers.remove(customer);
                System.out.println("Customer with ID " + customerID + " deleted successfully.");
                return;
            }
        }
        System.out.println("Customer with ID " + customerID + " not found.");
    }

    private void viewCustomerTransactionHistory(String customerID) {
        for (BankCustomer customer : customers) {
            if (customer.getCustomerID().equals(customerID)) {
                customer.displayTransactionHistory();
                return;
            }
        }
        System.out.println("Customer with ID " + customerID + " not found.");
    }

    private void performTransferForCustomer(String customerID, Scanner scanner) {
        for (BankCustomer customer : customers) {
            if (customer.getCustomerID().equals(customerID)) {
                System.out.println("Customer found: " + customer.getName());
                System.out.print("Enter source account number: ");
                String fromAccount = scanner.nextLine();
                System.out.print("Enter destination account number: ");
                String toAccount = scanner.nextLine();
                System.out.print("Enter amount to transfer: ");
                double transferAmount;
                try {
                    transferAmount = scanner.nextDouble();
                    scanner.nextLine();
                } catch (Exception e) {
                    System.out.println("Invalid amount. Transfer aborted.");
                    scanner.nextLine();
                    return;
                }
                customer.transfer(fromAccount, toAccount, transferAmount);
                return;
            }
        }
        System.out.println("Customer with ID " + customerID + " not found.");
    }
}