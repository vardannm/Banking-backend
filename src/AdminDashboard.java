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
            System.out.println("4. Exit Admin Dashboard");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

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
                    running = false;
                    System.out.println("Exiting Admin Dashboard...");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
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
}