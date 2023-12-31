package kMIS;
import java.util.Random;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashSet;

public class KMIS {
	
	public static int sizeL;
	public static BitSet[] graph; // grafo, cujos vértices são todos os subsets existentes
	public static int[][] edges; // Possui a quantidade de feromônios na aresta entre o vértice i e j.
	public static PrintWriter printWriterTests;
	
	public static void main(String[] args) throws IOException {
		
		final int k = 3;
		int ite = 0;
		final int MAX_ITER = 10; // 30;
		final int g = 5; //formigas ativas
		
		// cria o grafo, cujos vértices são todos os subsets existentes
		graph = chooseGroup();
		
		// Possui a quantidade de feromônios na aresta entre o vértice i e j.
		edges = new int[sizeL][sizeL];
		
		Solution bestSolution = new Solution(null, 0), solution;
		int cardInBestSolution = -1, cardIn;
		
		while(ite <= MAX_ITER) {
			
			printWriterTests.println("\r\n ***Iteração: " + ite + "***");
			
			// ArrayList com as informações das formigas ativas
			// a linha representa uma formiga (total de g formigas)
			// a coluna representa os subsets que a formiga caminhou (passou por até k vértices)
			ArrayList<ArrayList<Integer>> activeAnts = new ArrayList<ArrayList<Integer>>();
			
			// Etapa de construção de soluções
			activeAnts = updateActiveAnts(activeAnts, g, k);	
			
			// Atualização de L*
			activeAnts = walk(activeAnts, k); // anda com cada formiga pelo grafo
			printActiveAnts(activeAnts);
			
			// Aplicação da busca local VND nas g formigas
			for(ArrayList<Integer> activeAnt : activeAnts) {
				
				solution = heuristicaVND(activeAnt, k);
				cardIn = solution.cardinalityIntersection;
				
				if(cardIn > cardInBestSolution) {
					
					bestSolution = solution;
					cardInBestSolution = cardIn;
					
				}
			}
			
			printWriterTests.print("Melhor solução da iteração: ");
			printSolution(bestSolution);
			
			
			ite++;
		}
		
		printWriterTests.close();
	}
	
	/**
	 * Função que define quantos subsets serão retirados da formiga para serem substituídos
	 * @param k
	 */
	public static int f(int k) {
		
		if(k < 2) {
			printWriterTests.println("Erro: o k deve ser maior que 2");
			return 0;
		} else if(k == 2) {
			return 1;
		} else if(k == 3) {
			return 2;
		} 
		
		return Math.max((k*3)/10, 3);
		
	}
	
	/**
	 * Recebe uma arrayList com índices de subsets e preenche-a até que tenha tamanho k.
	 * A arraylist será preenchida pelos melhores subsets possíveis a serem combinados do grafo
	 * @param solution
	 * @param k
	 * @return uma solução completa dado a arraylist que possui uma solução incompleta 
	 */
	public static Solution kInter(ArrayList<Integer> solution, int k) {
		
		BitSet intersect = graph[solution.get(0)]; // o ideal é um bitset de 1s de tamanho sizeR
		for(int m = 0; m < solution.size(); m++) {
			intersect.and(graph[solution.get(m)]);
		}
		
		int bigger = -1;
		
		while(solution.size() < k) {
			
			int cardClone;
			bigger = -1;
			int bestSubset =-1;
			
			search: 
			for(int i = 1; i < sizeL; i++) {
				
				for(int j = 0; j < solution.size(); j++) {
					if(solution.get(j) == i) {
						continue search;
					}
				}
				
				BitSet clone = (BitSet) intersect.clone();
				clone.and(graph[i]);
				cardClone = clone.cardinality();
				
				if(cardClone > bigger) {
					bigger = cardClone;
					bestSubset = i;
				}
				
			}
			
			solution.add(bestSubset);
			intersect.and(graph[bestSubset]);
			
		}
		
		return new Solution(solution, bigger);
			
	}
	
