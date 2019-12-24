package hw6;

import java.util.*;
import java.io.*;
import java.lang.StringBuilder;


class sortBySize implements Comparator<Business> {
	public int compare(Business biz1, Business biz2) {
		if (biz1.getChar() > biz2.getChar())
			return 1;
		else if (biz1.getChar() < biz2.getChar())
			return -1;
		else 
			return -1; 
	}
}

class sortByMap implements Comparator<Map.Entry<String,Double>>
{
	public int compare(Map.Entry<String,Double> map1, Map.Entry<String,Double> map2) {
		return map2.getValue().compareTo(map1.getValue());
	}
}

public class YelpAnalysis {
	//read the file and create a business object for each business
	
	//SAMPLE
	//{28105, 
	//Bourquin Residential Group, 
	//2920 E Camelback Rd Ste100 Phoenix AZ 85016, 
	//this 
	//...
	//...the past years}

	public static void main(String[] args) {
		//begin parsing
		//Read the file, and create a Business Object for each business. 
		//Each Business should contain its reviews as a String. 
		//You may use the class Business provided in Business.java.

		//from teacher code
		Map<String, Integer> corpusDFCount = new HashMap<>();
		PriorityQueue<Business> businessQueue = new PriorityQueue<Business>(10, new sortBySize());
		HashSet<String> commonWords = new HashSet<String>(Arrays.asList("a", "had", "is","it","an", "and", "was", "our", "the", "be", "to", "of", "if", "in", "that", "have", "i", "I", "have", "for", "not", "on", "with", "he", "she", "they", "do", "by", "his", "we", "say", "one", "all", "my", "would", "there", "what", "so", "up", "but", "were", "her", "you", "are", "us", "very", "got", "me", "its")); 
		//list sourced from most common words in english language on wikipedia and words that were appearing too frequently in reviews
		
		//initalizae readers
		FileReader fileRead = null;
		BufferedReader buffRead = null; // for efficiency

		//try catch finally for file finding reading and closing stream
		try {
			buffRead = new BufferedReader(new FileReader("yelpDatasetParsed_full.txt"));
			//make new business objects until end of file
			while(true)
			{
				Business b = parseFile(buffRead);
				if (b == null)
					break; //end of file and processed all businesses

				addDocumentCount(corpusDFCount, b);
				businessQueue.add(b);
				if(businessQueue.size() > 10)
					businessQueue.remove();
			}

		}
		catch(FileNotFoundException e) {
			System.out.println("File not found");
			return;
		}		

		catch(IOException e) {
			System.out.println("IOException");
			return;	
		}

		finally {
			if (buffRead != null) {
				try{
					buffRead.close();
				} catch(IOException x) {
					System.out.println("IOException V2 - File not found");
				}
			}
		}

		//sort by character count
		//Collections.sort(businessList, Collections.)

		//for the top 10 businesses with the most review characters
		for (int i=0; i<10; i++)  {
			Business currB = businessQueue.remove();
			Map<String,Double> tfidfScoreMap = getTfidfScore(corpusDFCount, currB, 5, commonWords); 
			//System.out.print(tfidfScoreMap);

			//Entry is a static nested interface of class Map
			List<Map.Entry<String,Double>> tfidfScoreList = new ArrayList<>(tfidfScoreMap.entrySet());
			//System.out.print(tfidfScoreList);


			sortByTfidf(tfidfScoreList); 
			System.out.println(currB); 
			printTopWords(tfidfScoreList, 30);
		}
	}


	//add document count 
	public static void addDocumentCount(Map<String, Integer> corpus_count, Business b) {
		//this is called after a business b is read from the file and parsed
		
		//FIRST used in
		//Business b = parseFile(buffRead);
		//if (b == null)
			//break; //end of file and processed all businesses
		//addDocumentCount(corpusDFCount, b);

		//THEN used in 
		//Map<String,Double> tfidfScoreMap = getTfidfScore(corpusDFCount, currB, 5);

		//were building the corpus_count map to have the number of documents each word appears in 

		//idea
		//store and split reviews
		//create word array from a business review 
		//store it into a map 
		//go through all the documents (iterator?) 
		//if it appears increment the int
		//if it doesn't, dont 

		//store and split reviews
		String review = b.getReview();
		String[] word_array = review.split(" ");
		
		//store it into a map 
		Map<String,Integer> doc_map = new HashMap<String,Integer>();
		for(String value:word_array) {
			doc_map.put(value,1);
		}

		//go through all the documents (iterator?) 
		//if it appears increment the int
		//if it doesn't, dont 
		//iterate through MAP
		//private Iterator<Map<String,Integer>> corpusItr = corpus_count.entrySet().iterator();
		//private Iterator<Map<String,Integer>> corpusItr = doc_map.get(0).iterator();

		Iterator<Map.Entry<String,Integer>> corpusItr = doc_map.entrySet().iterator();
		while (corpusItr.hasNext()) {
			Map.Entry<String,Integer> local = corpusItr.next();
			String my_key = local.getKey();
			if(corpus_count.get(my_key) == null) 
				corpus_count.put(my_key,1);
			else {
				int value = corpus_count.get(my_key);
				corpus_count.put(my_key, value+1);
			}
		}

	}

