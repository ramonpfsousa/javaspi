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
import com.javaspi.exception.ConfigNotFoundException;
import com.javaspi.exception.NoInsertableClassException;
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
		columnWhere.delete(columnWhere.length()-5, columnWhere.length()-1);
		StringBuilder sb = new StringBuilder("UPDATE ");
		sb.append(nameTable).append(" SET ").append(columnSet).append(" WHERE ").append(columnWhere);
		System.out.println(sb.toString());
		valuesSet.addAll(valuesWhere);
		executeUpdate(sb.toString(), valuesSet);
		return table;
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
		return preparedStatement.execute();
	}
	
	private int executeUpdate(String query, List<Object> values) throws SQLException {
		PreparedStatement preparedStatement = conn.prepareStatement(query);
		processValues(preparedStatement, values);
		return preparedStatement.executeUpdate();
	}

	private void processValues(PreparedStatement preparedStatement, List<Object> values) throws SQLException {
		for (int i = 1; i <= values.size(); i++) {
			preparedStatement.setObject(i, values.get(i-1));
		}
	}
	
}
