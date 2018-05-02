package compliancevalidator.ontologyprofiling;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import compliancevalidator.misc.StringUtilities;
import compliancevalidator.wordnet.RiWordNetOperations;
import compliancevalidator.wordnet.WordNetOperations;
import fr.inrialpes.exmo.ontosim.string.StringDistances;
import net.didion.jwnl.JWNLException;
import rita.RiWordNet;

/**
 * @author audunvennesland Date:02.02.2017
 * @version 1.0
 */
public class OntologyStatistics {

	/**
	 * An OWLOntologyManagermanages a set of ontologies. It is the main point
	 * for creating, loading and accessing ontologies.
	 */
	static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	/**
	 * The OWLReasonerFactory represents a reasoner creation point.
	 */
	static OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();

	/**
	 * A HashMap holding an OWLEntity as key and an ArrayList of instances
	 * associated with the OWLEntity
	 */
	private static HashMap<OWLEntity, ArrayList<String>> instanceMap = new HashMap<OWLEntity, ArrayList<String>>();

	static StringDistances ontoString = new StringDistances();

	/**
	 * Default constructor
	 */
	public OntologyStatistics() {

	}

	/**
	 * Returns a Map holding a class as key and its superclass as value
	 * 
	 * @param o
	 *            the input OWL ontology from which classes and superclasses
	 *            should be derived
	 * @return classesAndSuperClasses a Map holding a class as key and its
	 *         superclass as value
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static Map<String, String> getClassesAndSuperClasses(OWLOntology o) throws OWLOntologyCreationException {

		OWLReasoner reasoner = reasonerFactory.createReasoner(o);
		Set<OWLClass> cls = o.getClassesInSignature();
		Map<String, String> classesAndSuperClasses = new HashMap<String, String>();
		ArrayList<OWLClass> classList = new ArrayList<OWLClass>();

		for (OWLClass i : cls) {
			classList.add(i);
		}

		// Iterate through the arraylist and for each class get the subclasses
		// belonging to it
		// Transform from OWLClass to String to simplify further processing...
		for (int i = 0; i < classList.size(); i++) {
			OWLClass currentClass = classList.get(i);
			NodeSet<OWLClass> n = reasoner.getSuperClasses(currentClass, true);
			Set<OWLClass> s = n.getFlattened();
			for (OWLClass j : s) {
				classesAndSuperClasses.put(currentClass.getIRI().getFragment(), j.getIRI().getFragment());
			}
		}

		manager.removeOntology(o);

		return classesAndSuperClasses;

	}

	/**
	 * Get all instances associated with a class in an ontology
	 * 
	 * @param owlClass
	 *            the OWL class from which instances should be retrieved
	 * @param ontology
	 *            the OWL ontology holding the owl class and its instances
	 * @return instanceList an ArrayList (String) of instances
	 */
	@SuppressWarnings("deprecation")
	public static ArrayList<String> getInstances(String owlClass, OWLOntology ontology) {
		ArrayList<String> instanceList = new ArrayList<String>();

		OWLReasonerFactory reasonerFactory = new PelletReasonerFactory();
		OWLReasoner reasoner = reasonerFactory.createNonBufferingReasoner(ontology);

		for (OWLClass c : ontology.getClassesInSignature()) {

			if (c.getIRI().getFragment().equals(owlClass)) {
				// Test
				// System.out.println("Found the class " + owlClass);

				NodeSet<OWLNamedIndividual> instanceSet = reasoner.getInstances(c, false);
				Iterator<Node<OWLNamedIndividual>> itr = instanceSet.iterator();
				if (!itr.hasNext()) {
					// Test
					// System.out.println("There are no instances associated
					// with " + c.getIRI().getFragment());
					break;
				} else {

					for (OWLNamedIndividual i : instanceSet.getFlattened()) {
						// Test
						// System.out.println("Adding " +
						// i.getIRI().getFragment() + " to the list");
						instanceList.add(i.getIRI().getFragment());
					}
				}
			}
		}

		return instanceList;
	}

