package com.account.accountservices.controller;

import com.account.accountservices.model.Account;
import com.account.accountservices.model.AccountTransferRequest;
import com.account.accountservices.service.AccountService;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.runner.RunWith;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;

@RunWith(SpringRunner.class)
@WebMvcTest(AccountController.class)
public class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    public void shouldTransferBalanceFromAccount1ToAccount2() throws Exception {
        AccountTransferRequest request  = prepareAccountTransferRequest(1L, 2L, BigDecimal.valueOf(10.0));
        this.mockMvc.perform(
                post("/v1/accounts/transfer/from/1/to/2/amount/10.5")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(convertObjectToJsonBytes(request))
        ).andExpect(status().isAccepted());
    }

    @Test
    public void shouldGetTheBalanceForAnAccount() throws Exception {
        when(accountService.retrieveBalances(any())).thenReturn(new Account(1L, BigDecimal.valueOf(1.2)));
        this.mockMvc.perform(get("/v1/accounts/1/balances"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("accountId").exists());
    }

    @Test
    public void shouldCreateAccountSuccessfully() throws Exception {
        Account account = new Account(1L, BigDecimal.valueOf(1.2));
        this.mockMvc.perform(
                post("/v1/accounts/create")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(convertObjectToJsonBytes(account))
        ).andExpect(status().isOk());
    }

    public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8")
    );
    public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(object);
    }

    private AccountTransferRequest prepareAccountTransferRequest(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        AccountTransferRequest request = new AccountTransferRequest();
        request.setAccountFromId(fromAccountId);
        request.setAccountToId(toAccountId);
        request.setAmount(amount);
        return request;
    }
}
