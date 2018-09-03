package com.ingenico.api.controller;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ingenico.api.model.Account;
import com.ingenico.api.model.ApiJsonResponse;
import com.ingenico.api.service.AccountService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
public class AccountController {
	@Autowired
	private AccountService accountService;
	private static Logger logger = LogManager.getLogger(AccountController.class);

	@ApiOperation(value = "Create monetary accounts with initial balance", nickname = "createAccount")
	@RequestMapping(method = RequestMethod.POST, path = "/api/account/create", produces = "application/json")
	@ApiImplicitParams({ //
			@ApiImplicitParam(name = "accountName", value = "Account Owner's Account Name", required = true, dataType = "string", paramType = "query"), //
			@ApiImplicitParam(name = "balance", value = "Account Balance, Sample value: 100.5", required = true, dataType = "number", paramType = "query") //
	})
	@ApiResponses(value = { //
			@ApiResponse(code = 200, message = "Success", response = ApiJsonResponse.class), //
			@ApiResponse(code = 400, message = "Bad Request"), //
			@ApiResponse(code = 500, message = "Failure, check complete request") //
	})
	public ApiJsonResponse createAccount(HttpServletRequest request, HttpServletResponse response, //
			@RequestParam(value = "accountName", required = true) String accountName,
			@RequestParam(value = "balance", required = true) String balance) {
		ApiJsonResponse apiJsonResponse = new ApiJsonResponse();
		StringBuilder errorMessage = new StringBuilder();
		try {
			BigDecimal balanceAmount = new BigDecimal(balance);
			if (balanceAmount.compareTo(BigDecimal.ZERO) < 0) {
				apiJsonResponse.setError("Balance can not be negative!");
				apiJsonResponse.setSuccess(false);
			}
			if (0 == accountService.createAccount(accountName, balanceAmount, errorMessage)) {
				apiJsonResponse.setError(errorMessage.toString());
				apiJsonResponse.setSuccess(false);
			} else {
				apiJsonResponse.setSuccess(true);
				apiJsonResponse
						.setData(accountName + " account has been successfully created with balance: " + balance);
			}
		} catch (Exception e) {
			logger.error("An exception is occured in createAccount: " + e);
			apiJsonResponse.setError(e.toString());
			apiJsonResponse.setSuccess(false);
		}
		return apiJsonResponse;
	}

	@ApiOperation(value = "List monetary accounts", nickname = "listAccounts")
	@RequestMapping(method = RequestMethod.GET, path = "/api/account/list", produces = "application/json")
	@ApiImplicitParams({ //
			@ApiImplicitParam(name = "accountName", value = "Account Owner's Account Name", required = false, dataType = "string", paramType = "query"), //
			@ApiImplicitParam(name = "searchType", value = "List whether all accounts or a specific account. Allowable Values: ALL,BY_ACCOUNT_NAME", required = true, dataType = "string", paramType = "query", defaultValue = "ALL", allowableValues = "ALL,BY_ACCOUNT_NAME") //
	})
	@ApiResponses(value = { //
			@ApiResponse(code = 200, message = "Success", response = ApiJsonResponse.class), //
			@ApiResponse(code = 400, message = "Bad Request"), //
			@ApiResponse(code = 500, message = "Failure, check complete request") //
	})
	public ApiJsonResponse listAccounts(HttpServletRequest request, HttpServletResponse response, //
			@RequestParam(value = "accountName", required = false) String accountName,
			@RequestParam(value = "searchType", required = true) String searchType) {
		ApiJsonResponse apiJsonResponse = new ApiJsonResponse();
		StringBuilder errorMessage = new StringBuilder();
		try {
			List<Account> accounts = accountService.listAccounts(accountName, searchType, errorMessage);
			if (!accounts.isEmpty()) {
				apiJsonResponse.setSuccess(true);
			} else {
				apiJsonResponse.setError(errorMessage.toString());
				apiJsonResponse.setSuccess(false);
			}
			apiJsonResponse.setData(accounts);
		} catch (Exception e) {
			logger.error("An exception is occured in listAccounts: " + e);
			apiJsonResponse.setError(e.toString());
			apiJsonResponse.setSuccess(false);
		}
		return apiJsonResponse;
	}

	@ApiOperation(value = "Transfer Money Between Accounts", nickname = "transferMoney")
	@RequestMapping(method = RequestMethod.POST, path = "/api/account/money/transfer", produces = "application/json")
	@ApiImplicitParams({ //
			@ApiImplicitParam(name = "sourceAccount", value = "Source Account Name", required = true, dataType = "string", paramType = "query"), //
			@ApiImplicitParam(name = "targetAccount", value = "Target Account Name", required = true, dataType = "string", paramType = "query"), //
			@ApiImplicitParam(name = "amount", value = "Transaction Amount, Sample value: 100.5", required = true, dataType = "number", paramType = "query") //
	})
	@ApiResponses(value = { //
			@ApiResponse(code = 200, message = "Success", response = ApiJsonResponse.class), //
			@ApiResponse(code = 400, message = "Bad Request"), //
			@ApiResponse(code = 500, message = "Failure, check complete request") //
	})
	public ApiJsonResponse transferMoney(HttpServletRequest request, HttpServletResponse response, //
			@RequestParam(value = "sourceAccount", required = true) String sourceAccount,
			@RequestParam(value = "targetAccount", required = true) String targetAccount,
			@RequestParam(value = "amount", required = true) String amount) {
		ApiJsonResponse apiJsonResponse = new ApiJsonResponse();
		StringBuilder errorMessage = new StringBuilder();
		try {
			BigDecimal transferAmount = new BigDecimal(amount);
			if (transferAmount.compareTo(BigDecimal.ZERO) < 0) {
				apiJsonResponse.setError("Transfer amount can not be negative!");
				apiJsonResponse.setSuccess(false);
			} else {
				if (2 != accountService.transferMoney(sourceAccount, targetAccount, transferAmount, errorMessage)) {
					apiJsonResponse.setError(errorMessage.toString());
					apiJsonResponse.setSuccess(false);
				} else {
					apiJsonResponse.setSuccess(true);
					apiJsonResponse.setData("Money transfer has been succeded from " + sourceAccount + " to "
							+ targetAccount + " with amount " + amount);
				}
			}
		} catch (Exception e) {
			logger.error("An exception is occured in transferMoney: " + e);
			apiJsonResponse.setError(e.toString());
			apiJsonResponse.setSuccess(false);
		}
		return apiJsonResponse;
	}
}
