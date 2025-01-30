public class BankAccount {
    private String accountNumber;
    private double balance;
    private String pin;

    public BankAccount(String accountNumber , double balance , String pin){
        this.accountNumber = accountNumber ;
        this.balance = balance;
    }
    public void deposit(double amount){
        if(amount>0){
            balance += amount;
            System.out.println("Deposited" + amount);
        }else{
            System.out.println("Deposit must be more then 0");;
        }
    };
    public String getAccountNumber() {
        return accountNumber;
    }
    public void withdraw(double amount){
        if(amount>0 && amount<=balance){
            balance -= amount;
            System.out.println("Withdrawn" + amount);
        }else{
            System.out.println("Insufficient balance");
        }
    }
    public void displayBalance() {
        System.out.println("Account: " + accountNumber + " | Balance: $" + balance);
    }
}
