package com.ep2.localizacao;

import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins="http://localhost:8090")
@SpringBootApplication
@RestController
public class LocalizacaoApplication {

	static String path = "EP.rdf";
	static Model model = ModelFactory.createDefaultModel();
	
	//String path = "EP.owl";
	//Model model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	//model.read(path);
	
	public static void main(String[] args) {
		
		//String dir ="dados";
		//Dataset dados = TDBFactory.createDataset(dir);
		
		model.read(path);	
		SpringApplication.run(LocalizacaoApplication.class, args);

	}
	
	public String retornaDirecao(String bloco) {
		if(bloco.equalsIgnoreCase("01")) {
			bloco="primeira (1º)";
			return bloco;
		}
		if(bloco.equalsIgnoreCase("02")) {
			bloco="segunda (2º)";
			return bloco;
		}
		if(bloco.equalsIgnoreCase("03")) {
			bloco="terceira (3º)";
			return bloco;
		}
		if(bloco.equalsIgnoreCase("04")) {
			bloco="quarta (4º)";
			return bloco;
		}
		return bloco;
	}
	
	
	//Recebe corredor e retorna detalhes de como chegar
	public String retornaCaminho(String corredor) {
		String caminho = "";
		
		String andar = corredor.substring(9,11);
		String bloco = corredor.substring(11);

		bloco = retornaDirecao(bloco);
		

		//Se for no térreo só manda ir ao corredor
		if(andar.equalsIgnoreCase("00")) {			
			caminho += "Vire na "+bloco +" esquerda";
		}
		//Se for no primeiro andar indica entrada e caminho
		if(andar.equalsIgnoreCase("01")) {
			caminho += "Vire na primeira esquerda e suba a escada-rolante,";
			caminho += "No primeiro andar vire na "+ bloco +" direita";
		}
		//Se for no segundo andar indica entrada
		if(andar.equalsIgnoreCase("02")) {
			caminho += "Vire na primeira esquerda e suba a escada-rolante,";
			caminho += "No primeiro andar vire à primeira direita e suba a escada-rolante,";
			caminho += "No segundo andar vire na "+bloco +" esquerda";
		}

		return caminho;
	}
	
	//Recebe corredor e retorna detalhes de como chegar (Só com elevador)
	public String retornaCaminhoM(String corredor) {
		String caminho = "";
		
		String andar = corredor.substring(9,11);
		String bloco = corredor.substring(11);
		
		bloco = retornaDirecao(bloco);


		//Se for no térreo só manda ir ao corredor
		if(andar.equalsIgnoreCase("00")) {			
			caminho += "Vire na "+bloco +" esquerda";
		}
		//Se for no primeiro andar indica entrada e caminho
		if(andar.equalsIgnoreCase("01")) {
			caminho+= "Vire na segunda esquerda e vá até o elevador, ";
			caminho += "No primeiro andar vire na "+ bloco +" direita";
		}
		//Se for no segundo andar indica entrada
		if(andar.equalsIgnoreCase("02")) {
			caminho+= "Vire na segunda esquerda e vá até o elevador, ";
			caminho += "Vire na "+bloco +" esquerda";
		}

		return caminho;
	}
	
	public String esproxima(String bloco, String es) {
		
		//dois ultimos números do bloco
		String numBloco = bloco.substring(8);
		
//		//dois ultimos números do bloco
//		String numAndar = bloco.substring(6,8);
//		
//		if(numAndar.equalsIgnoreCase("00")) {
//			return "terreo";
//		}
		
		String[] entradas = es.split(", ");
		String atual = "--";
		
		for(int i=0; i<entradas.length; i++) {
			atual = entradas[i].substring(5, 7);
			if(atual.equalsIgnoreCase(numBloco)){
				return entradas[i];
			}
		}
		
		return atual;
	}

