package com.ep2.localizacao;

public class ConversorJSON {

	public String converteJson(String[]rotulos,String[] entrada) {
		String resposta = "{\n";
		for(int i=0; i<entrada.length; i++) {
			resposta +=  rotulos[i]+": "+entrada[i];
			if(i!=entrada.length-1) {
				resposta+=",";
			}
			resposta +="\n";
		}
		resposta += "}";
		return resposta;
	}
	
}
