import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();
        System.out.print("Enter customer ID: ");
        String customerID = scanner.nextLine();
        System.out.println("Enter Pin");
        String pin = scanner.nextLine();
        BankCustomer customer = new BankCustomer(name, customerID,pin);
        System.out.print("How many accounts do you want to create? ");
        int numAccounts = scanner.nextInt();
        scanner.nextLine();

        for (int i = 0; i < numAccounts; i++) {
            System.out.print("Enter account number for Account " + (i + 1) + ": ");
            String accountNumber = scanner.nextLine();
            System.out.print("Enter initial balance: ");
            double balance = scanner.nextDouble();
            scanner.nextLine(); // Consume newline

            // Create and add account
            BankAccount account = new BankAccount(accountNumber, balance ,pin);
            customer.addAccount(account);
        }

        // Perform transactions
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
            scanner.nextLine(); // Consume newline

            if(choice == 1 || choice == 3 || choice == 2 ) {
                System.out.println("Enter the pin");
                String enteredPin = scanner.nextLine();
                if (!customer.validatePin(enteredPin)) {
                    pinAttempts++;
                    System.out.println("Invalid PIN. Access denied." + (MAX_PIN_ATTEMPTS-pinAttempts));
                    if (pinAttempts >= MAX_PIN_ATTEMPTS) {
                        System.out.println("Maximum PIN attempts reached. Aborting...");
                        running = false; // Terminate the program
                        break;
                    }
                    continue; // Skip the rest of the loop
                } else {
                    pinAttempts = 0; // Reset counter on successful PIN entry
                }
            }
            switch (choice) {
                case 1:
                    System.out.print("Enter account number: ");
                    String depositAccount = scanner.nextLine();
                    System.out.print("Enter amount to deposit: ");
                    double depositAmount = scanner.nextDouble();
                    scanner.nextLine();

                    // Find account and deposit
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

        scanner.close();
    }
}
