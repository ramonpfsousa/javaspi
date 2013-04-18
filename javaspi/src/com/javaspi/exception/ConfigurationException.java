package com.javaspi.exception;

public class ConfigurationException extends Exception {

	private static final long serialVersionUID = 494296964633590112L;
	
	public ConfigurationException(Class classe) {
		super("A classe "+classe.getName()+" nao esta mapeada como tabela");
	}

}
