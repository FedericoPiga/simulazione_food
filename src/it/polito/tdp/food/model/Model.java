package it.polito.tdp.food.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.food.db.Condiment;
import it.polito.tdp.food.db.FoodDao;

public class Model {
	
	private Map<Integer, Condiment> ingredientiMap;
	private Graph<Condiment, DefaultWeightedEdge> grafo;
	private List<IngredientiComuni> ingredientiComuni;
	private FoodDao dao;
	private List<Condiment> best;
	private double calMax;
	
	public Model() {
		this.ingredientiMap = new HashMap<>();
		this.dao = new FoodDao();
	}
	
	public void creaGrafo(double calorie) {
		
		this.grafo = new SimpleWeightedGraph<Condiment, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		ingredientiMap.clear();
		
		dao.getIngredientiDaCalorie(calorie, this.ingredientiMap);
		
		this.ingredientiComuni = new ArrayList<IngredientiComuni>(dao.getIngredientiComuni(ingredientiMap));
		
		Graphs.addAllVertices(grafo, ingredientiMap.values());
		
		for(IngredientiComuni ic : this.ingredientiComuni) {
			Graphs.addEdge(grafo, ic.getC1(), ic.getC2(), ic.getCibiComuni());
		}
	}
	
	public String getIngredienti() {
		String lista = "";
		List<Condiment> listaOrdinata = new ArrayList<Condiment>(grafo.vertexSet());
		Collections.sort(listaOrdinata);
		for(Condiment c1 : listaOrdinata) {
			int peso = 0;
			for(Condiment c2 : grafo.vertexSet()) {
				if(c1.equals(c2)) {
					for(DefaultWeightedEdge e : grafo.edgesOf(c1)) {
						peso += grafo.getEdgeWeight(e);
					}
					lista += c1.getDisplay_name() + " " + c1.getFood_code() + " " + c1.getCondiment_calories() + " " + peso + "\n";
				}
			}
		}
		return lista;

	}

	public Collection<Condiment> getListaIngredienti() {
		// TODO Auto-generated method stub
		return this.ingredientiMap.values();
	}
	
	public List<Condiment> getDietaEquilibrata(Condiment scelto) {
		best = new ArrayList<>();
		calMax = Double.MIN_VALUE;
		List<Condiment> parziale = new ArrayList<Condiment>();
		
		parziale.add(scelto);
		this.ricorsione(1, parziale);
		parziale.remove(scelto);
		
		return best;
	}
	
	private void ricorsione(int livello, List<Condiment> parziale) {
		
		double cal = 0;
		for(Condiment c : parziale) {
			cal += c.getCondiment_calories();
		}
		if(cal > calMax) {
			calMax = cal;
			best = new ArrayList<Condiment>(parziale);
		}
		
		Condiment ultimo = parziale.get(livello-1);
		
		for(Condiment c : this.ingredientiMap.values()) {
			if(!parziale.contains(c) && grafo.getEdge(c, ultimo) == null) {
				parziale.add(c);
				this.ricorsione(livello + 1, parziale);
				parziale.remove(c);
			}
		}
	}

}
