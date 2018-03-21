package compliancevalidator.matchers.equivalence;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import compliancevalidator.misc.ISub;
import compliancevalidator.misc.StringUtilities;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;

/**
 * @author audunvennesland
 * 16. nov. 2017 
 */
public class AnnotationMatcher {
	
public static void main(String[] args) throws OWLOntologyCreationException, AlignmentException, IOException {
		
		//import the two ontology files
		File ontoFile1 = new File("./files/ontologies/iwxxm/iwxxm_common.owl");
		File ontoFile2 = new File("./files/ontologies/airm/airm-mono.owl");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		OWLOntology ontology1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology ontology2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		double threshold = 0.1;
		
		String onto1 = StringUtilities.stripOntologyName(ontoFile1.getName());
		String onto2 = StringUtilities.stripOntologyName(ontoFile2.getName());
		
		BasicAlignment computedAlignment =  new URIAlignment();
		
		
		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		computedAlignment.init( ontology1.getOntologyID().getOntologyIRI().toURI(), ontology2.getOntologyID().getOntologyIRI().toURI(), A5AlgebraRelation.class, BasicConfidence.class );
		
		String alignmentFileName = null;
		File outputAlignment = null;
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;
		//Properties params = new Properties();
		
		
		//get a set of all classes for each of the OWL ontologies
		Set<OWLClass> onto1ClsSet = getOWLClasses(ontoFile1);
		Set<OWLClass> onto2ClsSet = getOWLClasses(ontoFile2);
		
		System.err.println("Created class sets");
		
		System.err.println("Ontology AIRM has " + onto1ClsSet.size() + " entities ");
		System.err.println("Ontology AIXM has " + onto2ClsSet.size() + " entities ");
		

		Map<OWLClass, String> onto1ClsAndDefinitionsMap = getDefinitions(ontoFile1);
		Map<OWLClass, String> onto2ClsAndDefinitionsMap = getDefinitions(ontoFile2);
		
		
		Map<String, Double> resultsMap = new HashMap<String, Double>();
		
		System.err.println("Starting matching process");
		final long startTime = System.nanoTime();

		for (Entry<OWLClass, String> test1 : onto1ClsAndDefinitionsMap.entrySet()) {
			System.out.println(test1.getValue());
		}
		
		System.out.println("Onto2");
		
		for (Entry<OWLClass, String> test2 : onto2ClsAndDefinitionsMap.entrySet()) {
			System.out.println(test2.getValue());
		}

		double sim  = 0;
		String entityPair = null;
		for (Entry<OWLClass, String> e1 : onto1ClsAndDefinitionsMap.entrySet()) {
			for (Entry<OWLClass, String> e2 : onto2ClsAndDefinitionsMap.entrySet()) {
				sim = computeSimpleSim(e1.getValue(), e2.getValue());
				//System.out.println("Matching " + e1.getKey() + " and " + e2.getKey() + " with sim: " + sim);
				
				if ( sim > 0.9) {
					computedAlignment.addAlignCell(e1.getKey().getIRI().toURI(), e2.getKey().getIRI().toURI(), "=", sim);
					entityPair = e1.getKey().getIRI().toString() + " - " + e2.getKey().getIRI().toString();

					resultsMap.put(entityPair,sim);
				}
			}
		}
		
		System.out.println("Number of correspondences identified: " + resultsMap.size());
		final long duration = System.nanoTime() - startTime;

		System.out.println("The matching took " + (duration / 1000000000) + " seconds ");
		
		System.out.println("Number of cells in computedAlignment: " + computedAlignment.nbCells());
		
		
		alignmentFileName = "./files/computedAlignments/" + onto1 + "-" + onto2 + "-AnnotationMatcher.rdf";

		outputAlignment = new File(alignmentFileName);

		writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		renderer = new RDFRendererVisitor(writer);

		BasicAlignment StringAlignment = (BasicAlignment)(computedAlignment.clone());

		StringAlignment.cut(threshold);

		StringAlignment.render(renderer);
		
		System.err.println("The StringAlignment contains " + StringAlignment.nbCells() + " correspondences");
		writer.flush();
		writer.close();
	}

/**
 * Creates a map holding all classes and corresponding definitions (RDFS:Comments) in an ontology
 * @param ontoFile
 * @return
 * @throws OWLOntologyCreationException
 */
private static Map<OWLClass, String> getDefinitions(File ontoFile) throws OWLOntologyCreationException {
	
	Map<OWLClass, String> classAndDefinition = new HashMap<OWLClass, String> ();
	String definition = null;
	

	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
	OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
	Iterator<OWLClass> itr = onto.getClassesInSignature().iterator();

	OWLClass thisEntity;

	while (itr.hasNext()) {
		thisEntity = itr.next();

		for (OWLAnnotationAssertionAxiom a : onto.getAnnotationAssertionAxioms(thisEntity.getIRI())) {
			if (a.getProperty().isComment()) {
				definition = a.getAnnotation().getValue().toString();
			}
		}
		
		classAndDefinition.put(thisEntity, definition);

	}

	manager.removeOntology(onto);

	return classAndDefinition;
}
	
	
	
	private static String getEntityDefinition(OWLEntity ent, OWLOntology onto) {
		String definition = null;

		Set<OWLAnnotationAssertionAxiom> oaa = onto.getAnnotationAssertionAxioms(ent.getIRI());
		
		for (OWLAnnotationAssertionAxiom a : oaa) {
			if (a.getProperty().isComment()) {
				definition = a.getAnnotation().getValue().toString();
			}
		}
		
		return definition;

		
	}
	
	private static String getClassDefinition(OWLClass cls, OWLOntology onto) {
		String definition = null;

		Set<OWLAnnotationAssertionAxiom> oaa = onto.getAnnotationAssertionAxioms(cls.getIRI());
		
		for (OWLAnnotationAssertionAxiom a : oaa) {
			if (a.getProperty().isComment()) {
				definition = a.getAnnotation().getValue().toString();
			}
		}
		
		return definition;

		
	}
	
	public static double computeEntityDefinitionsSim (File ontoFile1, File ontoFile2, OWLEntity e1, OWLEntity e2) throws OWLOntologyCreationException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		double simDef = 0;
		
		String a1Def = getEntityDefinition(e1, onto1);
		String a2Def = getEntityDefinition(e2, onto2);
		
		//compute similarity between definitions
		computeSimpleSim(a1Def, a2Def);

		
		return simDef;
		
	}
	
	
	public static double computeClassDefinitionsSim (File ontoFile1, File ontoFile2, OWLClass c1, OWLClass c2) throws OWLOntologyCreationException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(ontoFile1);
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(ontoFile2);
		
		double simDef = 0;

		
		String a1Def = getClassDefinition(c1, onto1);
		String a2Def = getClassDefinition(c2, onto2);
		
		//compute similarity between definitions
		computeSimpleSim(a1Def, a2Def);

		
		return simDef;
		
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
	
	private static Set<OWLEntity> getOWLEntities (File ontoFile) throws OWLOntologyCreationException {
		
		Set<OWLEntity> entitySet = new HashSet<OWLEntity>();
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		entitySet = onto.getSignature();
		
		return entitySet;
		
	}
	
	private static Set<OWLClass> getOWLClasses (File ontoFile) throws OWLOntologyCreationException {
		
		Set<OWLClass> clsSet = new HashSet<OWLClass>();
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology onto = manager.loadOntologyFromOntologyDocument(ontoFile);
		
		clsSet = onto.getClassesInSignature();
		
		return clsSet;
		
	}

	
	
	
	

}
