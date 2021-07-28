package com.account.accountservices.service;

import com.account.accountservices.model.Account;
import com.account.accountservices.repository.AccountRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.BDDMockito.*;

import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import static junit.framework.TestCase.*;

@RunWith(SpringRunner.class)
public class AccountServiceTest {
    @InjectMocks
    private AccountService accountService;

    @Before
    public void before() {
    }

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private RestTemplate restTemplate;
}
