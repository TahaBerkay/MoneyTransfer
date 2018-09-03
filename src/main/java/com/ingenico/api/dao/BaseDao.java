package com.ingenico.api.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BaseDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private static Logger logger = LogManager.getLogger(BaseDao.class);
	private static final String driverClassName = "oracle.jdbc.driver.OracleDriver";
	private static final String connectionUrl = "jdbc:oracle:thin:@localhost:1521/XE";
	private static final String connectionUsername = "INGENICO";
	private static final String connectionPassword = "INGENICO";

	public Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName(driverClassName);
		return DriverManager.getConnection(connectionUrl, connectionUsername, connectionPassword);
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void closeConnection(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error("Exception while trying to close connections: " + e);
			}
		}
	}

	public void closeStatement(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				logger.error("Exception while trying to connect statements: " + e);
			}
		}
	}
}
