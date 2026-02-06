Here's the JUnit 5 test class for the AccountController:

```java
package com.banking.api.controller;

import com.banking.api.model.Account;
import com.banking.api.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
public class AccountControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetAccountById() throws Exception {
        Account account = new Account(1L, "123456", new BigDecimal("1000.00"));
        when(accountService.getAccount(1L)).thenReturn(account);

        mockMvc.perform(get("/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("123456"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    public void testGetAccountsByCustomerId() throws Exception {
        List<Account> accounts = Arrays.asList(
                new Account(1L, "123456", new BigDecimal("1000.00")),
                new Account(2L, "789012", new BigDecimal("2000.00"))
        );
        when(accountService.getAccountsByCustomerId(1L)).thenReturn(accounts);

        mockMvc.perform(get("/customer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    public void testGetAccountBalance() throws Exception {
        when(accountService.getAccountBalance(1L)).thenReturn(new BigDecimal("1000.00"));

        mockMvc.perform(get("/1/balance"))
                .andExpect(status().isOk())
                .andExpect(content().string("1000.00"));
    }

    @Test
    public void testCreateAccount() throws Exception {
        Account newAccount = new Account(null, "123456", new BigDecimal("1000.00"));
        Account createdAccount = new Account(1L, "123456", new BigDecimal("1000.00"));
        when(accountService.createAccount(any(Account.class))).thenReturn(createdAccount);

        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newAccount)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("123456"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }
}
```