	//Retorna String com entradas/saidas de um andar Ex:["es1","es2",...,esN]
	public String entradaSaida(String loja, Model model) {
		
		String resposta="";
		int i=0;
		
		String consulta2 =
				"PREFIX EP2: <http://www.centrocomercial.com/ontologia#>"
				+ "SELECT ?id "
				+ "WHERE {"
				+ "?loja a EP2:Loja."
				+ "?loja EP2:seLocalizaEm ?local."
				+ "?local EP2:id_local ?idlocal."				
				+ "?local EP2:ficaNoBloco ?bloco."
				+ "?bloco EP2:rotuloBloco ?rotuloBloco."
				+ "?bloco EP2:ficaNoAndar ?andar."
				+ "?andar EP2:rotuloAndar ?rotuloAndar."
				+ "?andar EP2:acessivelPor ?es."
				+ "?es EP2:id_entrada_saida ?id."
				+ "FILTER (?loja=EP2:"+loja+")"
				+ "}";
		
		Query query = QueryFactory.create(consulta2);
		QueryExecution qe = QueryExecutionFactory.create(query, model);

		
		try {
			ResultSet resultados = qe.execSelect();
			while(resultados.hasNext()) {
				QuerySolution qs = resultados.nextSolution();
				Literal es = qs.getLiteral("id");
				if(i!=0) {
					resposta+=", \""+es+"\"";
				}else {
					resposta+="\""+es+"\"";
					i++;
				}
			}
			
		}finally {
			qe.close();
		}
		return resposta+"";
	}
	
	
	//Recebe uma instância loja e devolve sua localização como JSON
	
	@GetMapping("/local")
	public String local(@RequestParam(value = "loja", defaultValue = "Livraria Galáxia") String loja) {
		
			loja=loja.replaceAll(" ", "_");
			
			String resposta="";
												
			String consulta =
					"PREFIX EP2: <http://www.centrocomercial.com/ontologia#>"
					+ "SELECT ?idlocal ?rotuloBloco ?rotuloAndar ?corredor ?nome "
					+ "WHERE {"
					+ "?loja a EP2:Loja."
					+ "?loja EP2:seLocalizaEm ?local."
					//+ "?loja EP2:nome ?nome."
					+ "?local EP2:id_local ?idlocal."
					+ "?local EP2:corredor ?corredor."
					+ "?local EP2:ficaNoBloco ?bloco."
					+ "?bloco EP2:rotuloBloco ?rotuloBloco."
					+ "?bloco EP2:ficaNoAndar ?andar."
					+ "?andar EP2:rotuloAndar ?rotuloAndar."
					+ "FILTER (?loja=EP2:"+loja+")"
					+ "}";
						
			Query query = QueryFactory.create(consulta);
			QueryExecution qe = QueryExecutionFactory.create(query, model);

			try {
				ResultSet resultados = qe.execSelect();
				while(resultados.hasNext()) {
					QuerySolution qs = resultados.nextSolution();
					
					String idlocal = qs.getLiteral("idlocal").getString();
					String rotuloBloco = qs.getLiteral("rotuloBloco").getString();
					String rotuloAndar = qs.getLiteral("rotuloAndar").getString();
					String corredor = qs.getLiteral("corredor").getString();
					String es = entradaSaida(loja,model);
					String esprox =esproxima(rotuloBloco,es);
					String caminho = retornaCaminho(corredor);
					String caminhom = retornaCaminhoM(corredor);
					
					String[] rotulos = {"\"local\"","\"corredor\"","\"bloco\"","\"andar\"","\"es\"","\"es-prox\"","\"caminho\"","\"caminhoM\""};
					String[] res = {"\""+idlocal+"\"","\""+corredor+"\"","\""+rotuloBloco+"\"","\""+rotuloAndar+"\"","["+es+"]",esprox,"\""+caminho+"\"","\""+caminhom+"\""};
					ConversorJSON cj = new ConversorJSON();
					resposta += cj.converteJson(rotulos, res);
					System.out.println(resposta);
					
				}
				
			}finally {
				qe.close();
			}
						
		
		return String.format(resposta, loja);
	}

}
