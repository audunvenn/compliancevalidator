package definitions;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

/**
 * @author audunvennesland
 * 15. okt. 2017 
 */
public class DefinitionsExtractor {

	/**
	 * Takes a file of element names and corresponding definitions and creates a Map where the element name in each line is key and the definition is value (String)
	 * @param definitionsFile A file holding an element and corresponding definition on each line, separated by ;
	 * @return A Map<String, String>> where the key is a element name and the value is a definition
	 * @throws FileNotFoundException
	 */
	public static Map<String, String> createDefinitionsMap (File definitionsFile) throws FileNotFoundException {

		Map<String, String> defMap = new HashMap<String, String>();

		Scanner sc = new Scanner(definitionsFile);

		//read the file and extract the element name (first word in each line) as key and the definition as String as value in a Map
		while (sc.hasNextLine()) {

			String line = sc.nextLine();
			if (!line.isEmpty()) {
				String[] strings = line.split("&");

				//if the line contains both an element name and a definition
				if (strings.length == 2) {

					//st.replaceAll("\\s+","")
					String elementName = strings[0];
					String elementDefinition = strings[1];


					//using replaceAll to remove whitespace
					defMap.put(elementName.replaceAll("\\s+",  ""), elementDefinition);
				}
			}
		}
		sc.close();

		return defMap;
	}

	public static void addClassDefinitions (File inputOntologyFile, Map<String, String> defMap) throws OWLOntologyCreationException, OWLOntologyStorageException {

		//import AIRM ontology
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);
		OWLDataFactory df = OWLManager.getOWLDataFactory();

		//get all classes from the inputOntologyFile
		Set<OWLClass> classes = ontology.getClassesInSignature();

		//compare the class names (fragments) with the key of the definitions map and if they are equal, add the definition as rdfs comment to the class

		for (OWLClass cls : classes) {
			String className = cls.getIRI().getFragment();
			//for code lists
			if (className.contains("Type")) {
				//System.err.println(className + " contains Type");
				String classNameCode = (className.substring(0, className.indexOf("Type"))) + "BaseType";
				//System.err.println("The classNameCode is " + classNameCode);
				if (defMap.containsKey(classNameCode)) {
					System.err.println("Searching defMap for " + classNameCode);
					String def = defMap.get(classNameCode);

					OWLAnnotation comment = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral(def, "en"));
					OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(cls.getIRI(), comment);
					manager.applyChange(new AddAxiom(ontology, ax));
				}
			}

			else {
				if (defMap.containsKey(className)) {
			
				//System.err.println(className + " is in the definitions file");
				String def = defMap.get(className);

				OWLAnnotation comment = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral(def, "en"));
				OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(cls.getIRI(), comment);
				manager.applyChange(new AddAxiom(ontology, ax));
			} 
		}}

		manager.saveOntology(ontology);
		System.out.println("Definitions added to classes!");

	}
	

	public static void addIndividualsDefinitions (File inputOntologyFile, Map<String, String> defMap) throws OWLOntologyCreationException, OWLOntologyStorageException {

		//import AIRM ontology
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);
		OWLDataFactory df = OWLManager.getOWLDataFactory();

		//get all individuals from the inputOntologyFile
		Set<OWLNamedIndividual> individuals = ontology.getIndividualsInSignature();

		//compare the individual names (fragments) with the key of the definitions map and if they are equal, add the definition as rdfs comment to the individual

		for (OWLNamedIndividual ind : individuals) {
			String indName = ind.getIRI().getFragment();
			//for code lists
			
				if (defMap.containsKey(indName)) {

				String def = defMap.get(indName);

				OWLAnnotation comment = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral(def, "en"));
				OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(ind.getIRI(), comment);
				manager.applyChange(new AddAxiom(ontology, ax));
			
		}}

		manager.saveOntology(ontology);
		System.out.println("Definitions added to individuals!");

	}

	public static void addPropertyDefinitions (File inputOntologyFile, Map<String, String> defMap) throws OWLOntologyCreationException, OWLOntologyStorageException {

		//import AIRM ontology
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();		
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);
		OWLDataFactory df = OWLManager.getOWLDataFactory();

		//get all object properties from the inputOntologyFile
		Set<OWLObjectProperty> objectProperties = ontology.getObjectPropertiesInSignature();

		//get all data properties from the inputOntologyFile
		Set<OWLDataProperty> dataProperties = ontology.getDataPropertiesInSignature();

		//compare the object property names (fragments) with the key of the definitions map and if they are equal, add the definition as rdfs comment to the object property				
		for (OWLObjectProperty op : objectProperties) {
			String objectPropertyName = op.getIRI().getFragment();
			if (defMap.containsKey(objectPropertyName)) {
				//System.err.println(objectPropertyName + " is in the definitions file");
				String def = defMap.get(objectPropertyName);

				OWLAnnotation comment = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral(def, "en"));
				OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(op.getIRI(), comment);
				manager.applyChange(new AddAxiom(ontology, ax));
			}
		}

		System.out.println("Added definitions to object properties");

		//compare the data property names (fragments) with the key of the definitions map and if they are equal, add the definition as rdfs comment to the data property				
		for (OWLDataProperty dp : dataProperties) {
			String dataPropertyName = dp.getIRI().getFragment();
			if (defMap.containsKey(dataPropertyName)) {
				//System.err.println(dataPropertyName + " is in the definitions file");
				String def = defMap.get(dataPropertyName);

				OWLAnnotation comment = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral(def, "en"));
				OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(dp.getIRI(), comment);
				manager.applyChange(new AddAxiom(ontology, ax));
			}
		}

		System.out.println("Added definitions to data properties");		

		manager.saveOntology(ontology);
		System.out.println("Definitions added to properties!");

	}



	public static void main(String[] args) throws FileNotFoundException, OWLOntologyCreationException, OWLOntologyStorageException {

		File defFile = new File("./files/BEST/iwxxm/definitions/iWXXMDefs.txt");

		Map<String, String> defMap = createDefinitionsMap(defFile);


		//add definitions
		File aixmOnto = new File("./files/BEST/iwxxm/ontologies/iwxxm_taf.owl");
		addClassDefinitions(aixmOnto, defMap);
		addIndividualsDefinitions(aixmOnto, defMap);
		addPropertyDefinitions(aixmOnto, defMap);

	}

}
