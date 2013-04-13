package com.javaspi.test;

import java.io.IOException;
import java.sql.SQLException;

import com.javaspi.exception.ConfigNotFoundException;
import com.javaspi.provider.Manager;

public class Test {

	public static void main(String[] args) throws SQLException, IOException, ConfigNotFoundException {
		Manager manager = Manager.getInstance("adesivo2");
		Manager manager2 = Manager.getInstance("procurados");
		System.out.println("conectou");
		manager.close();
		manager2.close();
		System.out.println("fechou");
	}
	
}
