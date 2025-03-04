package com.bank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class BankController {
    private final BankService bankService;

    @Autowired
    public BankController(BankService bankService) {
        this.bankService = bankService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        BankCustomer customer = bankService.login(request.customerID(), request.pin());
        if (customer != null) {
            List<AccountSummary> accounts = customer.getAccounts().stream()
                    .map(AccountSummary::new)
                    .toList();
            return ResponseEntity.ok(new CustomerResponse(customer.getName(), accounts));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @GetMapping("/customer")
    public ResponseEntity<CustomerResponse> getCustomer(@RequestParam String customerID) {
        BankCustomer customer = bankService.loadCustomer(customerID);
        if (customer != null) {
            List<AccountSummary> accounts = customer.getAccounts().stream()
                    .map(AccountSummary::new)
                    .toList();
            return ResponseEntity.ok(new CustomerResponse(customer.getName(), accounts));
        }
        return ResponseEntity.status(404).body(null);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<BankAccount.Transaction>> getTransactions(@RequestParam String customerID) {
        List<BankAccount.Transaction> transactions = bankService.getTransactions(customerID);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody DepositRequest request) {
        BankCustomer customer = bankService.loadCustomer(request.customerID());
        if (customer != null) {
            for (BankAccount acc : customer.getAccounts()) {
                if (acc.getAccountNumber().equals(request.accountNumber())) {
                    acc.deposit(request.amount());
                    bankService.updateBalance(request.accountNumber(), acc.getBalance());
                    bankService.logTransaction(request.accountNumber(), "DEPOSIT", request.amount());
                    return ResponseEntity.ok("Deposit successful");
                }
            }
            return ResponseEntity.status(404).body("Account not found");
        }
        return ResponseEntity.status(404).body("Customer not found");
    }

    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(@RequestBody WithdrawRequest request) {
        BankCustomer customer = bankService.loadCustomer(request.customerID());
        if (customer != null) {
            for (BankAccount acc : customer.getAccounts()) {
                if (acc.getAccountNumber().equals(request.accountNumber())) {
                    acc.withdraw(request.amount());
                    bankService.updateBalance(request.accountNumber(), acc.getBalance());
                    bankService.logTransaction(request.accountNumber(), "WITHDRAWAL", request.amount());
                    return ResponseEntity.ok("Withdrawal successful");
                }
            }
            return ResponseEntity.status(404).body("Account not found");
        }
        return ResponseEntity.status(404).body("Customer not found");
    }

    @PostMapping("/add-account")
    public ResponseEntity<String> addAccount(@RequestBody AddAccountRequest request) {
        BankCustomer customer = bankService.loadCustomer(request.customerID());
        if (customer != null) {
            BankAccount account = new BankAccount(request.accountNumber(), request.initialBalance(), request.pin());
            customer.addAccount(account);
            bankService.addAccount(request.customerID(), request.accountNumber(), request.initialBalance(), request.pin());
            return ResponseEntity.ok("Account added successfully");
        }
        return ResponseEntity.status(404).body("Customer not found");
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@RequestBody TransferRequest request) {
        try {
            bankService.transfer(request.customerID(), request.fromAccountNumber(), request.toAccountNumber(), request.amount());
            return ResponseEntity.ok("Transfer successful");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Transfer failed: " + e.getMessage());
        }
    }
}

record LoginRequest(String customerID, String pin) {}
record CustomerResponse(String name, List<AccountSummary> accounts) {}
record AccountSummary(String accountNumber, double balance) {
    public AccountSummary(BankAccount account) {
        this(account.getAccountNumber(), account.getBalance());
    }
}
record DepositRequest(String customerID, String accountNumber, double amount) {}
record WithdrawRequest(String customerID, String accountNumber, double amount) {}
record AddAccountRequest(String customerID, String accountNumber, double initialBalance, String pin) {}
record TransferRequest(String customerID, String fromAccountNumber, String toAccountNumber, double amount) {}