	/**
	 * Get number of classes in an ontology
	 * 
	 * @param ontoFile
	 *            the file path of the OWL ontology
	 * @return numClasses an integer stating how many OWL classes the OWL
	 *         ontology has
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static int getNumClasses(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		int numClasses = onto.getClassesInSignature().size();

		manager.removeOntology(onto);

		return numClasses;
	}

	/**
	 * Returns an integer stating how many object properties an OWL ontology has
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return numObjectProperties an integer stating number of object
	 *         properties in an OWL ontology
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static int getNumObjectProperties(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		int numObjectProperties = onto.getObjectPropertiesInSignature().size();

		manager.removeOntology(onto);

		return numObjectProperties;
	}

	/**
	 * Returns an integer stating how many individuals an OWL ontology has
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return numIndividuals an integer stating number of individuals in an OWL
	 *         ontology
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static int getNumIndividuals(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		int numIndividuals = onto.getIndividualsInSignature().size();

		manager.removeOntology(onto);

		return numIndividuals;
	}

	/**
	 * Returns an integer stating how many subclasses reside in an OWL ontology.
	 * The method iterates over all classes in the OWL ontology and for each
	 * class counts how many subclasses the current class have. This count is
	 * updated for each class being iterated.
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return totalSubClassCount an integer stating number of subclasses in an
	 *         OWL ontology
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static int getNumSubClasses(File ontoFile) throws OWLOntologyCreationException {

		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		OWLReasoner reasoner = reasonerFactory.createReasoner(onto);

		OWLClass thisClass;
		NodeSet<OWLClass> subClasses;
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		Map<OWLClass, NodeSet<OWLClass>> classesAndSubClasses = new HashMap<OWLClass, NodeSet<OWLClass>>();
		int subClassCount = 0;
		int totalSubClassCount = 0;

		while (itr.hasNext()) {
			thisClass = itr.next();
			subClasses = reasoner.getSubClasses(thisClass, true);
			subClassCount = subClasses.getNodes().size();
			classesAndSubClasses.put(thisClass, subClasses);
			totalSubClassCount += subClassCount;
		}

		manager.removeOntology(onto);

		return totalSubClassCount;
	}

	/**
	 * Returns an integer stating how many of the classes in an OWL ontology
	 * contains individuals (members)
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return countClassesWithIndividuals an integer stating number of classes
	 *         having individuals/members in an OWL ontology
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static int containsIndividuals(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		OWLReasoner reasoner = reasonerFactory.createReasoner(onto);
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		int countClassesWithIndividuals = 0;

		OWLClass thisClass;

		while (itr.hasNext()) {
			thisClass = itr.next();
			if (!reasoner.getInstances(thisClass, true).isEmpty()) {
				countClassesWithIndividuals++;
			}

		}
		manager.removeOntology(onto);

		return countClassesWithIndividuals;
	}

	/**
	 * Returns an integer stating how many of the classes in an OWL ontology do
	 * not have comment annotations associated with them
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return numClassesWithoutComments an integer stating number of classes
	 *         not having annotations associated with them
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static int getNumClassesWithoutComments(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		int countClassesWithComments = 0;
		int sumClasses = onto.getClassesInSignature().size();

		IRI thisClass;

		while (itr.hasNext()) {
			thisClass = itr.next().getIRI();

			for (OWLAnnotationAssertionAxiom a : onto.getAnnotationAssertionAxioms(thisClass)) {
				if (a.getProperty().isComment()) {
					countClassesWithComments++;
				}
			}

		}

		manager.removeOntology(onto);

		int numClassesWithoutComments = sumClasses - countClassesWithComments;

		return numClassesWithoutComments;
	}

	/**
	 * Returns an integer stating how many of the classes in an OWL ontology do
	 * not have label annotations associated with them
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return numClassesWithoutLabels an integer stating number of classes not
	 *         having label annotations associated with them
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static int getNumClassesWithoutLabels(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		int countClassesWithLabels = 0;
		int sumClasses = onto.getClassesInSignature().size();

		IRI thisClass;

		while (itr.hasNext()) {
			thisClass = itr.next().getIRI();

			for (OWLAnnotationAssertionAxiom a : onto.getAnnotationAssertionAxioms(thisClass)) {
				if (a.getProperty().isLabel()) {
					countClassesWithLabels++;
				}
			}

		}

		manager.removeOntology(onto);

		int numClassesWithoutLabels = sumClasses - countClassesWithLabels;

		return numClassesWithoutLabels;
	}

	/**
	 * Returns a double stating the percentage of how many classes and object
	 * properties are present as words in WordNet. For object properties their
	 * prefix (e.g. isA, hasA, etc.) is stripped so only their "stem" is
	 * retained.
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return wordNetCoverage a double stating a percentage of how many of the
	 *         classes and object properties are represented in WordNet
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 * @throws JWNLException 
	 * @throws FileNotFoundException 
	 */
	public static double getWordNetCoverage(File ontoFile) throws OWLOntologyCreationException, FileNotFoundException, JWNLException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();
		Iterator<OWLObjectProperty> itrOP = onto.getObjectPropertiesInSignature().iterator();

