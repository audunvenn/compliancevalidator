package compliancevalidator.misc;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.OWLEntityRemover;

public class OntologyOperations {
	
	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
		File ontology = new File("./files/experiment_06032018/datasets/d2/ontologies/iwxxm_common.owl");
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		OWLOntology inputOnto = manager.loadOntologyFromOntologyDocument(ontology);
		
		System.out.println("inputOnto contains " + inputOnto.getClassesInSignature().size() + " classes");
		System.out.println("inputOnto contains " + inputOnto.getObjectPropertiesInSignature().size() + " ops");
		System.out.println("inputOnto contains " + inputOnto.getDataPropertiesInSignature().size() + " dps");
		
		OWLEntityRemover remover = new OWLEntityRemover (manager, Collections.singleton(inputOnto));
		
		Set<OWLClass> allClasses = inputOnto.getClassesInSignature();
		Set<OWLClass> classesToRemove = new HashSet<OWLClass>();
		
		for (OWLClass cls : allClasses) {
			if (cls.getIRI().getFragment().contains("_") || cls.getIRI().getFragment().contains("-")) {
				classesToRemove.add(cls);
			}
		}
		
		System.out.println("\nclassesToRemove contains (" + classesToRemove.size() + "):");
		
		for (OWLClass cls : classesToRemove) {
			//System.out.println("Removing: " + cls.getIRI().getFragment());
			cls.accept(remover);
		}
		
		manager.applyChanges(remover.getChanges());
		manager.saveOntology(inputOnto);
		
		//deleteClasses(manager, inputOnto);
		deleteObjectProperties(manager, inputOnto);
		deleteDataProperties(manager, inputOnto);
		
		System.out.println("inputOnto now contains " + inputOnto.getClassesInSignature().size() + " classes");
		System.out.println("inputOnto now contains " + inputOnto.getObjectPropertiesInSignature().size() + " ops");
		System.out.println("inputOnto now contains " + inputOnto.getDataPropertiesInSignature().size() + " dps");

	}
	
	public static void deleteClasses (OWLOntologyManager manager, OWLOntology inputOnto) throws OWLOntologyCreationException, OWLOntologyStorageException {
		
		
		OWLEntityRemover remover = new OWLEntityRemover (manager, Collections.singleton(inputOnto));
		
		Set<OWLClass> allClasses = inputOnto.getClassesInSignature();
		Set<OWLClass> classesToRemove = new HashSet<OWLClass>();
		
		for (OWLClass cls : allClasses) {
			if (cls.getIRI().getFragment().contains("_") || cls.getIRI().getFragment().contains("-")) {
				classesToRemove.add(cls);
			}
		}
		
		//System.out.println("\nclassesToRemove contains (" + classesToRemove.size() + "):");
		
		for (OWLClass cls : classesToRemove) {
			//System.out.println("Removing: " + cls.getIRI().getFragment());
			cls.accept(remover);
		}
		
		manager.applyChanges(remover.getChanges());
		manager.saveOntology(inputOnto);

		
		
	}
	
public static void deleteObjectProperties (OWLOntologyManager manager, OWLOntology inputOnto) throws OWLOntologyCreationException, OWLOntologyStorageException {
		
		
		OWLEntityRemover remover = new OWLEntityRemover (manager, Collections.singleton(inputOnto));
		
		Set<OWLObjectProperty> allOPProps = inputOnto.getObjectPropertiesInSignature();
		Set<OWLObjectProperty> oPpropsToRemove = new HashSet<OWLObjectProperty>();
		
		for (OWLObjectProperty oop : allOPProps) {
			if (oop.getIRI().getFragment().contains("_")) {
				oPpropsToRemove.add(oop);
			}
		}

		//System.out.println("\noPpropsToRemove contains (" + oPpropsToRemove.size() + "):");
		
		for (OWLObjectProperty oop : oPpropsToRemove) {
			//System.out.println("Removing: " + oop.getIRI().getFragment());
			oop.accept(remover);
		}
		
		manager.applyChanges(remover.getChanges());
		manager.saveOntology(inputOnto);
		
		
		
	}

public static void deleteDataProperties (OWLOntologyManager manager, OWLOntology inputOnto) throws OWLOntologyCreationException, OWLOntologyStorageException {
	
	
	OWLEntityRemover remover = new OWLEntityRemover (manager, Collections.singleton(inputOnto));
	
	Set<OWLDataProperty> allDPProps = inputOnto.getDataPropertiesInSignature();
	Set<OWLDataProperty> dPpropsToRemove = new HashSet<OWLDataProperty>();
	
	for (OWLDataProperty dp : allDPProps) {
		if (dp.getIRI().getFragment().contains("_")) {
			dPpropsToRemove.add(dp);
		}
	}

	//System.out.println("\ndPpropsToRemove contains (" + dPpropsToRemove.size() + "):");
	
	for (OWLDataProperty dp : dPpropsToRemove) {
		//System.out.println("Removing: " + dp.getIRI().getFragment());
		dp.accept(remover);
	}
	
	manager.applyChanges(remover.getChanges());
	manager.saveOntology(inputOnto);
		
	
}

}
