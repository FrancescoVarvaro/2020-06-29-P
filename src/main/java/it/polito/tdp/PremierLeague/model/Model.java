package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private Graph<Match, DefaultWeightedEdge> grafo;
	private PremierLeagueDAO dao;
	private Map<Integer, Match> idMap;
	private List<Match> best;
	private int pesoCamminoMax;
	public Model() {
		dao = new PremierLeagueDAO();
		idMap = new HashMap<Integer, Match>();
	}
	
	public void creaGrafo(int mese, int minuti) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		idMap = new HashMap<Integer, Match>();
		dao.getVertex(idMap, mese);
		Graphs.addAllVertices(this.grafo, idMap.values());
		
		List<Adiacenze> lista = dao.getAdiacenze(idMap, mese, minuti);
		for(Adiacenze a : lista) {
			Graphs.addEdgeWithVertices(this.grafo, a.getM1(), a.getM2(), a.getPeso());
		}
	}
	
	public List<Adiacenze> getPesoMax(){
		List<Adiacenze> listaVconPesoMax = new ArrayList<>();
		int max = 0;
		for(DefaultWeightedEdge d : this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(d)>max) {
				max = (int) this.grafo.getEdgeWeight(d);
			}
		}
		for(DefaultWeightedEdge d : this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(d)==max) {
				listaVconPesoMax.add(new Adiacenze(this.grafo.getEdgeSource(d),
						this.grafo.getEdgeTarget(d), max));
			}
		}
		return listaVconPesoMax;
	}
	
	public int nVertici(){
		return this.grafo.vertexSet().size();
	}
	
	public int nEdge(){
		return this.grafo.edgeSet().size();
	}
	
	public List<Match> calcolaPercorso (Match sorgente, Match destinazione){
			
			best = new LinkedList<>();
			List<Match> parziale = new LinkedList<>();
			pesoCamminoMax = 0;
			parziale.add(sorgente);
			cerca(parziale, destinazione);
			return best;
			
		}
	public int getPesoDiBest() {
		return pesoCamminoMax;
	}
	
	private void cerca(List<Match> parziale, Match destinazione) {
		// condizione di terminazione: se l'ultimo elemento di parziale coincide con la destinazione, cioè sono arrivato
		if(parziale.get(parziale.size()-1).equals(destinazione)) {
			//allo stesso tempo parziale.get(parziale.size-1).getTeamHomeNAME DIVERSO DA: destinazione.getTeamAwayNAME
			// && parziale.get(parziale.size-1).getTeamAwayNAME DIVERSO DA: destinazione.getTeamHomeNAME
			//è la migliore?
			int pesoCammino = CalcolaCammino(parziale);
			if(pesoCammino>pesoCamminoMax) {
				pesoCamminoMax = pesoCammino;
				best = new LinkedList<>(parziale);
			}
			return;
		}
		
		//scorro i vicini dell'ultimo inserito e provo le varie "strade"
		//per ogni vicino lo aggiungo in parziale, lancio la ricorsione, faccio backtracking
		for(Match m : Graphs.neighborListOf(this.grafo, parziale.get(parziale.size()-1))) {
			
			if(!parziale.contains(m) && valida(parziale, m)) { // evito di creare dei cicli
				parziale.add(m);
				cerca(parziale, destinazione);
				parziale.remove(parziale.size()-1);
			}
		}
	}

	private boolean valida(List<Match> parziale, Match m) {
		// TODO Auto-generated method stub
		if((parziale.get(parziale.size()-1).getTeamHomeNAME().equals(m.getTeamHomeNAME()) &&
				parziale.get(parziale.size()-1).getTeamAwayNAME().equals(m.getTeamAwayNAME()) ) || 
				(parziale.get(parziale.size()-1).getTeamHomeNAME().equals(m.getTeamAwayNAME()) &&
						parziale.get(parziale.size()-1).getTeamAwayNAME().equals(m.getTeamHomeNAME()) )) {
			return false;
		}
		
		return true;
	}

	private int CalcolaCammino(List<Match> parziale) {
		// TODO Auto-generated method stub
		int somma = 0;
		for(DefaultWeightedEdge d : this.grafo.edgeSet()) {
			somma += this.grafo.getEdgeWeight(d);
		}
		return somma;
	}
}
