package compliancevalidator.statistics;

import java.io.File;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

public class AlignmentStatistics {
	
	public static void main(String[] args) throws AlignmentException {
		//File alignmentFile = new File("./files/referenceAlignments/aixm-airportheliport2airm-aerodromeinfrastructure/ref-align_aixm-airportheliport-airm-aerodromeinfrastructure.rdf");
		File alignmentFile = new File("./files/referenceAlignments/iwxxm-metar2airm-meteorology/ref-align_iwxxm-metar-airm-meteorology.rdf");
		getAlignmentStatistics(alignmentFile);
	}
	
	//get number of equivalence, subsumption relations
	public static void getAlignmentStatistics(File inputAlignmentFile) throws AlignmentException {
		
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment inputAlignment = (BasicAlignment)parser.parse(inputAlignmentFile.toURI().toString());
		
		int numTotalRelations = inputAlignment.nbCells();
		int numEquivalenceRelations = 0;
		int numSubsumptionRelations = 0;
		
		for (Cell c : inputAlignment) {
			
			if (c.getRelation().getRelation().equals("=")) {
				numEquivalenceRelations +=1;
			}
			if (c.getRelation().getRelation().equals("<") || c.getRelation().getRelation().equals(">")) {
				numSubsumptionRelations +=1;
			}
		}
		
		System.out.println("\nNumber of relations in total: " + numTotalRelations);
		System.out.println("\nNumber of equivalence relations: " + numEquivalenceRelations);
		System.out.println("\nNumber of subsumption relations: " + numSubsumptionRelations);
	}

}
