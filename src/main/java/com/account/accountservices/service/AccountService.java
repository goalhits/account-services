package com.account.accountservices.service;

import com.account.accountservices.constant.ErrorCode;
import com.account.accountservices.exception.AccountNotExistException;
import com.account.accountservices.exception.CheckBalanceException;
import com.account.accountservices.exception.OverDraftException;
import com.account.accountservices.exception.SystemException;
import com.account.accountservices.model.Account;
import com.account.accountservices.model.AccountTransferRequest;
import com.account.accountservices.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${endpoint.accountBalance}")
    private String retrieveAccountBalanceUrl;

    public Account retrieveBalances(Long accountId) {
        Account account = accountRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AccountNotExistException("Account with id:" + accountId + " does not exist.", ErrorCode.ACCOUNT_ERROR, HttpStatus.NOT_FOUND));

        return account;
    }

    @Transactional
    public void transferBalances(AccountTransferRequest transfer) throws OverDraftException, AccountNotExistException, SystemException {
        Account accountFrom = accountRepository.getAccountForUpdate(transfer.getAccountFromId())
                .orElseThrow(() -> new AccountNotExistException("Account with id:" + transfer.getAccountFromId() + " does not exist.", ErrorCode.ACCOUNT_ERROR));

        Account accountTo = accountRepository.getAccountForUpdate(transfer.getAccountToId())
                .orElseThrow(() -> new AccountNotExistException("Account with id:" + transfer.getAccountFromId() + " does not exist.", ErrorCode.ACCOUNT_ERROR));

        if(accountFrom.getBalance().compareTo(transfer.getAmount()) < 0) {
            throw new OverDraftException("Account with id:" + accountFrom.getAccountId() + " does not have enough balance to transfer.", ErrorCode.ACCOUNT_ERROR);
        }

        accountFrom.setBalance(accountFrom.getBalance().subtract(transfer.getAmount()));
        accountTo.setBalance(accountTo.getBalance().add(transfer.getAmount()));
    }

    public BigDecimal checkBalance(Long accountId) throws SystemException {

        try {
            String url = retrieveAccountBalanceUrl.replace("{id}", accountId.toString());
            log.info("checking balance from "+url);
            ResponseEntity<Account> balanceCheckResult = restTemplate.getForEntity(url, Account.class);
            if(balanceCheckResult.getStatusCode().is2xxSuccessful()) {
                if(balanceCheckResult.hasBody()) {
                    return balanceCheckResult.getBody().getBalance();
                }
            }
        } catch (ResourceAccessException ex) {
            final String errorMessage = "Encounter timeout error, please check with system administrator.";

            if(ex.getCause() instanceof SocketTimeoutException) {
                throw new CheckBalanceException(errorMessage, ErrorCode.TIMEOUT_ERROR);
            }
        }
        // for any other fail cases
        throw new SystemException("Encounter internal server error, please check with system administrator.", ErrorCode.SYSTEM_ERROR);
    }

    public void createAccount(Account account) {
        accountRepository.save(account);
    }
}
