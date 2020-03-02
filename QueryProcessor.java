
import java.awt.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;

import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;

public class QueryProcessor {
	public static void main (String[] args) throws FileNotFoundException, InterruptedException {
	
		ArrayList<Pair> intermediateOut = new ArrayList();
		HashMap<String, HashMap<Integer, Integer>> invertedIndex = new HashMap<>();
		File queryFile = new File(args[1]);
		
		File dir = new File(args[0]);
		String[] fileList = dir.list();
		
		int counter = 0;
		System.out.print("Building term list...");
		for (String file : fileList) {
			intermediateOut(fileList[counter], args[0], intermediateOut);
			if (counter % 100 == 0) {System.out.print(".");}
			counter++;
		}
		
		System.out.println();
		removeStopWords(intermediateOut);
		HashMap<Integer, Integer> docTotals = getDocTotals(intermediateOut);
		reduce(intermediateOut, invertedIndex);	
		processQuery(docTotals, queryFile, invertedIndex);
	}
	
	public static void processQuery(HashMap<Integer, Integer> docTotals, File queryFile, 
		HashMap<String, HashMap<Integer, Integer>> invertedIndex) throws FileNotFoundException, InterruptedException {
		ArrayList<Triple> finalResults = new ArrayList();
		Scanner scanner = new Scanner(queryFile);
		QueryThread oldThread = null;
		//Create and start a new QueryThread for each query.
		//Pass each thread a pointer to the previous thread for synchronization.
		//Each thread prepares the query and waits for the previous thread before printing, preserving query order.
		//Java threads terminate automatically when completed.
		while (scanner.hasNextLine()) {
			String query = scanner.nextLine();	
			QueryThread thread = new QueryThread();
			thread.run(oldThread, finalResults, docTotals, query, invertedIndex);
			oldThread = thread;
		}
	}

	public static ArrayList getDocsForQuery (String[] queryWords, HashMap<String, HashMap<Integer, Integer>> invertedIndex) {
		HashMap<Integer, Integer> termMap;

		ArrayList<Integer> toReturn = new ArrayList();
		
		for (String queryWord : queryWords) {
			if (invertedIndex.containsKey(queryWord)) {
				termMap = invertedIndex.get(queryWord);	
				Set<Integer> termHits = termMap.keySet();
				for (Integer hit : termHits) {
					if (!toReturn.contains(hit)) {
						toReturn.add(hit);
					}	
				}
			}	
		}			
		return toReturn;
	}
	
	public static String[] processQueries (String[] queries) {
		Stemmer stemmer = new Stemmer();
		String[] toReturn = new String[queries.length];
		for (int a = 0; a < queries.length; a++) {
			toReturn[a] = queries[a].toLowerCase();
			toReturn[a] = stemmer.stem(queries[a]);
		}
		return toReturn;		
	}
		
	public static void reduce(ArrayList<Pair> pairs, HashMap invertedIndex) {
		HashMap<Integer, Integer> termIndex;
		System.out.print("Building term index...");
		for (Pair pair : pairs) {
			String term = pair.first.toString();
			int docId = Integer.valueOf( pair.second.toString());
			
			if (!invertedIndex.containsKey(term)) {
				termIndex = new HashMap<Integer, Integer>();
				termIndex.put(docId, 1);
				invertedIndex.put(term, termIndex);
				} 
			else {
				termIndex = (HashMap<Integer, Integer>) invertedIndex.get(term);
				if (termIndex.containsKey(docId)) {
					termIndex.put(docId, termIndex.get(docId) + 1);
				} 
				else {
					termIndex.put(docId, 1);
				}
			}
		}
		System.out.println();
	}
	
	public static void intermediateOut (String fileName, String directoryPath, ArrayList intermediateOut) throws FileNotFoundException {
		File file = new File(directoryPath + "/" + fileName);
		Scanner scanner = new Scanner(file);
		Stemmer stemmer = new Stemmer();
		String regexString = "(\\w+'?-?\\w+(?:\\.\\w+)?)";
		Pattern pattern = Pattern.compile(regexString);
		
		String[] tokens = fileName.split("\\.");
		String docId = tokens[0];
	
		while (scanner.hasNextLine()) {
			String line = parseHtml(scanner.nextLine());
			Matcher matcher = pattern.matcher(line);
			
			while(matcher.find()) {
				String match = matcher.group(0).toLowerCase();    
				match = stemmer.stem(match);
				Pair<String, Integer> pair = new Pair<String, Integer>();
				pair.setFirst(match);
				pair.setSecond(Integer.valueOf(docId));
				intermediateOut.add(pair);	
			}
		}
	}
		
	public static String parseHtml(String html) {
		return Jsoup.parse(html).text();
	}
	
	public static void removeStopWords(ArrayList<Pair> intermediateOut) throws FileNotFoundException {
		File stopWords = new File("stopWords.txt");
		Scanner scanner = new Scanner(stopWords);
		Stemmer stemmer = new Stemmer();
		while (scanner.hasNextLine()) {
			String stopWord = (stemmer.stem(scanner.nextLine()));
			intermediateOut.removeIf(term -> term.first.equals(stopWord));	
		}
	}

	public static HashMap<Integer, Integer> getDocTotals(ArrayList<Pair> pairList) {
		HashMap<Integer, Integer> docTotals = new HashMap();
		for (Pair pair: pairList) {
			Integer docId = Integer.valueOf(pair.second.toString());
		
			if (!docTotals.containsKey(docId)) {
				docTotals.put(docId, 1);
			} else {
				docTotals.put(docId, 
						docTotals.get(docId) + 1);
			}
		}
		return docTotals;
	}
	
	
} 
	



