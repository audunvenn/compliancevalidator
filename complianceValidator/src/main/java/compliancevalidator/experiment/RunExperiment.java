package compliancevalidator.experiment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Properties;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

//import compliancevalidator.combination.ParallelComposition;
//import compliancevalidator.combination.SequentialComposition;
import compliancevalidator.graph.GraphCreator;
import compliancevalidator.matchers.equivalence.CodeListMatcher;
import compliancevalidator.matchers.equivalence.DefinitionsMatcher;
import compliancevalidator.matchers.equivalence.ISubMatcher;
import compliancevalidator.matchers.equivalence.PropertyMatcher;
import compliancevalidator.matchers.equivalence.RangeMatcher;
import compliancevalidator.matchers.equivalence.WordNetSynMatcher;
import compliancevalidator.matchers.subsumption.ClosestParentMatcher;
import compliancevalidator.matchers.subsumption.CompoundMatcher;
import compliancevalidator.matchers.subsumption.DefinitionsSubsMatcher;
import compliancevalidator.misc.StringUtilities;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;

/**
 * @author audunvennesland
 * 13. nov. 2017 
 */
public class RunExperiment {

	//final static double threshold = 0.95;
	final static File datasetDir = new File("./files/KEOD18/datasets_refined/d1/alignments/equivalence");
	final static String prefix = "file:";
	
	final static File onto1 = new File("./files/KEOD18/datasets_refined/d1/ontologies/aixm_airportheliport.owl");
	final static File onto2 = new File("./files/KEOD18/datasets_refined/d1/ontologies/aerodromeinfrastructure.owl");

	//for the combination strategies
	//final static File topFolder = new File("./files/OAEI2009/alignments");


	public static void main(String[] args) throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {
		long startTime = 0;
		long stopTime = 0;
		long elapsedTime = 0;
		
		//Testing code list relations
		startTime = System.currentTimeMillis();
		System.out.println("\nRunning CodeList Matcher");
		runCodeListMatcher();
		stopTime = System.currentTimeMillis();
	    elapsedTime = stopTime - startTime;
	    System.out.println("The CodeList Matcher executed in " + elapsedTime/1000 + " seconds");
		
		
		//EQUIVALENCE MATCHERS
		
		
//		startTime = System.currentTimeMillis();
//		System.out.println("\nRunning WordNet Synonym Matcher");
//		runWNSynMatcher();
//		stopTime = System.currentTimeMillis();
//	    elapsedTime = stopTime - startTime;
//	    System.out.println("The WordNet Synonym executed in " + elapsedTime/1000 + " seconds");
//	   
//	    
//		startTime = System.currentTimeMillis();
//		System.out.println("\nRunning ISub Matcher");
//		runISubMatcher();
//		stopTime = System.currentTimeMillis();
//	    elapsedTime = stopTime - startTime;
//	    System.out.println("The ISub matcher executed in " + elapsedTime/1000 + " seconds");
//	    
//		startTime = System.currentTimeMillis();
//		System.out.println("\nRunning Property Matcher");
//		runPropertyMatcher();
//		stopTime = System.currentTimeMillis();
//	    elapsedTime = stopTime - startTime;
//	    System.out.println("The property matcher executed in " + elapsedTime/1000 + " seconds");
//
//	    startTime = System.currentTimeMillis();
//		System.out.println("\nRunning Definitions Equivalence Matcher");
//		runDefinitionsMatcher();
//		stopTime = System.currentTimeMillis();
//	    elapsedTime = stopTime - startTime;
//	    System.out.println("The definitions equivalence matcher executed in " + elapsedTime/1000 + " seconds");
//		
//		startTime = System.currentTimeMillis();
//		System.out.println("\nRunning Range Matcher");
//		runRangeMatcher();
//		stopTime = System.currentTimeMillis();
//	    elapsedTime = stopTime - startTime;
//	    System.out.println("The range matcher executed in " + elapsedTime/1000 + " seconds");
	    
//		//SUBSUMPTION MATCHERS
	
//	    startTime = System.currentTimeMillis();
//		System.out.println("\nRunning Compound Matcher");
//		runCompoundMatcher();
//		stopTime = System.currentTimeMillis();
//	    elapsedTime = stopTime - startTime;
//	    System.out.println("The Compound Matcher executed in " + elapsedTime/1000 + " seconds");
//
//		startTime = System.currentTimeMillis();
//		System.out.println("\nRunning Definitions Subsumption Matcher");
//		runDefinitionsSubsMatcher();
//		stopTime = System.currentTimeMillis();
//	    elapsedTime = stopTime - startTime;
//	    System.out.println("The Definitions Subsumption Matcher executed in " + elapsedTime/1000 + " seconds");

//		startTime = System.currentTimeMillis();
//		System.out.println("\nRunning Closest Parent Matcher");
//		runClosestParentMatcher();
//		stopTime = System.currentTimeMillis();
//	    elapsedTime = stopTime - startTime;
//	    System.out.println("The Closest Parent Matcher executed in " + elapsedTime/1000 + " seconds");
//	
//		startTime = System.currentTimeMillis();
//		System.out.println("\nRunning Ancestor Matcher");
//		runAncestorMatcher();
//		stopTime = System.currentTimeMillis();
//	    elapsedTime = stopTime - startTime;
//	    System.out.println("The Ancestor Matcher executed in " + elapsedTime/1000 + " seconds");

		//System.out.println("\nRunning OppositeSubclass Matcher");
		//runOppositeSubclassMatcher();

//		System.out.println("\nRunning WNHyponym Matcher");
//		runWNHyponymMatcher();

//		//run combinations
//		System.out.println("\nCombination strategy: Weighted Sequential Combination");
//		runWeightedSequentialCombination();
//
//		System.out.println("\nCombination strategy: Parallel Priority");
//		runParallelPriority();
//
//		System.out.println("\nCombination strategy: Parallel Simple Vote");
//		runParallelSimpleVote();

		//------------perform evaluation
		
	}
	
