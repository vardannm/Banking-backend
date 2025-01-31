import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class BankCustomer {
    private String name;
    private String customerID;
    private String hashedPin;
    private String pin;
    private String phoneNumber;
    private String email;
    private List<BankAccount> accounts;

    public BankCustomer(String name, String customerID,String pin,String email,String phoneNumber) {
        this.name = name;
        this.customerID = customerID;
        this.hashedPin = hashPin(pin);
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.accounts = new ArrayList<>();
    }
    private String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(pin.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing PIN", e);
        }
    }

    public boolean validatePin(String enteredPin) {
        return hashedPin.equals(hashPin(enteredPin));  // Compare entered PIN (hashed) with stored hashed PIN
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
    public List<BankAccount> getAccounts() {
        return accounts;
    }
}
