package compliancevalidator.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.rel.A5AlgebraRelation;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * @author audunvennesland
 * 30. des. 2017 
 */
public class AlignmentOperations {

	public static Alignment extractEquivalenceRelations(File inputAlignmentFile) throws AlignmentException {

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment)parser.parse(inputAlignmentFile.toURI().toString());

		BasicAlignment equivalenceAlignment = new URIAlignment();
		
		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		equivalenceAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		for (Cell c : inputAlignment) {
			if (c.getRelation().getRelation().equals("=")) {
				equivalenceAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
			}
		}

		return equivalenceAlignment;
	}

	public static Alignment extractSubsumptionRelations(File inputAlignmentFile) throws AlignmentException {

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment)parser.parse(inputAlignmentFile.toURI().toString());

		BasicAlignment subsumptionAlignment = new URIAlignment();
		
		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		subsumptionAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		for (Cell c : inputAlignment) {
			if (c.getRelation().getRelation().equals("<") || c.getRelation().getRelation().equals(">")) {
				subsumptionAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
			}
		}

		return subsumptionAlignment;
	}

	/**
	 * Prints a BasicAlignment to file
	 * @param alignmentFileName the filename of the printed BasicAlignment
	 * @param alignment an input BasicAlignment object
	 * @throws IOException
	 * @throws AlignmentException
	 */
	public static void printAlignment(String alignmentFileName, BasicAlignment alignment) throws IOException, AlignmentException {

		File alignmentFile = new File(alignmentFileName);

		System.err.println("The alignmentFile is " + alignmentFile);
		System.err.println("The alignment contains " + alignment.nbCells() + " cells");
		System.err.println(alignment.getOntology1URI() + " - " + alignment.getOntology2URI());

		PrintWriter pw = new PrintWriter(
				new BufferedWriter(
						new FileWriter(alignmentFile)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(pw);
		alignment.render(renderer);
		pw.close();

	}

	/**
	 * Ensures that the entities in a cell are represented in the same order as the ontology URIs
	 * @param inputAlignment
	 * @return a BasicAlignment with entities in the correct order
	 * @throws AlignmentException
	 */
	public static URIAlignment fixEntityOrder (BasicAlignment inputAlignment) throws AlignmentException {
		URIAlignment newReferenceAlignment = new URIAlignment();

		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();

		URI entity1 = null;
		URI entity2 = null;
		String relation = null;
		double threshold = 1.0;

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		newReferenceAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		for (Cell c : inputAlignment) {
			if (c.getObject1AsURI().toString().contains(onto1URI.toString())) {
				entity1 = c.getObject1AsURI();
				entity2 = c.getObject2AsURI();
				relation = c.getRelation().getRelation();
				newReferenceAlignment.addAlignCell(entity1, entity2, relation, threshold);

			} else if (c.getObject2().toString().contains(onto1URI.toString())) {
				System.out.println(c.getObject2AsURI());
				entity1 = c.getObject2AsURI();
				entity2 = c.getObject1AsURI();
				relation = c.getRelation().getRelation();

				if (relation.equals(">")) {
					relation = "<";
				} else if (relation.equals("<")) {
					relation = ">";
				} else {
					relation = "=";
				}

				newReferenceAlignment.addAlignCell(entity1, entity2, relation, threshold);

			}

		}


		return newReferenceAlignment;
	}

	/**
	 * Extract a (reference) alignment given the entities contained in an ontology module. 
	 * @param inputAlignment
	 * @return
	 * @throws AlignmentException
	 * @throws OWLOntologyCreationException 
	 */
	public static Alignment extractAlignment(File inputAlignmentFile, File inputOntologyFile) throws AlignmentException, OWLOntologyCreationException {

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment)parser.parse(inputAlignmentFile.toURI().toString());
		System.out.println("The input alignment contains " + inputAlignment.nbCells() + " cells");


		URIAlignment extractedAlignment = new URIAlignment();

		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology inputOntology = manager.loadOntologyFromOntologyDocument(inputOntologyFile);

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		extractedAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		Set<OWLClass> classes = inputOntology.getClassesInSignature();
		System.out.println("Printing class entities");
		for (OWLClass cl : classes) {
			System.out.println(cl.getIRI());

		}

		System.out.println("Printing alignment entities");
		for (Cell c : inputAlignment) {
			for (OWLClass cl : classes) {
				System.err.println("Trying " + c.getObject1() + " and " + cl.getIRI().getFragment());
				if (c.getObject1AsURI().getFragment().equals(cl.getIRI().getFragment())) {
					System.err.println("We have a match " + c.getObject1() + " and " + cl.getIRI());
					extractedAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
				}
			}
		}



		return extractedAlignment;

	}

	public static void main(String[] args) throws OWLOntologyCreationException, AlignmentException, IOException {

		//***** EXTRACT REFERENCE ALIGNMENT FROM TRANSFORMED REFERENCE ALIGNMENT ****
		 
		File alignmentFile = new File("./files/referenceAlignments/AIXM_Compliance_Mapping-Evidence_for_AIRM_Compliance_L1.rdf");
		File ontologyFile = new File("./files/ontologies/aixm/aixm_shared.owl");

		String onto1 = "aixm-shared";
		String onto2 = "airm-mono";

		Alignment extractedAlignment = extractAlignment(alignmentFile, ontologyFile);

		System.out.println("The extracted alignment contains " + extractedAlignment.nbCells() + " cells");

		//store the alignment
		String alignmentFileName = "./files/referenceAlignments/ref-align_" + onto1 + "-" + onto2 + ".rdf";
		File outputAlignment = new File(alignmentFileName);
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);
		extractedAlignment.render(renderer);
		writer.flush();
		writer.close();
		
		

		
		//***** EXTRACT SUBSUMPTION OR EQUIVALENCE REFERENCE ALIGNMENT FROM COMBINED REFERENCE ALIGNMENT ****
		/*File alignmentFile = new File("./files/experiment_06032018/datasets/d1/refalign/ref-align_aixm-airportheliport-airm-aerodromeinfrastructure.rdf");
		
		Alignment extractedAlignment = extractEquivalenceRelations(alignmentFile);
		
		System.out.println("The extracted alignment contains " + extractedAlignment.nbCells() + " relations");
		
		String relationType = "Equivalence";

		String onto1 = "aixm-airportheliport";
		String onto2 = "airm-aerodromeinfrastructure";


		//store the alignment
		String alignmentFileName = "./files/referenceAlignments/ref-align_" + onto1 + "-" + onto2 + "-" + relationType + ".rdf";
		File outputAlignment = new File(alignmentFileName);
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);
		extractedAlignment.render(renderer);
		writer.flush();
		writer.close();*/

	}


}
