import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;

//Each thread processes the query, then waits for the preceding thread to print before printing.
public class QueryThread extends Thread {
	
	public void run(QueryThread oldThread, ArrayList<Triple> finalResults, HashMap<Integer, Integer> docTotals, String query, 
			HashMap<String, HashMap<Integer, Integer>> invertedIndex) throws InterruptedException {
	
		long startTime =  System.nanoTime();	
		ArrayList<Pair> rankedResults = new ArrayList();
		ArrayList weightedResults = new ArrayList();
		String[] queryWords = query.split(" ");
		queryWords = QueryProcessor.processQueries(queryWords);
		
		ArrayList docsForQuery = QueryProcessor.getDocsForQuery(queryWords, invertedIndex);
	
		for (Object documentId : docsForQuery) {
			weightedResults = new ArrayList();
		
			for (String queryWord : queryWords) {

				if (invertedIndex.containsKey(queryWord) && invertedIndex.get(queryWord).containsKey(documentId)) {
					HashMap<Integer, Integer> termHits = invertedIndex.get(queryWord);	
				
					double weightedResult = (double) termHits.get(Integer.valueOf(documentId.toString())) / docTotals.get(documentId);
					weightedResult = Math.floor(weightedResult * 1000) / 1000;
					weightedResults.add(weightedResult);
				}
			}
		
			Double finalWeight = 0.0;
			for (Object weightedResult : weightedResults) {	
				finalWeight += Double.valueOf(weightedResult.toString());	
			}
		
			Pair pair = new Pair(finalWeight.toString(), documentId.toString());
			rankedResults.add(pair);
		}
	
		Collections.sort(rankedResults);
		Collections.reverse(rankedResults);
		long queryTime = System.nanoTime() - startTime;
		queryTime = queryTime / 1000000;
	
		//If there is a previous thread, wait for it to complete before printing.
		if (oldThread != null) {
			oldThread.join();
		}
	
		System.out.println("================================================================================");
		System.out.println("Query " + "\"" + query + "\", time to process: " + queryTime + "ms");
	
		int counter = 0;
		for (Pair resultSet : rankedResults) {
			if (counter < 10) {
				System.out.println("DocId: " + resultSet.second);
				counter++;
			}
		}
	}
}
