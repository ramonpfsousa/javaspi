package com.javaspi.provider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.javaspi.exception.ConfigNotFoundException;

public class Manager {

	private Connection conn;
	
	private Manager(Connection conn){
		this.conn = conn;
	}
	
	public static Manager getInstance(String context) throws SQLException, IOException, ConfigNotFoundException {
		return new Manager(AbstractProvider.getInstance(context).getConnection()); 
	}
	
	public <T> T insert(T table) {
		return null;
	}
	
	public <T> T update(T table) {
		return null;
	}
	
	public void executeProcedure(Object procedure) {
	}
	
	public void executeFunction(Object function) {
	}
	
	public void close() throws SQLException {
		if(conn != null && !conn.isClosed()) 
			conn.close();
	}
	
	public void commit() throws SQLException {
		if(conn != null && !conn.isClosed())
			conn.commit();
	}
}
