package com.account.accountservices.controller;

import com.account.accountservices.dto.TransferResult;
import com.account.accountservices.exception.AccountNotExistException;
import com.account.accountservices.exception.CheckBalanceException;
import com.account.accountservices.exception.OverDraftException;
import com.account.accountservices.model.Account;
import com.account.accountservices.model.AccountTransferRequest;
import com.account.accountservices.service.AccountService;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

@RestController
@RequestMapping("/v1/accounts")
@Api(tags = { "Accounts Controller" }, description = "Provide APIs for account related operation")
public class AccountController {

	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

	@Autowired
	private AccountService accountService;

	@PostMapping(path = "/transfer/from/{fromAccountId}/to/{toAccountId}/amount/{amount}")
	@ApiOperation(value = "API to create transaction", response = TransferResult.class, produces = "application/json")
	public ResponseEntity transferMoney(
			@PathVariable(value="fromAccountId") Long fromAccountId,
			@PathVariable(value="toAccountId") Long toAccountId,
			@PathVariable(value="amount") BigDecimal amount) throws Exception {
		try {
				AccountTransferRequest request = prepareAccountTransferRequest(fromAccountId, toAccountId, amount);
				accountService.transferBalances(request);
				TransferResult result = new TransferResult();
				result.setAccountFromId(request.getAccountFromId());
				result.setBalanceAfterTransfer(accountService.checkBalance(request.getAccountFromId()));
				return new ResponseEntity<>(result, HttpStatus.ACCEPTED);
		} catch (AccountNotExistException | OverDraftException e) {
			logger.error("Fail to transfer balances, please check with system administrator.");
			throw e;
		} catch (CheckBalanceException cbEx) {
			logger.error("Fail to check balances after transfer, please check with system administrator.");
			throw cbEx;
		}
	}

	@GetMapping("/{accountId}/balances")
	@ApiOperation(value = "Get account balance by id", response = Account.class, produces = "application/json")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid ID supplied"),
							@ApiResponse(code = 404, message = "Account not found with ID")})
	public Account getBalance(
			@ApiParam(value = "ID related to the account", required = true) @PathVariable Long accountId) {
		return accountService.retrieveBalances(accountId);
	}

	@PostMapping("/create")
	@ApiOperation(value = "Create new account", consumes = "application/json")
	public void createAccount(@Valid @RequestBody Account account) {
		accountService.createAccount(account);
	}

	private AccountTransferRequest prepareAccountTransferRequest(Long fromAccountId, Long toAccountId, BigDecimal amount) {
		AccountTransferRequest request = new AccountTransferRequest();
		request.setAccountFromId(fromAccountId);
		request.setAccountToId(toAccountId);
		request.setAmount(amount);
		return request;
	}
}
