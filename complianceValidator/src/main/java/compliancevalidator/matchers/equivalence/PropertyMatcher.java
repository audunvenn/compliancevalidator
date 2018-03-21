package compliancevalidator.matchers.equivalence;

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import compliancevalidator.misc.ISub;
import compliancevalidator.misc.Jaccard;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class PropertyMatcher extends ObjectAlignment implements AlignmentProcess {
	
	final static ISub isub = new ISub();
	final static double threshold = 0.8;
	
	public PropertyMatcher() {
	}
	
	public void align(Alignment alignment, Properties param) throws AlignmentException {
		
		//int counter = 0;

		try {
			// Match classes
			for ( Object cl2: ontology2().getClasses() ){
				for ( Object cl1: ontology1().getClasses() ){
					//counter++;
					//System.out.println("\n" + counter + " out of 39345 operations run");
					// add mapping into alignment object 
					addAlignCell(cl1,cl2, "=", getPropSim(cl1,cl2));  
				}

			}

		} catch (Exception e) { e.printStackTrace(); }
	}
	
	

	public double getPropSim(Object o1, Object o2) throws OntowrapException, OWLOntologyCreationException {

		//get the objects (entities)
		String s1 = ontology1().getEntityName(o1).toLowerCase();
		String s2 = ontology2().getEntityName(o2).toLowerCase();

		
		//System.out.println("Matching " +  s1 + " and " + s2);
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		String ontoFile1 = ontology1().getFile().toString();
		String ontoFile2 = ontology2().getFile().toString();
		
		String ontoFile1Path = ontoFile1.replace("file:", "");
		String ontoFile2Path = ontoFile2.replace("file:", "");
		
		//System.out.println("Onto1: " + ontoFile1Path);
		//System.out.println("Onto2: " + ontoFile2Path);
		
		OWLOntology onto1 = manager.loadOntologyFromOntologyDocument(new File(ontoFile1Path));
		OWLOntology onto2 = manager.loadOntologyFromOntologyDocument(new File(ontoFile2Path));
		
		Set<String> props1 = getProperties(onto1, s1);
		Set<String> props2 = getProperties(onto2, s2);
		
//		System.out.println("\nProps (" + props1.size() + ") for : " + s1);
//		for (String s : props1) {
//			System.out.println(s);
//		}
//		
//		System.out.println("Props (" + props2.size() + ") for : " + s2);
//		for (String s : props2) {
//			System.out.println(s);
//		}
		
		
		double jaccardSim = jaccardSetSim(props1, props2);
		double measure = 0;

		//System.out.println("\nThe jaccardSim is " + jaccardSim);
		
		if (jaccardSim > 0 && jaccardSim <= 1.0) {
			measure = jaccardSim;
		} else {
			measure = 0;
		}
		return measure;

	}
	
	private static Set<String> getProperties(OWLOntology onto, String clsString) {
		
		Set<OWLClass> allClasses = onto.getClassesInSignature();		

		
		Set<String> ops = new HashSet<String>();
		Set<String> dps = new HashSet<String>();
		
		for (OWLClass cls : allClasses) {
			if (cls.getIRI().getFragment().toLowerCase().equals(clsString)) {
			
				for (OWLObjectPropertyDomainAxiom op : onto.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
				if (op.getDomain().equals(cls)) {
					for (OWLObjectProperty oop : op.getObjectPropertiesInSignature()) {
						ops.add(oop.getIRI().getFragment().substring(oop.getIRI().getFragment().lastIndexOf("-") +1));
					}
				}
			}
			
			for (OWLDataPropertyDomainAxiom dp : onto.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN)) {
				if (dp.getDomain().equals(cls)) {
					for (OWLDataProperty odp : dp.getDataPropertiesInSignature()) {
						dps.add(odp.getIRI().getFragment().substring(odp.getIRI().getFragment().lastIndexOf("-") +1));
					}
				}
			}
			
			}
		}
		
		//merge all object and data properties into one set
		Set<String> props = new HashSet<String>();
		props.addAll(ops);
		props.addAll(dps);
		
		return props;
		
	}
	
	public static double jaccardSetSim (Set<String> set1, Set<String> set2) {
		

		int intersection = 0;
		
		for (String s1 : set1) {
			for (String s2 : set2) {
				if (s1.equals(s2)) {
					intersection += 1;
				}
			}
		}

		int union = (set1.size() + set2.size()) - intersection;
		
		double jaccardSetSim = (double) intersection / (double) union;
		
		return jaccardSetSim;
	}
	
	//Uses ISub for approximating string equality
	public static double modifiedJaccardSetSim (Set<String> set1, Set<String> set2) {
		
		int intersection = 0;
		
		for (String s1 : set1) {
			for (String s2 : set2) {
				if (isub.score(s1, s2) > threshold) {
					intersection += 1;
				}
			}
		}

		int union = (set1.size() + set2.size()) - intersection;
		
		double jaccardSetSim = (double) intersection / (double) union;
		
		return jaccardSetSim;
	}
	
	
	

}
