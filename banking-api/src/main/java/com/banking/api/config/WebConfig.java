package com.banking.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }
}


package com.banking.api.controller;

import com.banking.api.dto.TransactionRequest;
import com.banking.api.dto.TransactionResponse;
import com.banking.core.domain.Money;
import com.banking.transaction.domain.Transaction;
import com.banking.transaction.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "${allowed.origins}", allowCredentials = "true", methods = {RequestMethod.GET, RequestMethod.POST})
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestBody TransactionRequest request, HttpServletRequest servletRequest) {
        if (!validateCsrfToken(servletRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Transaction transaction = transactionService.deposit(
            request.getAccountId(),
            new Money(request.getAmount(), request.getCurrency()),
            request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(transaction));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@RequestBody TransactionRequest request, HttpServletRequest servletRequest) {
        if (!validateCsrfToken(servletRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Transaction transaction = transactionService.withdraw(
            request.getAccountId(),
            new Money(request.getAmount(), request.getCurrency()),
            request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(transaction));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestBody TransactionRequest request, HttpServletRequest servletRequest) {
        if (!validateCsrfToken(servletRequest)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Transaction transaction = transactionService.transfer(
            request.getFromAccountId(),
            request.getToAccountId(),
            new Money(request.getAmount(), request.getCurrency()),
            request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(transaction));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByAccount(@PathVariable String accountId, HttpServletRequest servletRequest) {
        String csrfToken = generateCsrfToken(servletRequest);
        List<Transaction> transactions = transactionService.getTransactionsByAccount(accountId);
        List<TransactionResponse> responses = transactions.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok().header("X-CSRF-TOKEN", csrfToken).body(responses);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String transactionId, HttpServletRequest servletRequest) {
        String csrfToken = generateCsrfToken(servletRequest);
        Transaction transaction = transactionService.getTransaction(transactionId);
        return ResponseEntity.ok().header("X-CSRF-TOKEN", csrfToken).body(toResponse(transaction));
    }

    private TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setAccountId(transaction.getAccountId());
        response.setType(transaction.getType().name());
        response.setAmount(transaction.getAmount().getAmount().doubleValue());
        response.setCurrency(transaction.getAmount().getCurrency());
        response.setTimestamp(transaction.getTimestamp().toString());
        response.setDescription(transaction.getDescription());
        response.setRelatedAccountId(transaction.getRelatedAccountId());
        return response;
    }

    private boolean validateCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String sessionToken = (String) session.getAttribute("CSRF_TOKEN");
        String requestToken = request.getHeader("X-CSRF-TOKEN");
        return sessionToken != null && sessionToken.equals(requestToken);
    }

    private String generateCsrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String token = java.util.UUID.randomUUID().toString();
        session.setAttribute("CSRF_TOKEN", token);
        return token;
    }
}


package com.banking.api.controller;

import com.banking.account.domain.Account;
import com.banking.account.service.AccountService;
import com.banking.api.dto.AccountCreateRequest;
import com.banking.api.dto.AccountResponse;
import com.banking.core.domain.AccountType;
import com.banking.core.domain.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountCreateRequest request) {
        Account account = accountService.createAccount(
            request.getCustomerId(),
            AccountType.valueOf(request.getAccountType()),
            new Money(request.getInitialBalance(), request.getCurrency())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(account));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountId) {
        Account account = accountService.getAccount(accountId);
        return ResponseEntity.ok(toResponse(account));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomer(@PathVariable String customerId) {
        List<Account> accounts = accountService.getAccountsByCustomer(customerId);
        List<AccountResponse> responses = accounts.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<Account> accounts = accountService.getAllAccounts();
        List<AccountResponse> responses = accounts.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<Money> getBalance(@PathVariable String accountId) {
        Money balance = accountService.getBalance(accountId);
        return ResponseEntity.ok(balance);
    }

    private AccountResponse toResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setAccountId(account.getAccountId());
        response.setCustomerId(account.getCustomerId());
        response.setAccountType(account.getAccountType().name());
        response.setBalance(account.getBalance().getAmount().doubleValue());
        response.setCurrency(account.getBalance().getCurrency());
        response.setActive(account.isActive());
        return response;
    }
}