package com.ingenico.api.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import com.ingenico.api.model.Account;

@Repository
public class AccountDao {
	private static Logger logger = LogManager.getLogger(AccountDao.class);
	private static final String SQL_CREATE_ACCOUNT = "INSERT INTO ACCOUNT (ID, ACCOUNT_NAME, BALANCE) VALUES (?, ?, ?)";
	private static final String SQL_LIST_ACCOUNTS_ALL = "SELECT * FROM ACCOUNT";
	private static final String SQL_LIST_ACCOUNT_BY_ACCOUNTNAME = "SELECT * FROM ACCOUNT WHERE ACCOUNT_NAME = ? ";
	private static final String SQL_LOCK_ACCOUNT_BY_NAME = "SELECT * FROM ACCOUNT WHERE ACCOUNT_NAME = ? FOR UPDATE";
	private static final String SQL_UPDATE_ACCOUNT_BALANCE = "UPDATE ACCOUNT SET BALANCE = ? WHERE ACCOUNT_NAME = ? ";
	@Autowired
	BaseDao baseDao;

	// IMPORTANT NOTE:
	// I added the following constructor for unit tests. 
	AccountDao(BaseDao baseDao) {
		this.baseDao = baseDao;
	}

	public int createAccount(Account account, StringBuilder message) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = baseDao.getConnection();
			ps = connection.prepareStatement(SQL_CREATE_ACCOUNT);
			ps.setString(1, UUID.randomUUID().toString());
			ps.setString(2, account.getAccountName());
			ps.setBigDecimal(3, account.getBalance());
			return ps.executeUpdate();
		} catch (Exception e) {
			logger.error("Exception in AccountDao-createAccount: " + e);
			message.append(e.toString());
		} finally {
			baseDao.closeStatement(ps);
			baseDao.closeConnection(connection);
		}
		return 0;
	}

	public List<Account> listAccounts(StringBuilder message) {
		try {
			return baseDao.getJdbcTemplate().query(SQL_LIST_ACCOUNTS_ALL,
					new BeanPropertyRowMapper<Account>(Account.class));
		} catch (Exception e) {
			logger.error("Exception in AccountDao-listAccounts: " + e);
			message.append(e.toString());
		}
		return null;
	}

	public Account listAccountByAccountName(String accountName, StringBuilder message) {
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = baseDao.getConnection();
			ps = connection.prepareStatement(SQL_LIST_ACCOUNT_BY_ACCOUNTNAME);
			return fetchAccount(accountName, ps);
		} catch (Exception e) {
			logger.error("Exception in AccountDao-listAccountByAccountName: " + e);
			message.append(e.toString());
		} finally {
			baseDao.closeStatement(ps);
			baseDao.closeConnection(connection);
		}
		return null;
	}

	public int transferMoney(String sourceAccount, String targetAccount, BigDecimal amount, StringBuilder message) {
		int numberOfUpdatedRows = 0;
		Connection connection = null;
		PreparedStatement lockAndFetchStatement = null;
		PreparedStatement updateStatement = null;
		try {
			if (amount.compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException("Transfer Amount can not be negative!");
			}
			connection = baseDao.getConnection();
			connection.setAutoCommit(false);
			lockAndFetchStatement = connection.prepareStatement(SQL_LOCK_ACCOUNT_BY_NAME);
			Account fetchedSourceAccount = fetchAccount(sourceAccount, lockAndFetchStatement);
			Account fetchedTargetAccount = fetchAccount(targetAccount, lockAndFetchStatement);
			if (fetchedSourceAccount == null || fetchedTargetAccount == null) {
				throw new IllegalArgumentException("Invalid Accounts!");
			}
			BigDecimal fetchedSourceAccountBalance = fetchedSourceAccount.getBalance();
			BigDecimal sourceAccountRemainingBalance = fetchedSourceAccountBalance.subtract(amount);
			if (sourceAccountRemainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
				throw new IllegalArgumentException("Overdrawn is not possible!");
			}
			numberOfUpdatedRows = processTransfer(amount, connection, fetchedSourceAccount, fetchedTargetAccount,
					sourceAccountRemainingBalance);
			connection.commit();
			return numberOfUpdatedRows;
		} catch (Exception e) {
			logger.error("Exception in AccountDao-transferMoney: " + e);
			message.append(e.toString());
			if (connection != null) {
				try {
					connection.rollback();
				} catch (Exception exc) {
					message.append(exc.toString());
					return 0;
				}
			}
		} finally {
			baseDao.closeStatement(updateStatement);
			baseDao.closeConnection(connection);
		}
		return numberOfUpdatedRows;
	}

	Account fetchAccount(String account, PreparedStatement fetchStatement) throws SQLException {
		fetchStatement.setString(1, account);
		ResultSet resultSet = fetchStatement.executeQuery();
		if (resultSet.next()) {
			return new Account(resultSet.getString("ACCOUNT_NAME"), resultSet.getBigDecimal("BALANCE"));
		}
		return null;
	}

	// IMPORTANT NOTE:
	// I didn't use batch update because of the following Oracle issue:
	// https://stackoverflow.com/questions/19022175/executebatch-method-return-array-of-value-2-in-java
	int processTransfer(BigDecimal amount, Connection connection, Account fetchedSourceAccount,
			Account fetchedTargetAccount, BigDecimal sourceAccountRemainingBalance) throws SQLException {
		PreparedStatement updateStatement;
		int numberOfUpdatedRows = 0;
		BigDecimal fetchedTargetAccountBalance = fetchedTargetAccount.getBalance();
		BigDecimal targetAccountRemainingBalance = fetchedTargetAccountBalance.add(amount);
		updateStatement = connection.prepareStatement(SQL_UPDATE_ACCOUNT_BALANCE);
		updateStatement.setBigDecimal(1, sourceAccountRemainingBalance);
		updateStatement.setString(2, fetchedSourceAccount.getAccountName());
		numberOfUpdatedRows += updateStatement.executeUpdate();
		updateStatement.setBigDecimal(1, targetAccountRemainingBalance);
		updateStatement.setString(2, fetchedTargetAccount.getAccountName());
		numberOfUpdatedRows += updateStatement.executeUpdate();
		return numberOfUpdatedRows;
	}
}
