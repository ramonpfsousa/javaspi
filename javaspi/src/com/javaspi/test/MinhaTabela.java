package com.javaspi.test;

import java.util.Date;

import com.javaspi.annotation.field.Id;
import com.javaspi.annotation.field.Sequence;
import com.javaspi.annotation.type.Table;

@Table(name="estado")
public class MinhaTabela {
	
	@Id
	@Sequence(name="estado_seq")
	private Long id;
	private String nome;
	private String sigla;

}

