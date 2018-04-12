package compliancevalidator.matchers.subsumption;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import compliancevalidator.misc.ISub;
import compliancevalidator.misc.Jaccard;
import compliancevalidator.misc.StringUtilities;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class DefinitionsSubsMatcher extends ObjectAlignment implements AlignmentProcess {

	final String isA = "&lt;";
	final String hasA = "&gt;";

	public DefinitionsSubsMatcher() {
	}

	public void align(Alignment alignment, Properties param) throws AlignmentException {

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
					
					//get map from getDefinitionsSubSim where the relation is the key and the value is the similarity measure
					Map<String, Double> matchingMap = getDefinitionsSubSim(cl1, cl2);
					
					//add mapping into alignment object for each entry in the matching map
					for (Map.Entry<String, Double> entry : matchingMap.entrySet()) {
						addAlignCell(cl1, cl2, entry.getKey(), entry.getValue());
					}

				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}



	private Map<String, Double> getDefinitionsSubSim(Object o1, Object o2) throws OntowrapException {

		//map to keep the relation and matching score
		Map<String,Double> matchingMap = new HashMap<String,Double>();
		String relation = null;
		double measure = 0;
		double sim = 0;

		//get definitions (comments) associated with the entities to be matched
		Set<String> o1Defs = ontology1().getEntityComments(o1);
		Set<String> o2Defs = ontology2().getEntityComments(o2);

		//probably only one set item with definition, but just in case we merge all possible set items into a single string
		String def1 = StringUtils.join(o1Defs, " ");
		String def2 = StringUtils.join(o2Defs, " ");

		//find out which definition has more text
		if (def1.length() > def2.length()) {
			relation = isA;
		} else {
			relation = hasA;
		}

		//if there is a definition it´s probably longer than 4 chars...
		if (def1.length() > 4 && def2.length() > 4) {
			sim = computeSimpleSim(def1, def2);
		}

		//need to keep our sim within [0..1] 
		if (sim > 0 && sim <= 1) {
			measure = sim;
		} else {
			measure = 0;
		}
		
		//test
		if (sim > 0.95) {
		System.out.println("Testing definitions subsumption sim");
		System.out.println("Definition 1 (" + o1.toString() + "): " + def1);
		System.out.println("\nDefinition 2 (" + o2.toString() + "): " + def2);
		System.out.println("Sim: " + sim);
		
		}
		matchingMap.put(relation, measure);
		
		return matchingMap;

	}

	public double getDefinitionsSim(Object o1, Object o2) throws OntowrapException, OWLOntologyCreationException, IOException {

		double measure = 0;
		double sim = 0;
		//get definitions (comments) associated with the entities to be matched
		Set<String> o1Defs = ontology1().getEntityComments(o1);
		Set<String> o2Defs = ontology2().getEntityComments(o2);

		//probably only one set item with definition, but just in case we merge all possible set items into a single string
		String def1 = StringUtils.join(o1Defs, " ");
		String def2 = StringUtils.join(o2Defs, " ");

		//if there is a definition it´s probably longer than 4 chars...
		if (def1.length() > 4 && def2.length() > 4) {
			sim = computeSimpleSim(def1, def2);
		}

		//need to keep our sim within [0..1] 
		if (sim > 0 && sim <= 1) {
			measure = sim;
		} else {
			measure = 0;
		}


		return measure;

	}


	/**
	 * A very simple string matcher based on the ISub algorithm
	 * @param s1
	 * @param s2
	 * @return
	 */
	private static double computeSimpleSim(String s1, String s2) {

		double sim = 0; 

		ISub isub = new ISub();

		sim = isub.score(s1, s2);

		return sim;
	}

	private static double computeJaccardSim(String s1, String s2) throws IOException {

		double sim = 0;

		//remove stopwords
		String s1WOStop = StringUtilities.removeStopWordsFromString(s1);
		String s2WOStop = StringUtilities.removeStopWordsFromString(s2);

		String[] s1Array = s1WOStop.split(" ");
		String[] s2Array = s2WOStop.split(" ");		

		Set<String> s1Set = new HashSet<String>(Arrays.asList(s1Array));
		Set<String> s2Set = new HashSet<String>(Arrays.asList(s2Array));

		sim = Jaccard.jaccardSetSim(s1Set, s2Set);

		return sim;

	}
}