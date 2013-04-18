package com.javaspi.test;

import java.util.Date;

import com.javaspi.annotation.field.Id;
import com.javaspi.annotation.field.Sequence;
import com.javaspi.annotation.type.Table;

@Table(name="estado")
public class MinhaTabela {
	
	@Id
	@Sequence(name="estado_seq")
	private Integer id;
	private String nome;
	private String sigla;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getSigla() {
		return sigla;
	}
	public void setSigla(String sigla) {
		this.sigla = sigla;
	}
}