	//returns a clean business object
	public static Business parseFile(BufferedReader x) throws IOException{
		//what comes in each bizObj
		// String businessID;
		// String businessName;
		// String businessAddress;
		// String reviews;
		// int reviewCharCount;
		int reviewCharCount = 0;

		// parse by finding chunks separated by commas
		
		String line = x.readLine();	
		if (line == null)
			return null;	

		String[] parsedBiz = line.split(",");

		//clean up data, by removing spaces and braces
		//use stringBuilder since string is immutable 
		//make new string for each parsed section

		//biz id
		StringBuilder b_id = new StringBuilder(parsedBiz[0]);
		b_id.deleteCharAt(0); //getting rid of opening brace

		//biz name 
		StringBuilder b_name = new StringBuilder(parsedBiz[1]);
		b_name.deleteCharAt(0); //get rid of intro space

		//biz address 
		StringBuilder b_address = new StringBuilder(parsedBiz[2]);
		b_address.deleteCharAt(0); //get rid of intro space

		//string of reviews 
		StringBuilder b_reviews = new StringBuilder(parsedBiz[3]);
		b_reviews.deleteCharAt(0); //get rid of intro space
		
		//get rid of exit curly brace
		int curlyEndIndex = b_reviews.length() - 1;
		b_reviews.deleteCharAt(curlyEndIndex);

		//get char count
		reviewCharCount = b_reviews.length();

		//can't pass stringBuilder since pass by reference so change them all around
		parsedBiz[0] = b_id.toString();
		parsedBiz[1] = b_name.toString();
		parsedBiz[2] = b_address.toString();
		parsedBiz[3] = b_reviews.toString();

		//make new business object to return 
		Business biz_Return = new Business(parsedBiz[0], parsedBiz[1], parsedBiz[2], parsedBiz[3], reviewCharCount);
		return biz_Return;
	}

	//2. for each word count the number of documents the word appears in 


	//Map<String,Double> tfidfScoreMap = getTfidfScore(corpusDFCount, currB, 5)
	public static Map<String,Double> getTfidfScore(Map<String, Integer> corpus, Business b, int my_int, HashSet<String> commonWords) {
		//map to be returned with scores
		Map<String,Double> tfidf_map = new HashMap<String, Double>();

		//map to hold frequencies in review per word 
		Map<String,Double> frequencies = new HashMap<String,Double>();

		//mimic the code from wordDocCount
		String review = b.getReview();
		String[] wordsArray = review.split(" ");

		for (String s : wordsArray) {
			if(!commonWords.contains(s)) {
				if(!frequencies.containsKey(s)) {
					frequencies.put(s, 0.0); //start at 0
				}
				else {
					double count = frequencies.get(s);
					frequencies.put(s, count+1.0);
				}
			}
		}
		//System.out.println();
		//calculate tfidf 
		String word = "";
		for(Map.Entry<String,Double> entry : frequencies.entrySet()) {
			//go through each element in frequencies
			word = entry.getKey();
			double timesAppearingInReview = 0.0; //numerator in formula
			double timesAppearingInFile = 0.0; //denominator in formula

			//for the entire file (corpus) get the number of times the word appears
			//System.out.println(word);
			//System.out.println(corpus);
			timesAppearingInFile = (double)corpus.get(word);
			timesAppearingInReview = entry.getValue();

			//4. If a word appears in less than 5 documents, assign a tf-idf score of 0.
			//if the tfidf score appears in less than my_int documents, make the tfidf score 0
			double score = timesAppearingInReview/timesAppearingInFile;

			if(timesAppearingInFile < my_int)
				tfidf_map.put(word, 0.0);
			else
				tfidf_map.put(word, score);
		}
		//System.out.println(tfidf_map);

		return tfidf_map;
	}

	public static void sortByTfidf(List<Map.Entry<String,Double>> x) {
		Collections.sort(x, new sortByMap());
	}

	//3. For the top 10 Businesses with the most characters in their reviews, 
	//output (to the command line) the top 30 words with the highest tf-idf scores.
	public static void printTopWords(List<Map.Entry<String,Double>> tfidf_score_list, int num_top_words) {
		PrintStream o = System.out;

		// System.out.print("score list");
		// System.out.print(tfidf_score_list);
		//go through the num_top_words to print this section of the code 
		//no need to use 10 at all as this will be called for each of the top ten businesses
		for (int i = 0; i < num_top_words; i++)
		{		
			//make local entry set 
			Map.Entry<String,Double> local = tfidf_score_list.get(i);

			String word = local.getKey();
			double score = local.getValue();
			// System.out.print("Score is");
			// System.out.print(score);
			//sample output
			//(bacchanal,10.75)
			o.format("(%s,%.2f)", word, score);
		}
	}

	//Business currB = businessQueue.remove();		
	//Map<String,Double> tfidfScoreMap = getTfidfScore(corpusDFCount, currB, 5); 
	// //Entry is a static nested interface of class Map
	// List<Map.Entry<String,Double>> tfidfScoreList = new ArrayList<>(tfidfScoreMap.entrySet());

	// sortByTfidf(tfidfScoreList); 
	// System.out.println(currB); 
	// printTopWords(tfidfScoreList, 30);
}



