package com.javaspi.exception;

public class SequenceNameNotFoundException extends Exception {

	public SequenceNameNotFoundException(Class classe) {
		super("Nome da sequencia nao informado na classe "+classe.getName());
	}
}
