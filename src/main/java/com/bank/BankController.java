package com.bank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
            double balance = customer.getAccounts().isEmpty() ? 0.0 : customer.getAccounts().get(0).getBalance();
            return ResponseEntity.ok(new CustomerResponse(customer.getName(), balance));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @GetMapping("/customer")
    public ResponseEntity<CustomerResponse> getCustomer(@RequestParam String customerID) {
        BankCustomer customer = bankService.loadCustomer(customerID);
        if (customer != null) {
            double balance = customer.getAccounts().isEmpty() ? 0.0 : customer.getAccounts().get(0).getBalance();
            return ResponseEntity.ok(new CustomerResponse(customer.getName(), balance));
        }
        return ResponseEntity.status(404).body(null);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<BankAccount.Transaction>> getTransactions(@RequestParam String customerID) {
        BankCustomer customer = bankService.loadCustomer(customerID);
        if (customer != null) {
            return ResponseEntity.ok(customer.getAccounts().isEmpty() ? new ArrayList<>() : customer.getAccounts().get(0).getTransactionHistory());
        }
        return ResponseEntity.status(404).body(null);
    }

    @PostMapping("/deposit")
    public ResponseEntity<String> deposit(@RequestBody DepositRequest request) {
        BankCustomer customer = bankService.loadCustomer(request.customerID());
        if (customer != null) {
            for (BankAccount acc : customer.getAccounts()) {
                if (acc.getAccountNumber().equals(request.accountNumber())) {
                    acc.deposit(request.amount());
                    bankService.updateBalance(request.accountNumber(), acc.getBalance());
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
}

record LoginRequest(String customerID, String pin) {}
record CustomerResponse(String name, double balance) {}
record DepositRequest(String customerID, String accountNumber, double amount) {}
record WithdrawRequest(String customerID, String accountNumber, double amount) {}
record AddAccountRequest(String customerID, String accountNumber, double initialBalance, String pin) {}