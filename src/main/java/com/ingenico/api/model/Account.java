package com.ingenico.api.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class Account {
	private String accountName;
	private BigDecimal balance;

	public Account() {
	}

	public Account(String accountName, BigDecimal balance) {
		this.accountName = accountName;
		this.balance = balance;
	}

	@JsonProperty(required = true)
	@ApiModelProperty(notes = "Account Owner's Account Name", required = true)
	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	@JsonProperty(required = true)
	@ApiModelProperty(notes = "Account Balance", required = true)
	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}
}
