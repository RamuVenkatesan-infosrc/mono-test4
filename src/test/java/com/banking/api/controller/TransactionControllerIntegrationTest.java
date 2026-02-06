Here's the JUnit 5 test class for the TransactionController:

```java
package com.banking.api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAccountTransactions() throws Exception {
        mockMvc.perform(get("/account/{accountId}", "123"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTransaction() throws Exception {
        mockMvc.perform(get("/{transactionId}", "456"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeposit() throws Exception {
        String depositJson = "{\"accountId\":\"123\",\"amount\":100.00}";
        mockMvc.perform(post("/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(depositJson))
                .andExpect(status().isOk());
    }

    @Test
    void testWithdraw() throws Exception {
        String withdrawJson = "{\"accountId\":\"123\",\"amount\":50.00}";
        mockMvc.perform(post("/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(withdrawJson))
                .andExpect(status().isOk());
    }

    @Test
    void testTransfer() throws Exception {
        String transferJson = "{\"fromAccountId\":\"123\",\"toAccountId\":\"456\",\"amount\":75.00}";
        mockMvc.perform(post("/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(transferJson))
                .andExpect(status().isOk());
    }
}
```
