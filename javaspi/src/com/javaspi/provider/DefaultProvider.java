package com.javaspi.provider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.javaspi.exception.ConfigNotFoundException;
import com.javaspi.util.Util;

class DefaultProvider implements Provider {
	private String context;
	
	public DefaultProvider(String context) {
		this.context = context;
	}

	@Override
	public Connection getConnection() throws SQLException, IOException, ConfigNotFoundException {
		Properties properties = Util.getProperties(context);
		return DriverManager.getConnection(properties.getProperty("jspi.url"), properties.getProperty("jspi.user"), properties.getProperty("jspi.passwd"));
//		return DriverManager.getConnection("jdbc:postgresql://localhost:5432/procurados", "postgres", "admin");
	}

	@Override
	public void closeConnection(Connection conn) throws SQLException {
		if(conn != null && !conn.isClosed())
			conn.close();
	}

}
