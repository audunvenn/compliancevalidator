package compliancevalidator.combination;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;

import compliancevalidator.misc.StringUtilities;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;


public class SequentialComposition {

	static double threshold;
	static File outputAlignment = null;

	/**
	 * Returns an alignment where correspondences that are identified both by the previous matcher and the current matcher are strengthened. 
	 * This combination strategy considers the order of the alignments (i.e. only the first matcher (get(0)) in the ArrayList
	 *  
	 * @param inputAlignments an ArrayList of all alignments to be combined
	 * @return an alignment with weighted correspondences
	 * @throws AlignmentException
	 */
	public static Alignment weightedSequentialComposition(ArrayList<Alignment> inputAlignments) throws AlignmentException {

		Alignment newAlignment = new URIAlignment();

		//set the first alignment in the array list as "prioritised alignment" and remove it from the arraylist
		Alignment priAlignment = inputAlignments.get(0);
		
		//create a list of cells from the "prioritised alignment"
		ArrayList<Cell> priCellsList = new ArrayList<Cell>();
		for (Cell c : priAlignment) {
			priCellsList.add(c);
		}
		
		//create a list of cells from the other alignments
		ArrayList<Cell> allOtherCellsList = new ArrayList<Cell>();		
		for (Alignment a : inputAlignments) {
			for (Cell c : a) {
				allOtherCellsList.add(c);
			}
		}
		
		//map to hold number of occurrences of each cell from the prioritised alignment in the other alignments
		Map<Cell, Integer> cellCountMap = new HashMap<Cell, Integer>();

		for (Cell c1 : priCellsList) {
			int counter = 0;
			for (Cell c2: allOtherCellsList) {
				if (c2.equals(c1)) {
					counter+=1;
				} 
				
			}
			cellCountMap.put(c1, counter);
		}
		
		//System.out.println("Printing cellCountMap");
		
//		for (Entry<Cell, Integer> entry : cellCountMap.entrySet()) {
//			System.out.println(entry.getKey().getObject1AsURI().getFragment() + " - " + entry.getKey().getObject2AsURI().getFragment() + " : " + entry.getKey().getRelation().getRelation() + " = " + entry.getValue());
//		}
		
		for (Entry<Cell, Integer> e : cellCountMap.entrySet()) {
			//if no other alignments have this cell -> reduce its confidence by 50 percent
			if (e.getValue() == (0)) {
				newAlignment.addAlignCell(e.getKey().getObject1(), e.getKey().getObject2(), StringUtilities.validateRelationType(e.getKey().getRelation().getRelation()), reduceCellStrength(e.getKey().getStrength()));
				//if one other alignment have this cell
			} else if (e.getValue() == (1)) {
				newAlignment.addAlignCell(e.getKey().getObject1(), e.getKey().getObject2(), StringUtilities.validateRelationType(e.getKey().getRelation().getRelation()), e.getKey().getStrength()-0.2);
			//if two other alignments have this cell
			} else if (e.getValue() == (2)) {
				newAlignment.addAlignCell(e.getKey().getObject1(), e.getKey().getObject2(), StringUtilities.validateRelationType(e.getKey().getRelation().getRelation()), e.getKey().getStrength());
			//if all other alignments have this cell, give it 100 percent confidence
			} else if (e.getValue() == (3)) {
				newAlignment.addAlignCell(e.getKey().getObject1(), e.getKey().getObject2(), StringUtilities.validateRelationType(e.getKey().getRelation().getRelation()), 1.0);
			}
		}
		

		//remove duplicates before returning the completed alignment
		((BasicAlignment) newAlignment).normalise();

		//test
		//System.err.println("newAlignment now contains " + newAlignment.nbCells() + " cells");

		return newAlignment;		
	}
	

	/**
	 * Increases an input value by 12 percent
	 * @param inputStrength the strength/confidence for a correspondence to be increased
	 * @return a value 12 percent higher than its input value
	 */
	public static double increaseCellStrength(double inputStrength) {

		double newStrength = inputStrength + (inputStrength * 0.12);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}

	/**
	 * Decreases an input value by 12 percent
	 * @param inputStrength the strength/confidence for a correspondence to be decreased
	 * @return a value 12 percent lower than its input value
	 */
	public static double reduceCellStrength(double inputStrength) {

		double newStrength = inputStrength - (inputStrength * 0.12);

		if (newStrength > 1.0) {
			newStrength = 1.0;
		}

		return newStrength;
	}


}
