import java.util.ArrayList;
import java.util.List;

public class BankCustomer {
    private String name;
    private String customerID;

    private String pin;
    private String phoneNumber;
    private String email;
    private List<BankAccount> accounts;

    public BankCustomer(String name, String customerID,String pin,String email,String phoneNumber) {
        this.name = name;
        this.customerID = customerID;
        this.pin = pin;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.accounts = new ArrayList<>();
    }
    public String getCustomerID(){
        return customerID;
    }
    public String getName(){
        return name;
    }
    public String getPhoneNumber(){
        return phoneNumber;
    }
    public String getEmail(){
        return email;
    }

    public void addAccount(BankAccount account) {
        accounts.add(account);
        System.out.println("Account " + account.getAccountNumber() + " added for " + name + "Email" + email + "Phone number" + phoneNumber);
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
    public boolean validatePin(String enteredPin){
     return pin.equals(enteredPin);
    }
    public List<BankAccount> getAccounts() {
        return accounts;
    }
}
