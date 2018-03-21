package compliancevalidator.wordnet;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author audunvennesland
 * 4. okt. 2017 
 */
public class WordNetDomainsMap {
	
	/**
	 * Takes a file of words and corresponding vectors and creates a Map where the word in each line is key and the vectors are values (as ArrayList<Double>)
	 * @param vectorFile A file holding a word and corresponding vectors on each line
	 * @return A Map<String, ArrayList<Double>> where the key is a word and the value is a list of corresponding vectors
	 * @throws FileNotFoundException
	 */
	public static Map<Long, ArrayList<String>> createDomainMap (File vectorFile) throws FileNotFoundException {

		Map<Long, ArrayList<String>> vectorMap = new HashMap<Long, ArrayList<String>>();

		Scanner sc = new Scanner(vectorFile);

		//read the file holding the synset offset to domain mapping and extract the synset offset (first number in each line) as key and the domains as ArrayList<String> as value in a Map
		while (sc.hasNextLine()) {

			String line = sc.nextLine();
			String[] strings = line.split(", ");
			String trimmedSynsetString = null;
			
			String synsetString = strings[0];
			
			System.err.println("synsetString is " + synsetString);
			
			/*
			
			if (synsetString.endsWith("-n")) {
				System.out.println("Ends with -n");
			trimmedSynsetString = synsetString.substring(0, synsetString.indexOf("-n"));
			} else if (synsetString.endsWith("-r")) {
				trimmedSynsetString = synsetString.substring(0, synsetString.indexOf("-r"));
			} else if (synsetString.endsWith("-v")) {
				trimmedSynsetString = synsetString.substring(0, synsetString.indexOf("-v"));
			} else if (synsetString.endsWith("-a")) {
				trimmedSynsetString = synsetString.substring(0, synsetString.indexOf("-a"));
			} else if (synsetString.endsWith("-s")) {
				trimmedSynsetString = synsetString.substring(0, synsetString.indexOf("-s"));
			} else {
				System.out.println(synsetString);
			}*/
			
			System.err.println("synsetString is now " + trimmedSynsetString);

			Long synset = Long.valueOf(trimmedSynsetString);

			ArrayList<String> domains = new ArrayList<String>();
			for (int i = 1; i < strings.length; i++) {
				domains.add(strings[i]);
			}
			vectorMap.put(synset, domains);

		}
		sc.close();

		return vectorMap;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		
		File wnDomainFile = new File("./files/wndomains/wn-domains-3.2-20070223.txt");
		
		Map<Long, ArrayList<String>> domainMap = createDomainMap(wnDomainFile);
		
		System.out.println("domainMap contains " + domainMap.size() + " entries");
		
	}

}