	/**
	 * Pegará uma solução (uma formiga) e gerará um conjunto de vizinhos (formigas parecidas)
	 * essas formigas parecidas terão f subsets de diferença
	 * No fim, a melhor formiga da vizinhança deve ser escolhida
	 * 
	 * Gera vizinhança de tamanho CombinaçãoSimples(k, f(k))
	 * @param solution
	 * @param k
	 * @return
	 */
	public static Solution heuristicaVND(ArrayList<Integer> solution, int k) {
		
		printWriterTests.print("Aplicação de VND para a formiga: ");
		for(Integer s : solution) {
			printWriterTests.print(s + " ");
		}
		printWriterTests.println("");
		
		// vizinhança que conterá as variações de formigas
		ArrayList<ArrayList<Integer>> neighborhood = new ArrayList<ArrayList<Integer>>();
		
		// Cálculo dos índices de subsets que serão retirados de solution para formar as novas formigas da vizinhança
		ArrayList<Integer> setForCombinations = new ArrayList<Integer>();
		for(int i=0; i<k; i++) setForCombinations.add(i);
		ArrayList<ArrayList<Integer>> combinations = gerarCombinacoes(setForCombinations, f(k));
		
		// Cria a vizinhança
		for(ArrayList<Integer> combination : combinations) {
			ArrayList<Integer> newSolution = (ArrayList<Integer>) solution.clone();
			for(Integer i : combination) {
				newSolution.set(i, -1);
			}
			for (int j = newSolution.size() - 1; j >= 0; j--) {
	            if (newSolution.get(j) == -1) {
	            	newSolution.remove(j);
	            }
	        }
			neighborhood.add(newSolution);
		}
		
		Solution bestSolution = new Solution(null, 0);
		Solution auxSolution;
		int biggerCardinality = -1;
		
		// em cada vizinho, aplica a heurística kInter
		for(ArrayList<Integer> neighbor : neighborhood) {
			
			auxSolution = kInter(neighbor, k);
			printSolution(auxSolution);
			
			if(auxSolution.cardinalityIntersection > biggerCardinality) {
				biggerCardinality = auxSolution.cardinalityIntersection;
				bestSolution.solution = auxSolution.solution;
				bestSolution.cardinalityIntersection = auxSolution.cardinalityIntersection;
			}
		}
		
		printWriterTests.print("Melhor solução: ");
		printSolution(bestSolution);
		
        return bestSolution;
	}
	
	/**
	 * Imprime informações da solução
	 * @param sol
	 */
	public static void printSolution(Solution sol) {
		printWriterTests.print("Solução com subsets: ");
		for(Integer s : sol.solution) {
			printWriterTests.print(s + " ");
		}
		printWriterTests.println(". Cardinalidade da interseção: "+ sol.cardinalityIntersection);
	}
	
	/**
	 * Anda com a formiga pelo grafo
	 * @param activeAnts
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> walk(ArrayList<ArrayList<Integer>> activeAnts, int kSub){
		
        double arrProbability[] = new double[sizeL];
		
		// a cada iteração trabalhamos com uma formiga
		for(ArrayList<Integer> ant : activeAnts) {
			
			while(ant.size() < kSub) {
				
				// criação do vetor com as probabilidades da formiga visitar cada um dos vértices
				double sum = 0;
				int i = ant.get(ant.size() - 1);
				for(int k = 0; k < sizeL; k++) {
					sum += auxProbability(i, k, ant);
					arrProbability[k] = sum;
					printWriterTests.println("Probabilidade da formiga "+ i +" ir para o subset "+k+": "+ arrProbability[k]);
				}
				
				// escolha do próximo subset que a formiga percorrerá obedecendo a probabilidade
				Random r = new Random();
				double randomNumber = r.nextDouble() * sum;
		        printWriterTests.println("Numero aleatorio: " + randomNumber );
		        int subset = 0;
		        while((arrProbability[subset] <= randomNumber) && subset < sizeL) {
		        	subset++;
		        }
		        printWriterTests.println("Subset escolhido: " +  subset);
		        
		        // adição do subset ao caminho percorrido pela formiga 
		        ant.add(subset);
		        
		        // adição de feromônio ao caminho que a formiga percorreu
		        edges[i][subset] += (edges[i][subset] <= 0) ? 2 : 1;
				
			}
			
		}
		return activeAnts;
	}
	
	
	/**
	 * Função do cálculo da propabilidade da formiga sair do vértice i e ir para o vértice j
	 * @param i
	 * @param j
	 * @param pathAnt
	 * @return
	 */
	public static double probability(int i, int j, ArrayList<Integer> pathAnt) {
		double sum = 0;
		
		for(int k = 0; k < sizeL; k++) {
			if(k == i) continue;
			sum += auxProbability(i, k, pathAnt);
		}
		
		return auxProbability(i, j, pathAnt) / sum;
	}
	
	/**
	 * Função auxiliar do cálculo da propabilidade da formiga t, sair do vértice i e ir para o vértice j
	 * @param t
	 * @param i
	 * @param j
	 * @return
	 */
	public static double auxProbability(int i, int j, ArrayList<Integer> pathAnt) {
		
		if(i==j) return 0;
		
		// Faz a interseção de todos os subsets (vértices) em que a formiga já passou
		BitSet intersect = (BitSet) graph[pathAnt.get(0)].clone();
		for(int l = 1; l < pathAnt.size(); l++) {
			intersect.and(graph[pathAnt.get(l)]);
		}
		// armazena o valor de elementos da interseção
		int cardinalityIntersect = intersect.cardinality(); 
		
		// Faz interseção com o novo subset que se quer inserir
		intersect.and(graph[j]);
		// armazena o valor de elementos da interseção até o vértice j
		int cardinalityIntersectUntilVerticeJ = intersect.cardinality(); 
		
		// cálculo do ntij
		double dtij = cardinalityIntersect - cardinalityIntersectUntilVerticeJ + 1;
		double ntij = 1 / dtij;
		
		// cálculo do Tij, que representa a quantidade de feromônio do vértice i até o j
		double Tij = (edges[i][j]==0) ?  1 : edges[i][j];
		
		return ntij * Tij;
	}
	