	private static void runCodeListMatcher() throws AlignmentException, URISyntaxException, IOException {

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		String alignmentFileName = null;
		File outputAlignment = null;

			AlignmentProcess a = new CodeListMatcher();

			System.out.println("Matching " + onto1 + " and " + onto2 );
			a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			alignmentFileName = datasetDir + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-CodeListMatcher.rdf";

			outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			

		System.out.println("\nCodeListMatcher completed!");
	}
	
	private static void runWNSynMatcher() throws AlignmentException, URISyntaxException, IOException {

		//File[] filesInDir = ontologyDir.listFiles();
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		double[] thresholds = {0.5, 0.7, 0.8, 0.9, 0.95};
		String alignmentFileName = null;
		File outputAlignment = null;

			AlignmentProcess a = new WordNetSynMatcher();

			System.out.println("Matching " + onto1 + " and " + onto2 );
			a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			for (int i = 0; i < thresholds.length; i++) {

			alignmentFileName = datasetDir + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-WNSyn"+thresholds[i]+".rdf";

			outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(thresholds[i]);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			}

		System.out.println("\nWordNet Synonym Matcher completed!");
	}
	
	private static void runISubMatcher() throws AlignmentException, URISyntaxException, IOException {

		//File[] filesInDir = ontologyDir.listFiles();
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		double[] thresholds = {0.5, 0.7, 0.9, 0.95};
		String alignmentFileName = null;
		File outputAlignment = null;

			AlignmentProcess a = new ISubMatcher();

			System.out.println("Matching " + onto1 + " and " + onto2 );
			a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			for (int i = 0; i < thresholds.length; i++) {

			alignmentFileName = datasetDir + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-ISub"+thresholds[i]+".rdf";

			outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(thresholds[i]);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			}

		System.out.println("\nISub matcher completed!");
	}
	
