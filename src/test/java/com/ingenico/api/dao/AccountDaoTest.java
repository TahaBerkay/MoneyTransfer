package com.ingenico.api.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import com.ibatis.common.jdbc.ScriptRunner;
import com.ingenico.api.model.Account;

@RunWith(MockitoJUnitRunner.class)
public final class AccountDaoTest {
	private static final String driverClassName = "oracle.jdbc.driver.OracleDriver";
	private static final String connectionUrl = "jdbc:oracle:thin:@localhost:1521/XE";
	private static final String connectionUsername = "INGENICO";
	private static final String connectionPassword = "INGENICO";
	private static final String BEFORE_SQL_PATH = "src/test/resources/BeforeTestScripts.sql";
	private static final String AFTER_SQL_PATH = "src/test/resources/AfterTestScripts.sql";
	private static Logger logger = LogManager.getLogger(AccountDaoTest.class);
	AccountDao accountDao = new AccountDao(new BaseDao());

	@BeforeClass
	public static void beforeClass() {
		Connection connection = null;
		try {
			Class.forName(driverClassName);
			connection = DriverManager.getConnection(connectionUrl, connectionUsername, connectionPassword);
			ScriptRunner sr = new ScriptRunner(connection, false, false);
			Reader reader = new BufferedReader(new FileReader(BEFORE_SQL_PATH));
			sr.runScript(reader);
		} catch (Exception e) {
			logger.error("Exception in AccountDaoTest-beforeClass:" + e);
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException exp) {
					logger.error("Exception in AccountDaoTest-beforeClass:" + exp);
				}
			}
		}
	}

	@AfterClass
	public static void afterClass() {
		Connection connection = null;
		try {
			Class.forName(driverClassName);
			connection = DriverManager.getConnection(connectionUrl, connectionUsername, connectionPassword);
			ScriptRunner sr = new ScriptRunner(connection, false, false);
			Reader reader = new BufferedReader(new FileReader(AFTER_SQL_PATH));
			sr.runScript(reader);
		} catch (Exception e) {
			logger.error("Exception in AccountDaoTest-afterClass:" + e);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error("Exception in AccountDaoTest-afterClass:" + e);
				}
			}
		}
	}

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testTransferMoney_Basic() {
		StringBuilder errorMessage = new StringBuilder();
		BigDecimal transferAmount = BigDecimal.valueOf(10);
		int updateCount = accountDao.transferMoney("test_1", "test_2", transferAmount, errorMessage);
		Account sourceAccount = accountDao.listAccountByAccountName("test_1", errorMessage);
		Account targetAccount = accountDao.listAccountByAccountName("test_2", errorMessage);
		assertEquals(2, updateCount);
		assertTrue(sourceAccount.getBalance().equals(BigDecimal.valueOf(90)));
		assertTrue(targetAccount.getBalance().equals(BigDecimal.valueOf(110)));
	}

	@Test
	public void testTransferMoney_BasicDecimal() {
		StringBuilder errorMessage = new StringBuilder();
		BigDecimal transferAmount = BigDecimal.valueOf(26.57);
		int updateCount = accountDao.transferMoney("test_5", "test_6", transferAmount, errorMessage);
		Account sourceAccount = accountDao.listAccountByAccountName("test_5", errorMessage);
		Account targetAccount = accountDao.listAccountByAccountName("test_6", errorMessage);
		assertEquals(2, updateCount);
		assertTrue(sourceAccount.getBalance().equals(BigDecimal.valueOf(73.43)));
		assertTrue(targetAccount.getBalance().equals(BigDecimal.valueOf(126.57)));
	}

	@Test
	public void testTransferMoney_OverDrawn() {
		StringBuilder errorMessage = new StringBuilder();
		BigDecimal transferAmount = BigDecimal.valueOf(110);
		int updateCount = accountDao.transferMoney("test_3", "test_4", transferAmount, errorMessage);
		Account sourceAccount = accountDao.listAccountByAccountName("test_3", errorMessage);
		Account targetAccount = accountDao.listAccountByAccountName("test_4", errorMessage);
		assertEquals(0, updateCount);
		assertTrue(sourceAccount.getBalance().equals(BigDecimal.valueOf(100)));
		assertTrue(targetAccount.getBalance().equals(BigDecimal.valueOf(100)));
	}

	@Test
	public void testTransferMoney_NegativeTransferAmount() {
		StringBuilder errorMessage = new StringBuilder();
		BigDecimal transferAmount = BigDecimal.valueOf(-1);
		int updateCount = accountDao.transferMoney("test_3", "test_4", transferAmount, errorMessage);
		Account sourceAccount = accountDao.listAccountByAccountName("test_3", errorMessage);
		Account targetAccount = accountDao.listAccountByAccountName("test_4", errorMessage);
		assertEquals(0, updateCount);
		assertTrue(sourceAccount.getBalance().equals(BigDecimal.valueOf(100)));
		assertTrue(targetAccount.getBalance().equals(BigDecimal.valueOf(100)));
	}

	@Test
	public void testTransferMoney_MissingSourceAccount() {
		StringBuilder errorMessage = new StringBuilder();
		BigDecimal transferAmount = BigDecimal.valueOf(10);
		int updateCount = accountDao.transferMoney("test_3333", "test_4", transferAmount, errorMessage);
		Account sourceAccount = accountDao.listAccountByAccountName("test_3333", errorMessage);
		Account targetAccount = accountDao.listAccountByAccountName("test_4", errorMessage);
		assertEquals(0, updateCount);
		assertNull(sourceAccount);
		assertTrue(targetAccount.getBalance().equals(BigDecimal.valueOf(100)));
	}

	@Test
	public void testTransferMoney_MissingTargetAccount() {
		StringBuilder errorMessage = new StringBuilder();
		BigDecimal transferAmount = BigDecimal.valueOf(10);
		int updateCount = accountDao.transferMoney("test_4", "test_3333", transferAmount, errorMessage);
		Account sourceAccount = accountDao.listAccountByAccountName("test_4", errorMessage);
		Account targetAccount = accountDao.listAccountByAccountName("test_3333", errorMessage);
		assertEquals(0, updateCount);
		assertNull(targetAccount);
		assertTrue(sourceAccount.getBalance().equals(BigDecimal.valueOf(100)));
	}

	@Test
	public void testTransferMoney_SequentialTransfer() {
		StringBuilder errorMessage = new StringBuilder();
		BigDecimal transferAmount = BigDecimal.valueOf(10);
		int updateCount = accountDao.transferMoney("test_7", "test_8", transferAmount, errorMessage);
		updateCount += accountDao.transferMoney("test_8", "test_9", transferAmount, errorMessage);
		Account firstSourceAccount = accountDao.listAccountByAccountName("test_7", errorMessage);
		Account firstTargetAccount = accountDao.listAccountByAccountName("test_8", errorMessage);
		Account secondTargetAccount = accountDao.listAccountByAccountName("test_9", errorMessage);
		assertEquals(4, updateCount);
		assertTrue(firstSourceAccount.getBalance().equals(BigDecimal.valueOf(90)));
		assertTrue(firstTargetAccount.getBalance().equals(BigDecimal.valueOf(100)));
		assertTrue(secondTargetAccount.getBalance().equals(BigDecimal.valueOf(110)));
	}

	@Test
	public void testTransferMoney_ConcurrentTransfers() throws SQLException, InterruptedException {
		StringBuilder errorMessage = new StringBuilder();
		BigDecimal transferAmount = BigDecimal.valueOf(2);
		ExecutorService es = Executors.newCachedThreadPool();
		int i = 0;
		for (; i < 50; i++) {
			es.execute(new Runnable() {
				@Override
				public void run() {
					accountDao.transferMoney("test_13", "test_14", transferAmount, errorMessage);
				}
			});
		}
		es.shutdown();
		boolean finished = es.awaitTermination(5, TimeUnit.MINUTES);
		Account sourceAccount = accountDao.listAccountByAccountName("test_13", errorMessage);
		Account targetAccount = accountDao.listAccountByAccountName("test_14", errorMessage);
		assertTrue(finished);
		assertTrue(sourceAccount.getBalance().equals(BigDecimal.valueOf(400)));
		assertTrue(targetAccount.getBalance().equals(BigDecimal.valueOf(600)));
	}

	@Test
	public void testTransferMoney_LockAccount() throws Exception {
		StringBuilder errorMessage = new StringBuilder();
		BigDecimal transferAmount = BigDecimal.valueOf(2);
		new Thread(new Runnable() {
			@Override
			public void run() {
				Connection connection = null;
				try {
					Class.forName(driverClassName);
					connection = DriverManager.getConnection(connectionUrl, connectionUsername, connectionPassword);
					connection.setAutoCommit(true);
					final String sss = "SELECT * FROM ACCOUNT WHERE ACCOUNT_NAME = ? FOR UPDATE";
					accountDao.fetchAccount("test_15", connection.prepareStatement(sss));
					TimeUnit.SECONDS.sleep(11);
				} catch (Exception e) {
					logger.error("Exception in AccountDaoTest-testTransferMoney_LockAccount:" + e);
				} finally {
					if (connection != null) {
						try {
							connection.close();
						} catch (SQLException e) {
							logger.error("Exception in AccountDaoTest-testTransferMoney_LockAccount:" + e);
						}
					}
				}
			}
		}).start();
		ExecutorService es = Executors.newCachedThreadPool();
		es.execute(new Runnable() {
			@Override
			public void run() {
				try {
					TimeUnit.SECONDS.sleep(2);
					accountDao.transferMoney("test_15", "test_16", transferAmount, errorMessage);
				} catch (InterruptedException e) {
					logger.error("Exception in AccountDaoTest-testTransferMoney_LockAccount:" + e);
				}
			}
		});
		es.shutdown();
		// IMPORTANT NOTE:
		// I assume it has to finish its job between 2 - 8 seconds
		boolean finished = es.awaitTermination(8, TimeUnit.SECONDS);
		assertFalse(finished);
	}
}