	/**
	 * Imprime o ArrayList com as formigas ativas, mostrando os vértices que a formiga já percorreu
	 * @param activeAnts
	 * @param g
	 * @param k
	 */
	public static void printActiveAnts(ArrayList<ArrayList<Integer>> activeAnts) {
		
		for(ArrayList<Integer> ant : activeAnts) {
			
			printWriterTests.print("Formiga: ");
			
			for(Integer subset : ant) {
				printWriterTests.print(subset +", ");
			}
			
			printWriterTests.println("");
			
		}
		
	}
	
	/**
	 * Retorna uma lista de tamanho (size) de números aleatórios não repetidos entre [0, range-1], 
	 * @param size
	 * @param range
	 * @return
	 */
	public static ArrayList<Integer> listRandomNumbers(int size, int range){
		
		if(size > range) {
			printWriterTests.println("Não foi possível criar a lista de números aleatórios");
			return null;
		}
		
		ArrayList<Integer> list = new ArrayList<Integer>(); 
		Set<Integer> set = new HashSet<>();
		Random random = new Random();
		
        for (int i = 0; i < size; i++) {
        	
            int randomNumber = random.nextInt(range);
            
            while (set.contains(randomNumber)) {
                randomNumber = random.nextInt(range);
            }
            
            list.add(randomNumber);
            
            set.add(randomNumber);
        }
		
		return list;
	}
	
	
	/**
	 * Antieriormente: Retorna um array LStar com números aleatórios não repetidos de [0,sizeL-1}
	 * @param LStar
	 * @param k
	 * @return
	 */
	/**
	 * Gera g formigas e coloca eles em um vértice aleatório do grafo. 
	 * Os k-1 caminhos restantes da formiga recebem valores aleatorios, mas temos que mudar essa função
	 * (Os k-1 caminhos restantes da formiga recebem valor -1)
	 * @param activeAnts
	 * @param g
	 * @param k
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> updateActiveAnts(ArrayList<ArrayList<Integer>> activeAnts, int g, int k) {
		
		/*
        ArrayList<Integer> list = listRandomNumbers(g * k, sizeL);
        */
        ArrayList<Integer> list = listRandomNumbers(g, sizeL);
        
        int cont_list = 0;
        
        for(int it_g = 0; it_g < g; it_g++) {
        	
        	ArrayList<Integer> ant = new ArrayList<Integer>();
        	
        	/*
        	for(int it_k = 0; it_k < k; it_k++) {
        		ant.add(list.get(cont_list));
        		cont_list++;
        	}
        	*/
        	ant.add(list.get(cont_list));
        	cont_list++;
        	for(int it_k = 1; it_k < k; it_k++) {
        		//ant.add(-1);
        	}
        	
        	
        	activeAnts.add(ant);
        }
        