		String thisClass;
		String thisOP;

		int numClasses = onto.getClassesInSignature().size();
		//int numOPs = onto.getObjectPropertiesInSignature().size();

		int classCounter = 0;
		int OPCounter = 0;

		while (itr.hasNext()) {
			thisClass = itr.next().getIRI().getFragment();
			if (WordNetOperations.containedInWordNet(thisClass) == true) {
				classCounter++;
			}
		}


		//double wordNetClassCoverage = ((double) classCounter / (double) numClasses);
		double wordNetCoverage = ((double) classCounter / (double) numClasses);
		//double wordNetOPCoverage = ((double) OPCounter / (double) numOPs);

		//double wordNetCoverage = (wordNetClassCoverage + wordNetOPCoverage) / 2;

		return wordNetCoverage;
	}


	/**
	 * Returns a double stating the percentage of how many classes and object
	 * properties are present as words in WordNet. For object properties their
	 * prefix (e.g. isA, hasA, etc.) is stripped so only their "stem" is
	 * retained.
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return wordNetCoverage a double stating a percentage of how many of the
	 *         classes and object properties are represented in WordNet
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 * @throws JWNLException 
	 * @throws FileNotFoundException 
	 */
	public static double getWordNetCoverageComp(File ontoFile) throws OWLOntologyCreationException, FileNotFoundException, JWNLException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		Set<OWLClass> classes = onto.getClassesInSignature();

		int classCounter = 0;

		for (OWLClass cl : classes) {
			//get all tokens of the class name
			String[] tokens = cl.getIRI().getFragment().split("(?<=.)(?=\\p{Lu})");

			int numTokens = tokens.length;
			int tokenCounter = 0;
			int totalCounter = 0;

			for (int i = 0; i < tokens.length; i++) {

				if (WordNetOperations.containedInWordNet(tokens[i])) {
					tokenCounter++;
				}
			}

			if (tokenCounter == numTokens) {
				totalCounter++;
			}
			
			classCounter += totalCounter;
			
		}

		
		int numClasses = onto.getClassesInSignature().size();

		double wordNetCoverage = ((double) classCounter / (double) numClasses);

		return wordNetCoverage;
	}

	/**
	 * Returns the average number of hyponyms in WordNet for each class in an
	 * ontology
	 * 
	 * @param ontoFile:
	 *            an ontology file
	 * @return the average number of hyponyms per class in an ontology
	 * @throws OWLOntologyCreationException
	 */
	public static double getHyponymRichness(File ontoFile) throws OWLOntologyCreationException {

		RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");

		double hyponymRichness = 0;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Set<OWLClass> classes = onto.getClassesInSignature();

		for (OWLClass cl : classes) {
			String[] hyponyms = RiWordNetOperations
					.getHyponyms(StringUtilities.stringTokenize(cl.getIRI().getFragment(), true));

			int numHyponyms = hyponyms.length;

			hyponymRichness += numHyponyms;
		}

		return (double) hyponymRichness / classes.size();
	}

	/**
	 * Returns the average number of synonyms in WordNet for each class in an
	 * ontology. The class name is tokenized and synonyms for each token is retrieved.
	 * 
	 * @param ontoFile:
	 *            an ontology file
	 * @return the average number of synonyms per class in an ontology
	 * @throws OWLOntologyCreationException
	 */
	public static double getSynonymRichnessComp(File ontoFile) throws OWLOntologyCreationException {

		RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");

		double synonymRichness = 0;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Set<OWLClass> classes = onto.getClassesInSignature();

		for (OWLClass cl : classes) {
			String[] tokens = cl.getIRI().getFragment().split("(?<=.)(?=\\p{Lu})");

			int numTokens = tokens.length;
			int tokenCounter = 0;
			int totalCounter = 0;

			Set<String> synTest = new HashSet<String>();

			//check if synonyms exists for all tokens
			for (int i = 0; i < tokens.length; i++) {
				String[] synonyms = RiWordNetOperations
						.getSynonyms(tokens[i].toLowerCase());

				if (synonyms.length > 0) {
					tokenCounter++;
				}

			}

			if (tokenCounter == numTokens) {
				totalCounter++;
			}


			//accumulate the synonym score for each class into a synonym richness score for all classes in the ontology
			synonymRichness += totalCounter;
		}

		//average synonym richness per class
		return (double)synonymRichness/classes.size();
	}

	

	/**
	 * Returns the average number of synonyms in WordNet for each class in an
	 * ontology without processing the class name
	 * 
	 * @param ontoFile:
	 *            an ontology file
	 * @return the average number of synonyms per class in an ontology
	 * @throws OWLOntologyCreationException
	 */
	public static double getSynonymRichness(File ontoFile) throws OWLOntologyCreationException {

		RiWordNet database = new RiWordNet("/Users/audunvennesland/Documents/PhD/Development/WordNet/WordNet-3.0/dict");

		double synonymRichness = 0;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Set<OWLClass> classes = onto.getClassesInSignature();

		for (OWLClass cl : classes) {
			String[] synonyms = RiWordNetOperations
					.getSynonyms(cl.getIRI().getFragment());

			int numSynonyms = synonyms.length;

			synonymRichness += numSynonyms;
		}

		return (double) synonymRichness / classes.size();
	}


	/**
	 * Returns a boolean stating whether a term is considered a compound term
	 * (e.g. ElectronicBook)
	 * 
	 * @param a
	 *            the input string tested for being compound or not
	 * @return boolean stating whether the input string is a compound or not
	 */
	public static boolean isCompound(String a) {
		boolean test = false;

		String[] compounds = a.split("(?<=.)(?=\\p{Lu})");

		if (compounds.length > 1) {
			test = true;
		}

		return test;
	}

	/**
	 * Returns a count of how many classes are considered compound words in an
	 * ontology
	 * 
	 * @param ontoFile
	 *            the file path of the input OWL ontology
	 * @return numCompounds a double stating the percentage of how many of the
	 *         classes in the ontology are compounds
	 * @throws OWLOntologyCreationException
	 *             An exception which describes an error during the creation of
	 *             an ontology. If an ontology cannot be created then subclasses
	 *             of this class will describe the reasons.
	 */
	public static double getNumClassCompounds(File ontoFile) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);

		Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();

		String thisClass;

		int numClasses = onto.getClassesInSignature().size();

		int counter = 0;

		while (itr.hasNext()) {
			thisClass = StringUtilities.replaceUnderscore(itr.next().getIRI().getFragment());

			if (isCompound(thisClass) == true) {
				counter++;

			}
		}

		double numCompounds = ((double) counter / (double) numClasses);

		return numCompounds;
	}

	/**
	 * Returns how many characters the most common substring among two
	 * ontologies have
	 * 
	 * @param ontoFile1:
	 *            an ontology file
	 * @param ontoFile2:
	 *            an ontology file
	 * @return an integer stating how many characters the most common substring
	 *         among two ontologies consists of
	 * @throws OWLOntologyCreationException
	 */
	private static int mostCommonSubstringLength(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Set<OWLClass> onto1Classes = onto1.getClassesInSignature();
		Set<OWLClass> onto2Classes = onto2.getClassesInSignature();

		List<Integer> commonSubstringLengths = new ArrayList<Integer>();

		for (OWLClass cl1 : onto1Classes) {
			for (OWLClass cl2 : onto2Classes) {
				Set<String> result = longestCommonSubstrings(cl1.getIRI().getFragment(), cl2.getIRI().getFragment());
				for (String s : result) {
					int length = s.length();
					commonSubstringLengths.add(length);
				}
			}
		}

		int mostCommonSubstringLength = mostCommon(commonSubstringLengths);

		return mostCommonSubstringLength;

	}

	/**
	 * Counts the most common strings in a list
	 * 
	 * @param list:
	 *            a list of strings
	 * @return number of characters in the most represented string in a list
	 */
	private static <T> T mostCommon(List<T> list) {
		Map<T, Integer> map = new HashMap<T, Integer>();

		for (T t : list) {
			Integer val = map.get(t);
			map.put(t, val == null ? 1 : val + 1);
		}

		Entry<T, Integer> max = null;

		for (Entry<T, Integer> e : map.entrySet()) {
			if (max == null || e.getValue() > max.getValue())
				max = e;
		}

		return max.getKey();
	}

	/**
	 * Returns a count of the number of common substring in two ontologies
	 * 
	 * @param ontoFile1:
	 *            an ontology file
	 * @param ontoFile2:
	 *            an ontology file
	 * @return an integer stating how many common substrings exist in two
	 *         ontologies
	 * @throws OWLOntologyCreationException
	 */
	private static int numCommonSubStrings(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {

		int commonSubStrings = 0;

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);

		Set<OWLClass> onto1Classes = onto1.getClassesInSignature();
		Set<OWLClass> onto2Classes = onto2.getClassesInSignature();

		for (OWLClass cl1 : onto1Classes) {
			for (OWLClass cl2 : onto2Classes) {
				Set<String> result = longestCommonSubstrings(cl1.getIRI().getFragment(), cl2.getIRI().getFragment());

				if (!result.isEmpty()) {


					for (String s : result) {
					}

					commonSubStrings++;
				}

			}
		}

		return commonSubStrings;

	}

	/**
	 * Returns a set of common substrings (after having compared character by
	 * character)
	 * 
	 * @param s:
	 *            an input string
	 * @param t:
	 *            an input string
	 * @return a set of common substrings among the input strings
	 */
	private static Set<String> longestCommonSubstrings(String s, String t) {
		int[][] table = new int[s.length()][t.length()];
		int longest = 5;
		Set<String> result = new HashSet<String>();

		for (int i = 0; i < s.length(); i++) {
			for (int j = 0; j < t.length(); j++) {
				if (s.charAt(i) != t.charAt(j)) {
					continue;
				}

				table[i][j] = (i == 0 || j == 0) ? 1 : 1 + table[i - 1][j - 1];
				if (table[i][j] > longest) {
					longest = table[i][j];
					result.clear();
				}
				if (table[i][j] == longest) {
					result.add(s.substring(i - longest + 1, i + 1));
				}
			}
		}
		return result;
	}

	/**
	 * Returns a measure of how many common substrings two ontologies have
	 * averaged by the total number of classes in the ontologies
	 * 
	 * @param ontoFile1:
	 *            an ontology file
	 * @param ontoFile2:
	 *            an ontology file
	 * @return a double of how many common substrings divided by the total
	 *         number of classes in the ontologies
	 * @throws OWLOntologyCreationException
	 */
	public static double commonSubstringRatio(File ontoFile1, File ontoFile2) throws OWLOntologyCreationException {

		int numClasses = getNumClasses(ontoFile1) + getNumClasses(ontoFile2);
		int numCommonSubstrings = numCommonSubStrings(ontoFile1, ontoFile2);

		int commonSubstringRatio = numCommonSubstrings / numClasses;

		return (double) commonSubstringRatio;

	}


}