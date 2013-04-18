package com.javaspi.test;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import com.javaspi.exception.ConfigNotFoundException;
import com.javaspi.exception.ConfigurationException;
import com.javaspi.exception.NoInsertableClassException;
import com.javaspi.exception.NoSelectableClassException;
import com.javaspi.exception.NoUpdatableClassException;
import com.javaspi.exception.SequenceNameNotFoundException;
import com.javaspi.provider.Manager;

public class Test {

	public static void main(String[] args) throws SQLException, IOException, ConfigNotFoundException, NoInsertableClassException, IllegalArgumentException, IllegalAccessException, SequenceNameNotFoundException, NoUpdatableClassException, NoSelectableClassException, ConfigurationException, InstantiationException {
		Manager manager = Manager.getInstance("adesivo2");
//		Manager manager2 = Manager.getInstance("procurados");
//		System.out.println("conectou");
//		manager.insert(new MinhaTabela());
		MinhaTabela table = new MinhaTabela();
//		table.setId(44l);
//		table.setNome("Novo Estado");
//		table.setSigla("NE");
//		manager.update(table);
		List<MinhaTabela> lista = manager.select(table);
		for (MinhaTabela minhaTabela : lista) {
			System.out.println(minhaTabela.getNome());
		}
		manager.close();
//		manager2.close();
//		System.out.println("fechou");
	}
	
}