	private static void runRangeMatcher() throws AlignmentException, URISyntaxException, IOException {

		//File[] filesInDir = ontologyDir.listFiles();
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		double[] thresholds = {0.5, 0.7, 0.9, 0.95};
		String alignmentFileName = null;
		File outputAlignment = null;

			AlignmentProcess a = new RangeMatcher();

			System.out.println("Matching " + onto1 + " and " + onto2 );
			a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			for (int i = 0; i < thresholds.length; i++) {

			alignmentFileName = datasetDir + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-RangeMatcher"+thresholds[i]+".rdf";

			outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(thresholds[i]);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			}

		System.out.println("\nRange matcher completed!");
	}
	
	private static void runDefinitionsMatcher() throws AlignmentException, URISyntaxException, IOException {

		//File[] filesInDir = ontologyDir.listFiles();
		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		double[] thresholds = {0.5, 0.7, 0.9, 0.95};
		String alignmentFileName = null;
		File outputAlignment = null;


			AlignmentProcess a = new DefinitionsMatcher();

			System.out.println("Matching " + onto1 + " and " + onto2 );
			a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			for (int i = 0; i < thresholds.length; i++) {
			alignmentFileName = datasetDir + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-DefinitionsMatcher"+thresholds[i]+".rdf";

			outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(thresholds[i]);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			}

		System.out.println("\nDefinitions matcher completed!");
	}
	
	private static void runDefinitionsSubsMatcher() throws AlignmentException, URISyntaxException, IOException {


		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		double[] thresholds = {0.5, 0.7, 0.9, 0.95};
		String alignmentFileName = null;
		File outputAlignment = null;

			AlignmentProcess a = new DefinitionsSubsMatcher();

			System.out.println("Matching " + onto1 + " and " + onto2 );
			a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			for (int i = 0; i < thresholds.length; i++) {

			alignmentFileName = datasetDir + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-DefinitionsSubsumptionMatcher"+thresholds[i]+".rdf";

			outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(thresholds[i]);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			}
			
		System.out.println("\nDefinitions subsumption matcher completed!");
	}
	
	private static void runPropertyMatcher() throws AlignmentException, URISyntaxException, IOException {

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		double[] thresholds = {0.5, 0.7, 0.9, 0.95};
		String alignmentFileName = null;
		File outputAlignment = null;
		
			AlignmentProcess a = new PropertyMatcher();

			System.out.println("Matching " + onto1 + " and " + onto2 );
			a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			
			for (int i = 0; i < thresholds.length; i++) {

			alignmentFileName = datasetDir + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-PropertyMatcher"+thresholds[i]+".rdf";

			outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(thresholds[i]);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			}

		System.out.println("\nProperty matcher completed!");
	}

	
	

	private static void runCompoundMatcher() throws AlignmentException, URISyntaxException, IOException {

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;	
		Properties params = new Properties();
		double[] thresholds = {0.5, 0.7, 0.9, 0.95};
		String alignmentFileName = null;
		File outputAlignment = null;
		
			AlignmentProcess a = new CompoundMatcher();

			System.out.println("Matching " + onto1 + " and " + onto2 );
			System.out.println("Init with " + prefix.concat(onto1.toString().substring(2)) + " and " +  prefix.concat(onto2.toString().substring(2)));
			a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	

			for (int i = 0; i < thresholds.length; i++) {
			alignmentFileName = datasetDir + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-Compound"+thresholds[i]+".rdf";

			outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(thresholds[i]);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			}

		System.out.println("\nCompound matcher completed!");
	}
	
