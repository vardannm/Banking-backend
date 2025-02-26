import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BankAccount {
    private String accountNumber;
    private double balance;
    private List<Transaction> transactionHistory;

    // Inner class to represent a transaction
    private static class Transaction {
        String type; // "Deposit", "Withdrawal", or "Transfer"
        double amount;
        String dateTime;

        Transaction(String type, double amount) {
            this.type = type;
            this.amount = amount;
            this.dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        @Override
        public String toString() {
            return dateTime + " | " + type + " | $" + amount;
        }
    }

    public BankAccount(String accountNumber, double balance, String pin) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.transactionHistory = new ArrayList<>();
    }

    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            transactionHistory.add(new Transaction("Deposit", amount));
            System.out.println("Deposited $" + amount);
        } else {
            System.out.println("Deposit must be more than 0");
        }
    }

    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            transactionHistory.add(new Transaction("Withdrawal", amount));
            System.out.println("Withdrawn $" + amount);
        } else {
            System.out.println("Insufficient balance");
        }
    }

    public void displayBalance() {
        System.out.println("Account: " + accountNumber + " | Balance: $" + balance);
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void displayTransactionHistory() {
        if (transactionHistory.isEmpty()) {
            System.out.println("No transactions for Account " + accountNumber);
        } else {
            System.out.println("\nTransaction History for Account " + accountNumber + ":");
            for (Transaction t : transactionHistory) {
                System.out.println(t);
            }
        }
    }

    // New method to access balance for transfer functionality
    public double getBalance() {
        return balance;
    }
}