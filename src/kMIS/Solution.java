package kMIS;
import java.util.ArrayList;

public class Solution {
	
	// a solução é um array com k subsets do grafo
	public ArrayList<Integer> solution;
	
	// cardinalityIntersection é a cardinalidade da interseção de todos os subsets do grafo
	public int cardinalityIntersection = 0;
	
	public Solution(ArrayList<Integer> _sol, int _cardIn){
		solution = _sol;
		cardinalityIntersection = _cardIn;
	}

}
