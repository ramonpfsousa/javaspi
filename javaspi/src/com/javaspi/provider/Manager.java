package com.javaspi.provider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.javaspi.annotation.field.AutoIncrement;
import com.javaspi.annotation.field.Column;
import com.javaspi.annotation.field.Id;
import com.javaspi.annotation.field.Sequence;
import com.javaspi.annotation.type.Table;
import com.javaspi.annotation.type.View;
import com.javaspi.exception.ConfigNotFoundException;
import com.javaspi.exception.ConfigurationException;
import com.javaspi.exception.NoInsertableClassException;
import com.javaspi.exception.NoSelectableClassException;
import com.javaspi.exception.NoUpdatableClassException;
import com.javaspi.exception.SequenceNameNotFoundException;

public class Manager {

	private Connection conn;
	
	private Manager(Connection conn){
		this.conn = conn;
	}
	
	public static Manager getInstance(String context) throws SQLException, IOException, ConfigNotFoundException {
		return new Manager(AbstractProvider.getInstance(context).getConnection()); 
	}
	
	public <T> T insert(T table) throws NoInsertableClassException, IllegalArgumentException, IllegalAccessException, SequenceNameNotFoundException, SQLException {
		Table tableAnnotation = table.getClass().getAnnotation(Table.class);
		if(tableAnnotation == null)
			throw new NoInsertableClassException(table.getClass());
		
		Field[] fields = table.getClass().getDeclaredFields();
		StringBuilder columnNames = new StringBuilder();
		StringBuilder columnValues = new StringBuilder();
		List<Object> values = new ArrayList<Object>();
		String nameTable = (tableAnnotation.name() != null && !tableAnnotation.name().equals(""))?tableAnnotation.name().toUpperCase():table.getClass().getSimpleName().toUpperCase();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			AutoIncrement autoIncrementAnnotation = field.getAnnotation(AutoIncrement.class);
			if(autoIncrementAnnotation != null)
				break;
			Column columnAnnotation = field.getAnnotation(Column.class);
			String nameColumn = field.getName().toUpperCase();
			if(columnAnnotation != null && columnAnnotation.name() != null && !columnAnnotation.name().equals(""))
				nameColumn = columnAnnotation.name();
			columnNames.append(nameColumn).append(",");
			columnValues.append("?,");
			field.setAccessible(true);
			Sequence sequenceAnnotation = field.getAnnotation(Sequence.class);
			if(sequenceAnnotation != null) {
				if(sequenceAnnotation.name() == null || sequenceAnnotation.name().equals(""))
					throw new SequenceNameNotFoundException(table.getClass());
				Object sequenceValue = executeSequence(sequenceAnnotation.name().toUpperCase());
				values.add(sequenceValue);
				field.set(table, sequenceValue);
			} else {
				values.add(field.get(table));
			}
			field.setAccessible(false);
		}
		columnNames.deleteCharAt(columnNames.length()-1);
		columnValues.deleteCharAt(columnValues.length()-1);
		StringBuilder sb = new StringBuilder("INSERT INTO ");
		sb.append(nameTable).append("(").append(columnNames).append(") VALUES(").append(columnValues).append(")");
		System.out.println(sb.toString());
		executeInsert(sb.toString(), values);
		return table;
	}
	
	public <T> T update(T table) throws NoInsertableClassException, NoUpdatableClassException, IllegalArgumentException, IllegalAccessException, SQLException {
		Table tableAnnotation = table.getClass().getAnnotation(Table.class);
		if(tableAnnotation == null)
			throw new NoUpdatableClassException(table.getClass());
		
		Field[] fields = table.getClass().getDeclaredFields();
		StringBuilder columnSet = new StringBuilder();
		StringBuilder columnWhere = new StringBuilder();
		List<Object> valuesSet = new ArrayList<Object>();
		List<Object> valuesWhere = new ArrayList<Object>();
		String nameTable = (tableAnnotation.name() != null && !tableAnnotation.name().equals(""))?tableAnnotation.name().toUpperCase():table.getClass().getSimpleName().toUpperCase();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Column columnAnnotation = field.getAnnotation(Column.class);
			String nameColumn = field.getName().toUpperCase();
			if(columnAnnotation != null && columnAnnotation.name() != null && !columnAnnotation.name().equals(""))
				nameColumn = columnAnnotation.name();
			field.setAccessible(true);
			Id idAnnotation = field.getAnnotation(Id.class);
			if(idAnnotation != null) {
				columnWhere.append(nameColumn).append("=? AND ");
				valuesWhere.add(field.get(table));
				continue;
			}
			columnSet.append(nameColumn).append("=?,");
			valuesSet.add(field.get(table));
			field.setAccessible(false);
		}
		columnSet.deleteCharAt(columnSet.length()-1);
		
		StringBuilder sb = new StringBuilder("UPDATE ");
		sb.append(nameTable).append(" SET ").append(columnSet);
		
		if(columnWhere.length() > 0) {
			columnWhere.delete(columnWhere.length()-5, columnWhere.length()-1);
			sb.append(" WHERE ").append(columnWhere);
		}
		
		System.out.println(sb.toString());
		valuesSet.addAll(valuesWhere);
		executeUpdate(sb.toString(), valuesSet);
		return table;
	}
	
	public <T> List<T> select(T table) throws NoSelectableClassException, ConfigurationException, IllegalArgumentException, IllegalAccessException, InstantiationException, SQLException {
		Table tableAnnotation = table.getClass().getAnnotation(Table.class);
		View viewAnnotation = table.getClass().getAnnotation(View.class);
		if(tableAnnotation == null && tableAnnotation == null)
			throw new NoSelectableClassException(table.getClass());
		
		String nameTable = null;
		// testa se a classe possui as duas annotations
		if(tableAnnotation != null && viewAnnotation == null)
			nameTable = (tableAnnotation.name() != null && !tableAnnotation.name().equals(""))?tableAnnotation.name().toUpperCase():table.getClass().getSimpleName().toUpperCase();
		
		if(tableAnnotation == null && viewAnnotation != null)
			nameTable = (viewAnnotation.name() != null && !viewAnnotation.name().equals(""))?viewAnnotation.name().toUpperCase():table.getClass().getSimpleName().toUpperCase();
			
		if(nameTable == null)
			throw new ConfigurationException(table.getClass());
		
		Field[] fields = table.getClass().getDeclaredFields();
		List<Object> values = new ArrayList<Object>();
		StringBuilder columnNames = new StringBuilder();
		StringBuilder columnWhere = new StringBuilder();
		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			Column columnAnnotation = field.getAnnotation(Column.class);
			String nameColumn = field.getName().toUpperCase();
			if(columnAnnotation != null && columnAnnotation.name() != null && !columnAnnotation.name().equals(""))
				nameColumn = columnAnnotation.name();
			field.setAccessible(true);
			Object value = field.get(table);
			if(value != null) {
				values.add(value);
				columnWhere.append(nameColumn).append("=? AND ");
			}
			columnNames.append(nameColumn).append(",");
			field.setAccessible(false);
		}
		
		columnNames.deleteCharAt(columnNames.length()-1);
		
		StringBuilder sb = new StringBuilder("SELECT ");
		sb.append(columnNames).append(" FROM ").append(nameTable);
		
		if(columnWhere.length() > 0) {
			columnWhere.delete(columnWhere.length()-5, columnWhere.length()-1);
			sb.append(" WHERE ").append(columnWhere);
		}
		
		System.out.println(sb.toString());
		
		List<T> result = executeSelect(sb.toString(), values, table);
		
		return result;
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

	private Object executeSequence(String name) throws SQLException {
		String select = "SELECT NEXTVAL('"+name+"')";
		ResultSet resultSet = conn.prepareCall(select).executeQuery();
		resultSet.next();
		return resultSet.getObject(1);
	}
	
	private boolean executeInsert(String query, List<Object> values) throws SQLException {
		PreparedStatement preparedStatement = conn.prepareStatement(query);
		processValues(preparedStatement, values);
		boolean result = preparedStatement.execute(); 
		return result;
	}
	
	private int executeUpdate(String query, List<Object> values) throws SQLException {
		PreparedStatement preparedStatement = conn.prepareStatement(query);
		processValues(preparedStatement, values);
		int result = preparedStatement.executeUpdate();
		return result;
	}
	
	private <T> List<T> executeSelect(String query, List<Object> values, T classe) throws SQLException, InstantiationException, IllegalAccessException {
		PreparedStatement preparedStatement = conn.prepareStatement(query);
		processValues(preparedStatement, values);
		List<T> result = processResultSet(preparedStatement.executeQuery(), classe);
		return result;
	}

	private void processValues(PreparedStatement preparedStatement, List<Object> values) throws SQLException {
		for (int i = 1; i <= values.size(); i++) {
			preparedStatement.setObject(i, values.get(i-1));
		}
	}

	private <T> List<T> processResultSet(ResultSet resultSet, T classe) throws InstantiationException, IllegalAccessException, SQLException {
		List<T> result = new ArrayList<T>();
		while(resultSet.next()) {
			T t = (T) classe.getClass().newInstance();
			Field[] fields = classe.getClass().getDeclaredFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				Column columnAnnotation = field.getAnnotation(Column.class);
				String nameColumn = field.getName().toUpperCase();
				if(columnAnnotation != null && columnAnnotation.name() != null && !columnAnnotation.name().equals(""))
					nameColumn = columnAnnotation.name();
				field.setAccessible(true);
				field.set(t, resultSet.getObject(nameColumn));
				field.setAccessible(false);
			}
			result.add(t);
		}
		return result;
	}
	
}
