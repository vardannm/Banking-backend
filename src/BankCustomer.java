import java.util.ArrayList;
import java.util.List;

public class BankCustomer {
    private String name;
    private String customerID;

    private String pin;
    private List<BankAccount> accounts;

    public BankCustomer(String name, String customerID , String pin) {
        this.name = name;
        this.customerID = customerID;
        this.pin = pin;
        this.accounts = new ArrayList<>();
    }
    public String getCustomerID(){
        return customerID;
    }
    public String getName(){
        return name;
    }

    public void addAccount(BankAccount account) {
        accounts.add(account);
        System.out.println("Account " + account.getAccountNumber() + " added for " + name);
    }
    public void displayCustomerInfo() {
        System.out.println("\nCustomer ID: " + customerID);
        System.out.println("Customer Name: " + name);
        System.out.println("Accounts:");
        for (BankAccount account : accounts) {
            account.displayBalance();
        }
    }
    public boolean validatePin(String enteredPin){
     return pin.equals(enteredPin);
    }
    public List<BankAccount> getAccounts() {
        return accounts;
    }
}
