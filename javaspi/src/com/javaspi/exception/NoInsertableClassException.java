package com.javaspi.exception;

public class NoInsertableClassException extends Exception {

	private static final long serialVersionUID = 494296964633590112L;
	
	public NoInsertableClassException(Class classe) {
		super("A classe "+classe.getName()+" nao esta mapeada como tabela");
	}

}