	private static void runClosestParentMatcher() throws AlignmentException, URISyntaxException, IOException, OWLOntologyCreationException {

		PrintWriter writer = null;
		AlignmentVisitor renderer = null;	
		BasicAlignment evaluatedAlignment = null;

		Properties params = new Properties();
		String ontologyParameter1 = null;
		String ontologyParameter2 = null;	
		GraphCreator creator = null;
		OWLOntologyManager manager = null;
		OWLOntology o1 = null;
		OWLOntology o2 = null;
		Label labelO1 = null;
		Label labelO2 = null;
		double[] thresholds = {0.5, 0.7, 0.9, 0.95};
		String alignmentFileName = null;
		File outputAlignment = null;

			//create a new instance of the neo4j database in each run
			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String dbName = String.valueOf(timestamp.getTime());
			//final File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/test");
			File dbFile = new File("/Users/audunvennesland/Documents/phd/development/Neo4J_new/" + dbName);				
			GraphDatabaseService db = new GraphDatabaseFactory().newEmbeddedDatabase(dbFile);
			registerShutdownHook(db);

			ontologyParameter1 = StringUtilities.stripPath(onto1.toString());
			ontologyParameter2 = StringUtilities.stripPath(onto2.toString());

			//create new graphs
			manager = OWLManager.createOWLOntologyManager();
			o1 = manager.loadOntologyFromOntologyDocument(onto1);
			o2 = manager.loadOntologyFromOntologyDocument(onto2);
			labelO1 = DynamicLabel.label( ontologyParameter1 );
			labelO2 = DynamicLabel.label( ontologyParameter2 );

			creator = new GraphCreator(db);
			creator.createOntologyGraph(o1, labelO1);
			creator.createOntologyGraph(o2, labelO2);

			AlignmentProcess a = new ClosestParentMatcher(ontologyParameter1,ontologyParameter2, db);

			System.out.println("Matching " + onto1 + " and " + onto2 );
			a.init( new URI(prefix.concat(onto1.toString().substring(2))), new URI(prefix.concat(onto2.toString().substring(2))));
			params = new Properties();
			params.setProperty("", "");
			a.align((Alignment)null, params);	
			for (int i = 0; i < thresholds.length; i++) {

			alignmentFileName = datasetDir + "/" + StringUtilities.stripOntologyName(onto1.toString()) + 
					"-" + StringUtilities.stripOntologyName(onto2.toString()) + "-ClosestParent"+thresholds[i]+".rdf";

			outputAlignment = new File(alignmentFileName);


			writer = new PrintWriter(
					new BufferedWriter(
							new FileWriter(outputAlignment)), true); 
			renderer = new RDFRendererVisitor(writer);

			evaluatedAlignment = (BasicAlignment)(a.clone());

			evaluatedAlignment.normalise();

			evaluatedAlignment.cut(thresholds[i]);

			evaluatedAlignment.render(renderer);
			writer.flush();
			writer.close();
			}

		System.out.println("\nClosest Parent matcher completed!");
	}


	private static void registerShutdownHook(final GraphDatabaseService db)
	{
		Runtime.getRuntime().addShutdownHook( new Thread()
		{
			@Override
			public void run()
			{
				db.shutdown();

			}
		} );
	}

	private static String getMatcherName(String inputAlignmentName) {

		String matcherName = inputAlignmentName.substring(inputAlignmentName.lastIndexOf("-") + 1, inputAlignmentName.lastIndexOf("-") +4);

		return matcherName;
	}
	
	public static void permute(String[] arr){
	    permuteHelper(arr, 0);
	}

	private static void permuteHelper(String[] arr, int index){
	    if(index >= arr.length - 1){ //If we are at the last element - nothing left to permute
	        System.out.print("[");
	        for(int i = 0; i < arr.length - 1; i++){
	            System.out.print(arr[i] + ", ");
	        }
	        if(arr.length > 0) 
	            System.out.print(arr[arr.length - 1]);
	        System.out.println("]");
	        return;
	    }

	    for(int i = index; i < arr.length; i++){ //For each index in the sub array arr[index...end]

	        //Swap the elements at indices index and i
	        String t = arr[index];
	        arr[index] = arr[i];
	        arr[i] = t;

	        //Recurse on the sub array arr[index+1...end]
	        permuteHelper(arr, index+1);

	        //Swap the elements back
	        t = arr[index];
	        arr[index] = arr[i];
	        arr[i] = t;
	    }
	}

}
