package compliancevalidator.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
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
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
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

	public static Alignment extractRewrittenRelations(File inputAlignmentFile) throws AlignmentException {

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment)parser.parse(inputAlignmentFile.toURI().toString());

		BasicAlignment rewrittenAlignment = new URIAlignment();

		URI onto1URI = inputAlignment.getOntology1URI();
		URI onto2URI = inputAlignment.getOntology2URI();

		//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
		rewrittenAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );

		for (Cell c : inputAlignment) {
			if (c.getRelation().getRelation().equals(")(")) {
				rewrittenAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
			}
		}

		return rewrittenAlignment;
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

	public static void countAlignmentCells(File inputAlignmentFile) throws AlignmentException {

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment)parser.parse(inputAlignmentFile.toURI().toString());

		System.out.println("This alignment contains: " + inputAlignment.nbCells() + " cells");
	}

	public static void computeDifferentConfidenceThresholds(String matcher, File datasetDir, File inputAlignmentFile) throws IOException, AlignmentException {

		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment)parser.parse(inputAlignmentFile.toURI().toString());

		String alignmentFileName = null;
		File outputAlignment = null;
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;
		BasicAlignment evaluatedAlignment = null;


		double[] thresholds = {0.5, 0.7, 0.9};

		for (int i = 0; i < thresholds.length; i++) {

			alignmentFileName = datasetDir + "/" + matcher + "-"+thresholds[i]+".rdf";

			outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(inputAlignment.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(thresholds[i]);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
		}

	}
	
	private static void removeCodeRelations(String inputFolderName, String outputFolderName) throws AlignmentException, URISyntaxException, IOException {
		
		AlignmentParser aparser = new AlignmentParser(0);

		File folder = new File(inputFolderName);
		File[] filesInDir = folder.listFiles();
		Alignment evaluatedAlignment = null;
		BasicAlignment processedAlignment = null;
		
		PrintWriter writer = null;
		File outputAlignment = null;
		AlignmentVisitor renderer = null;

		String alignmentName = null;

		for (int i = 0; i < filesInDir.length; i++) {
			
			processedAlignment = new URIAlignment();

			alignmentName = filesInDir[i].getName();

			String URI = StringUtilities.convertToFileURL(inputFolderName) + "/" + StringUtilities.stripPath(filesInDir[i].toString());
			System.out.println("Evaluating file " + URI);
			evaluatedAlignment = aparser.parse(new URI(URI));
			
			System.out.println("The input alignment contains " + evaluatedAlignment.nbCells() + " cells");
			
			URI onto1URI = evaluatedAlignment.getOntology1URI();
			URI onto2URI = evaluatedAlignment.getOntology2URI();
			
			//need to initialise the alignment with ontology URIs and the type of relation (e.g. A5AlgebraRelation) otherwise exceptions are thrown
			processedAlignment.init( onto1URI, onto2URI, A5AlgebraRelation.class, BasicConfidence.class );
			
			
			for (Cell c : evaluatedAlignment) {
				
				if (!c.getObject1AsURI().getFragment().startsWith("Code")) {
					processedAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
				}
			}
			
			//store the processed alignment in outputfolder
			String alignmentFileName = outputFolderName + "Rev-"+alignmentName;
			outputAlignment = new File(alignmentFileName);
			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);
			processedAlignment.render(renderer);
			
			System.out.println("The processed alignment contains " + processedAlignment.nbCells() + " cells");
			
			writer.flush();
			writer.close();

		}

	}


	public static void main(String[] args) throws OWLOntologyCreationException, AlignmentException, IOException, URISyntaxException {


		String inputFolderName = "./files/KEOD18/datasets_refined/d1/alignments/equivalence/";
		String outputFolderName = "./files/KEOD18/datasets_refined/d1/alignments/equivalence/";
		
		removeCodeRelations(inputFolderName, outputFolderName);

//		String matcher = "AML";
//		File dataDir = new File("./files/KEOD18/datasets_refined/d1/AMLNew");
//				
//		File alignmentFile = new File("./files/KEOD18/datasets_refined/d1/AMLNew/AML-05.rdf");
//				
//		computeDifferentConfidenceThresholds(matcher, dataDir, alignmentFile);



		//File alignmentFile = new File("./files/inputAlignments/ref-align_aixm-airportheliport-airm-aerodromeinfrastructure-Rewritten.rdf");

		//countAlignmentCells(alignmentFile);

		//***** EXTRACT REFERENCE ALIGNMENT FROM TRANSFORMED REFERENCE ALIGNMENT ****
		/*
		File alignmentFile = new File("./files/inputAlignments/ref-align_aixm-airportheliport-airm-aerodromeinfrastructure-RewrittenComplete.rdf");
		File ontologyFile = new File("./files/ontologies/airm/aerodromeinfrastructure.owl");

		String onto1 = "aixm-airportheliport";
		String onto2 = "airm-aerodromeinfrastructure";

		Alignment extractedAlignment = extractAlignment(alignmentFile, ontologyFile);

		System.out.println("The extracted alignment contains " + extractedAlignment.nbCells() + " cells");

		//store the alignment
		String alignmentFileName = "./files/inputAlignments/ref-align_" + onto1 + "-" + onto2 + "-RewrittenComplete.rdf";
		File outputAlignment = new File(alignmentFileName);
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);
		extractedAlignment.render(renderer);
		writer.flush();
		writer.close();
		 */


		/*
		//***** EXTRACT SUBSUMPTION, REWRITTEN OR EQUIVALENCE REFERENCE ALIGNMENT FROM COMBINED REFERENCE ALIGNMENT ****
		File alignmentFile = new File("./files/inputAlignments/AIXM2AIRM.rdf");

		Alignment extractedAlignment = extractEquivalenceRelations(alignmentFile);

		System.out.println("The extracted alignment contains " + extractedAlignment.nbCells() + " relations");

		String relationType = "Equivalence";

		String onto1 = "aixm";
		String onto2 = "airm";


		//store the alignment
		String alignmentFileName = "./files/inputAlignments/ref-align_" + onto1 + "-" + onto2 + "-" + relationType + ".rdf";
		File outputAlignment = new File(alignmentFileName);
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(
						new FileWriter(outputAlignment)), true); 
		AlignmentVisitor renderer = new RDFRendererVisitor(writer);
		extractedAlignment.render(renderer);
		writer.flush();
		writer.close();
		 */
	}


}
