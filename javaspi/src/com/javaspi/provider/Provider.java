package com.javaspi.provider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.javaspi.exception.ConfigNotFoundException;

interface Provider {

	public Connection getConnection() throws SQLException, IOException, ConfigNotFoundException;
	
	public void closeConnection(Connection conn) throws SQLException;
}
