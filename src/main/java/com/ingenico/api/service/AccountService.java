package com.ingenico.api.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ingenico.api.dao.AccountDao;
import com.ingenico.api.model.Account;

@Service
public class AccountService {
	@Autowired
	private AccountDao accountDao;

	public int createAccount(String accountName, BigDecimal balance, StringBuilder errorMessage) {
		Account account = new Account(accountName, balance);
		return accountDao.createAccount(account, errorMessage);
	}

	public List<Account> listAccounts(String accountName, String searchType, StringBuilder errorMessage) {
		if ("ALL".equalsIgnoreCase(searchType)) {
			List<Account> allAccounts = accountDao.listAccounts(errorMessage);
			if (allAccounts != null && !allAccounts.isEmpty()) {
				return allAccounts;
			} else {
				return new ArrayList<>();
			}
		} else {
			Account account = accountDao.listAccountByAccountName(accountName, errorMessage);
			if(account != null) {
				return new ArrayList<>(Arrays.asList(account));
			} else {
				return new ArrayList<>();
			}			
		}
	}

	public int transferMoney(String sourceAccount, String targetAccount, BigDecimal amount, StringBuilder message) {
		return accountDao.transferMoney(sourceAccount, targetAccount, amount, message);
	}
}
