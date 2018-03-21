package compliancevalidator.misc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import compliancevalidator.matchers.equivalence.ISubMatcher;
import compliancevalidator.wordnet.RiWordNetOperations;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class Test {
	
	public static void main(String[] args) throws IOException, OWLOntologyCreationException, OntowrapException {
		
		String s1 = "AirportHeliport";
		String s2 = "Aerodrome";
		
		String[] s1Array = s1.split("(?<=.)(?=\\p{Lu})");
		String[] s2Array = s2.split("(?<=.)(?=\\p{Lu})");
		
		double score = wordNetMatch(s1Array, s2Array);
		
		System.out.println("The score is " + score);
		
		/*ISub isub = new ISub();
		double score = isub.score(s1, s2);
		System.out.println("The ISub score is " + score);
		
		String defRunwayElement1 = StringUtilities.removeStopWordsFromString("Runway element may consist of one ore more polygons not defined as other portions of the runway class.");
		String defRunwayElement2 = StringUtilities.removeStopWordsFromString("A portion of a runway.");
		
		System.out.println(defRunwayElement1);
		System.out.println("\n"+defRunwayElement2);
		
		if (s1.contains(s2) && lastToken(s1).equals(lastToken(s2))) {
			System.out.println("s1 > s2");
		}
		
		System.out.println(lastToken(s1));
		
		String[] arrayS1 = s1.split("(?<=.)(?=\\p{Lu})");
		int length = arrayS1.length;
		
		System.out.println("The length is " + length);
		
		String s3 = "CloudLayer";
		String s4 = "Cloud";
		
		System.out.println(compoundMatch(s3,s4));
		
		System.out.println(isCompoundSimple(s3,s4));
		
		File ontology = new File("./files/experiment_06032018/datasets/d2/ontologies/iwxxm_common.owl");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		OWLOntology inputOnto = manager.loadOntologyFromOntologyDocument(ontology);
		
		Set<OWLClass> classes = inputOnto.getClassesInSignature();
		
		for (OWLClass cls : classes) {
			System.out.println(cls.getIRI().getFragment());
		}*/
		
	}
	
	public static String lastToken(String inputString) {
		
		String[] compounds = inputString.split("(?<=.)(?=\\p{Lu})");
		
		int last = compounds.length;
		
		return compounds[last-1];
	}
	
	public static double compoundMatch(String s1, String s2)  {


			if (s1 == null || s2 == null) return 0.;
			
			if (isCompoundSimple(s1,s2) && !s1.equals(s2)) { 
				return 1.0;
			} else if (isCompoundSimple(s2,s1) && !s2.equals(s1)) { 
				return 1.0;
			}
			else { 
				return 0.;
			}

	}
	
	//public static String[] getSynonyms(String inputWord) {
	public static double wordNetMatch(String[] s1, String[] s2) {
		
		double jaccardSim = 0;
		
		Set<String> synonymsS1 = new HashSet<String>();
		Set<String> synonymsS2 = new HashSet<String>();
		
		for (String s : s1) {
			List<String> s1List = Arrays.asList(RiWordNetOperations.getSynonyms(s.toLowerCase()));
			for (String t : s1List) {
				System.out.println("Adding " + t + " to synonymsS1");
				synonymsS1.add(t);
			}
		}
		
		for (String s : s2) {
			List<String> s2List = Arrays.asList(RiWordNetOperations.getSynonyms(s.toLowerCase()));
			for (String t : s2List) {
				System.out.println("Adding " + t + " to synonymsS2");
				synonymsS2.add(t);
			}
		}
		
		if (!synonymsS1.isEmpty() && !synonymsS2.isEmpty()) {
		jaccardSim = Jaccard.jaccardSetSim(synonymsS1, synonymsS2);
		}
		
		System.out.println("The size of synonymsS1 is " + synonymsS1.size());
		System.out.println("The size of synonymsS2 is " + synonymsS2.size());
		
		return jaccardSim;
		
		
	}
	
	public static boolean isCompoundSimple(String a, String b) {
		
		String[] arrayA = a.split("(?<=.)(?=\\p{Lu})");
		String[] arrayB = b.split("(?<=.)(?=\\p{Lu})");
		
		int numWordsA = arrayA.length;
		int numWordsB = arrayB.length;
		
			boolean test = false;

			if (b.contains(a) && lastToken(b).equals(lastToken(a)) || a.contains(b) && lastToken(a).equals(lastToken(b))) {
				test = true;
			} else if (b.contains(a) && numWordsB <=2 || a.contains(b) && numWordsA <= 2)
				test = true;
			else {
				test = false;
			}

		return test;
	}
		
}

	