        return activeAnts;
	}
	
	/*
	 * Escolhe as seguintes variáveis randomicamente para selecionar o grafo bipartido da pasta GilbertInstances
	 * 
	 * type: {1,2,3}
	 * l: {0,1,...,9} (to choose an element of allN)
	 * group: {1,2,3}
	 * 
	 */
	public static BitSet[] chooseGroup() throws IOException {
		
		Random generator = new Random();
		
		int type = 3; //generator.nextInt(3) + 1;
		int l = 1; //generator.nextInt(10);
		int group = 1; //generator.nextInt(3) + 1;
		
		int allN[] = {40, 60, 80, 100, 140, 180, 200, 240, 280, 300};
		int nL, nR;
		
		if(type == 1){
            nL = allN[l];
            nR = allN[l];
        } else if(type == 2){
            nL = allN[l];
            nR = (allN[l]*8)/10;
        }else{
            nL = (allN[l]*8)/10;
            nR = allN[l];
        }
		
		return initialSolution(group, nL, nR);
	}
	
	/*
	 * Cria um grafo representado por vetor de string de bits.
	 * cada string de bits representa um vértice(nó, Si), 
	 * o valor 0 na posição i representa que não há o valor i no conjunto Si.
	 * o valor 1 na posição i representa que o valor i está no conjunto Si.
	 * 
	 * group: {1, 2, 3}
	 * sizeL: Número de subsets. L representa os conjuntos que estão em S.
	 * sizeR: Número de elementos dos valores associados aos subsets. R representa os valores que estão dentro dos conjuntos que estão em S.
	 */
	public static BitSet[] initialSolution(int group, int nL, int nR) throws IOException {
		
		int type;
		sizeL = nL;
		
		if(nL == nR) {
			type = 1;
		} else if(nL > nR) {
			type = 2;
		} else {
			type = 3;
		}
		
		String path = "GilbertInstances/type"+type+"/group_"+group+"_"+nL+"_"+nR+".txt";
		
		Date now = new Date();
		printWriterTests = new PrintWriter("testes/" + "GilbertInstances/type"+type+"/group_"+group+"_"+nL+"_"+nR+ "_" + now.getTime() + ".txt" );
		
		// cada elemento do vetor é uma bitString que representa um subset Si
		BitSet graph[] = new BitSet[nL];
		for(int i=0;i<nL;i++) {
			graph[i] = createGraphNode(nR);
		}
		
		
		// file reader
		BufferedReader buffRead = new BufferedReader(new FileReader(path));
		
		String line = buffRead.readLine();
		
		while(true) {

			line = buffRead.readLine();
			
			if(line != null) {
				
				String values[] = line.split("\\s");
				int vL = Integer.parseInt(values[0]);
				int vR = Integer.parseInt(values[1]);
				
				graph[vL-1].set(vR-1);
				
			} else 
				break;
			
			
		}
		
		buffRead.close();
		
		printGraph(graph, path);
		
		return graph;
	}
	
	
	/**
	 * Cria um nó do grafo, cujos elementos do subset são todos 0
	 * 
	 * @param sizeR
	 * @return
	 */
	public static BitSet createGraphNode(int sizeR) {
		BitSet node = new BitSet();
		node.clear();
		return node;
	}
	
	/**
	 * Imprime os nós do grafo (as bitStrings que compõem o grafo)
	 * @param graph
	 * @param path
	 */
	public static void printGraph(BitSet[] graph, String path) {
		
		for(int i=0;i<graph.length;i++) {
			printWriterTests.println("Subset " + (i+1) + ": " + graph[i].toString());
		}
		
		printWriterTests.println("File: " + path);
		
	}
	
	/**
	 * Função que gera combinações simples
	 * @param conjunto
	 * @param tamanho
	 * @return
	 */
	public static ArrayList<ArrayList<Integer>> gerarCombinacoes(ArrayList<Integer> conjunto, int tamanho) {
		ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        gerarCombinacoesRecursivas(conjunto, tamanho, 0, new ArrayList<>(), result);
        return result;
    }
	
	/**
	 * Função auxiliar de combinações simples
	 * @param conjunto
	 * @param tamanho
	 * @param inicio
	 * @param combAtual
	 * @param result
	 */
    private static void gerarCombinacoesRecursivas(ArrayList<Integer> conjunto, int tamanho, int inicio, ArrayList<Integer> combAtual, ArrayList<ArrayList<Integer>> result) {
        if (tamanho == 0) {
            result.add(new ArrayList<>(combAtual));
            return;
        }

        for (int i = inicio; i <= conjunto.size() - tamanho; i++) {
            combAtual.add(conjunto.get(i));
            gerarCombinacoesRecursivas(conjunto, tamanho - 1, i + 1, combAtual, result);
            combAtual.remove(combAtual.size() - 1);
        }
    }
	
	
	
	
	
	// funções desnecessárias
	
	/**
	 * Retorna um array LStar com números aleatórios não repetidos de [0,sizeL-1}
	 * @param LStar
	 * @param k
	 * @return
	 */
	public static int[] updateLStar(int[] LStar, int k) {
		
        Set<Integer> set = new HashSet<>();

        Random random = new Random();
        for (int i = 0; i < k; i++) {
        	
            int randomNumber = random.nextInt(sizeL);
            
            while (set.contains(randomNumber)) {
                randomNumber = random.nextInt(sizeL);
            }
            
            LStar[i] = randomNumber;
            
            set.add(randomNumber);
        }

        return LStar;
	}
	
	
	
	
	
	public static ArrayList<int[]> updateActiveAnts_preenche_o_primeiro_cara(ArrayList<int[]> activeAnts, int g, int k) {
		
        Set<Integer> set = new HashSet<>();

        Random random = new Random();
        for (int i = 0; i < g; i++) {
        	
            int randomNumber = random.nextInt(sizeL);
            
            while (set.contains(randomNumber)) {
                randomNumber = random.nextInt(sizeL);
            }
            
            int[] antPath = new int[k];
            antPath[0] = randomNumber;
            for(int j = 1; j < k; j++) {
            	antPath[j] = -1;
            }
            
            activeAnts.add(antPath);
            
            set.add(randomNumber);
        }

        return activeAnts;
	}

}


