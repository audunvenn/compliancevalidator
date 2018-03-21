package compliancevalidator.combination;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import compliancevalidator.misc.StringUtilities;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicConfidence;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontosim.*;


import compliancevalidator.combination.ExtendedCell;

/*
 * 1. Identify highest correspondences for each matcher (alignment)
 * 2. Remove highest correspondences below a threshold (e.g. 0.3)
 * 
 * 
 * 
 * 
 * 
 */

public class AutoWeight {
	
	public static void main(String[] args) throws AlignmentException, URISyntaxException {
		File origFile = new File("./files/test/ISub.rdf");
		File aFile = new File("./files/test/ISub1.rdf");
		File bFile = new File("./files/test/ISub2.rdf");
		File cFile = new File("./files/test/ISub3.rdf");
		
		AlignmentParser parser = new AlignmentParser();

		//parse the alignment file
		BasicAlignment origAlignment = (BasicAlignment)parser.parse(origFile.toURI().toString());
		BasicAlignment a = (BasicAlignment)parser.parse(aFile.toURI().toString());
		BasicAlignment b = (BasicAlignment)parser.parse(bFile.toURI().toString());
		BasicAlignment c = (BasicAlignment)parser.parse(cFile.toURI().toString());

		System.out.println("The original alignment contains " + origAlignment.nbCells() + " cells");
		for (Cell cl : origAlignment) {
			System.out.println(cl.getObject1AsURI().getFragment() + "-" + cl.getObject2AsURI().getFragment());
		}
		
		
		Alignment newAlignment = getHighestCorrespondencesFromSingleAlignment(origAlignment);
		System.out.println("Highest correspondences:");
		for (Cell cl : newAlignment) {
			System.out.println(cl.getObject1AsURI().getFragment() + "-" + cl.getObject2AsURI().getFragment());
		}
		

	}
	


    public static Alignment getHighestCorrespondencesFromSingleAlignment(Alignment a) throws AlignmentException {
    	
    	//get a sorted (by strength) representation of the cells in alignment a
    	List<Cell> sortedCells = sortAlignmentCellsByStrength(a);
		
    	double confidenceThreshold = 0.30;

		Set<Cell> todel = new HashSet<Cell>();
		Set<Cell> toKeep = new HashSet<Cell>();
		BasicAlignment highestCorrAlignment = new URIAlignment();
		
		BasicAlignment aCopy = (BasicAlignment) a.clone();
		
		//can omit all correspondences not satisfying the confidence threshold
		aCopy.cut(confidenceThreshold);
		
		for (Cell currentCell : sortedCells) {

			if (!todel.contains(currentCell)) {
				//get all cells that has the same object1 as c1 OR the same object2 as c1
				Set<Cell> cells2 = a.getAlignCells1(currentCell.getObject1());
				cells2.addAll(a.getAlignCells2(currentCell.getObject2()));

				if (cells2.size() > 1) {
	
						//these are the cells that contain the same object1 or object2 as the current cell
						for (Cell c : cells2) {
							
							//only as long as their strengths are not equal (if their strengths are equal we omit both cells)
							//this is according to the CroMatcher paper, I don´t understand why they would potentially omit "good relations" this way...
							if (c.getStrength() != currentCell.getStrength()) {
							
							//check if the relations match
							if (c != currentCell && c.getRelation().equals(currentCell.getRelation())) {
								
								if (c != currentCell && c.getStrength() > currentCell.getStrength()) {
									toKeep.add(c);
									todel.add(currentCell);
								} else {
									toKeep.add(currentCell);
									todel.add(c);
								}
								
							}
							}
						}
					
				} else {
					//if the current cell is unique, that is, the only cell with this combination of object1 and object2, we keep it
					toKeep.add(currentCell);
				}
			}

		}
		//move all cells "to keep" over to the alignment
		for (Cell c : toKeep) {
			highestCorrAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().toString(), c.getStrength());
		}

		//remove duplicate cells (if any)
		highestCorrAlignment.normalise();

	return highestCorrAlignment;
		
	}
    
    public static List<Cell> sortAlignmentCellsByStrength(Alignment a) throws AlignmentException {
		
		List<Cell> sortedCellList = new ArrayList<Cell>();
		
		for (Cell c : a) {
			sortedCellList.add(c);
		}
		
		Collections.sort(sortedCellList);
		
		return sortedCellList;
		
	}
	
    /**
	 * 
	 * @param aSet a set of alignments from several matchers
	 * @return a set of 'Highest Correspondences' with an important coefficient
	 * @throws AlignmentException 
	 */
	public static Set<ExtendedCell> getHighestCorrespondencesFromAllAlignments(List<Alignment> aSet) throws AlignmentException {
		
		Set<ExtendedCell> highestCorrs = new HashSet<ExtendedCell>();
		
		List<Cell> allCells = new ArrayList<Cell>();
		
		//get all cells from all alignments into a single set
		for (Alignment a : aSet) {
			for (Cell c : a) {
				allCells.add(c);
			}
		}
		
		//make a copy of allCells
		List<Cell> allCellsCopy = new ArrayList<Cell>();
		allCellsCopy.addAll(allCells);
		
		//get a Map of how many times a cell c occurs
		Map<Cell, Integer> cellCounterMap = new HashMap<Cell, Integer>();
		
		int counter = 0;
		
		for (Cell c : allCellsCopy) {
			System.out.println("Freq of c: "+Collections.frequency(allCells, c));
			counter = Collections.frequency(allCells, c);
			cellCounterMap.put(c, counter);
		}
	
		
		for (Entry<Cell, Integer> e : cellCounterMap.entrySet()) {
			System.out.println(e.getKey().getObject1AsURI().getFragment() + " - " + e.getKey().getObject2AsURI().getFragment() + ": " + e.getValue());
		}
		
		
		
		return highestCorrs;
		
	}
	
/*	public static Alignment getHighestCorrespondencesFromSingleAlignment(Alignment a) throws AlignmentException {

		double confidenceThreshold = 0.30;

		Set<Cell> todel = new HashSet<Cell>();
		Set<Cell> toKeep = new HashSet<Cell>();
		BasicAlignment highestCorrAlignment = new URIAlignment();
		
		BasicAlignment aCopy = (BasicAlignment) a.clone();
		
		//can omit all correspondences not satisfying the confidence threshold
		aCopy.cut(confidenceThreshold);
		
		for (Cell currentCell : aCopy) {

			if (!todel.contains(currentCell)) {
				//get all cells that has the same object1 as c1 OR the same object2 as c1
				Set<Cell> cells2 = a.getAlignCells1(currentCell.getObject1());
				cells2.addAll(a.getAlignCells2(currentCell.getObject2()));

				//has to be larger than 1 otherwise we loose those cells that don´t have any entities in other cells
				if (cells2.size() > 1) {
					
						System.out.println("currentCell is " + currentCell.getObject1AsURI().getFragment() + " - " + currentCell.getObject2AsURI().getFragment());

						//these are the cells that contain the same object1 or object2 as the current cell
						for (Cell c : cells2) {
							
							//only as long as their strengths are not equal (if their strengths are equal we omit both cells)
							//this is according to the CroMatcher paper, I don´t understand why they would potentially omit "good relations" this way...
							if (c.getStrength() != currentCell.getStrength()) {
							
							//check if the relations match
							if (c != currentCell && c.getRelation().equals(currentCell.getRelation())) {
								
								if (c != currentCell && c.getStrength() > currentCell.getStrength()) {
									toKeep.add(c);
									System.out.println("Adding " + c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " to toKeep");
									todel.add(currentCell);
								} else {
									toKeep.add(currentCell);
									System.out.println("Adding " + currentCell.getObject1AsURI().getFragment() + " - " + currentCell.getObject2AsURI().getFragment() + " to toKeep");
									todel.add(c);
								}
								
							}
							}
						}
					
				} else {
					//if the current cell is unique, that is, the only cell with this combination of object1 and object2, we keep it
					toKeep.add(currentCell);
				}
			}

		}
		//move all cells "to keep" over to the alignment
		for (Cell c : toKeep) {
			highestCorrAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().toString(), c.getStrength());
		}

		//remove duplicate cells if any
		highestCorrAlignment.normalise();
		
		
		
		//do another check to see if there are more than one correspondence with the same entity 1 or entity 2
		//Set<Cell> ent1 = highestCorrAlignment.getAlignCells1(highestCorrAlignment);
		//Set<Cell> ent2 = highestCorrAlignment.getAlignCells2(highestCorrAlignment);
		
		
		

	return highestCorrAlignment;

}*/

	
	/*public static Alignment getHighestCorrespondencesFromSingleAlignment(Alignment a) throws AlignmentException {

			double confidenceThreshold = 0.30;

			Set<Cell> todel = new HashSet<Cell>();
			Set<Cell> toKeep = new HashSet<Cell>();
			BasicAlignment highestCorrAlignment = new URIAlignment();
			
			BasicAlignment aCopy = (BasicAlignment) a.clone();
			
			//can omit all correspondences not satisfying the confidence threshold
			aCopy.cut(confidenceThreshold);
			
			for (Cell currentCell : aCopy) {

				if (!todel.contains(currentCell)) {
					//get all cells that has the same object1 as c1 OR the same object2 as c1
					Set<Cell> cells2 = a.getAlignCells1(currentCell.getObject1());
					cells2.addAll(a.getAlignCells2(currentCell.getObject2()));

					//has to be larger than 1 otherwise we loose those cells that don´t have any entities in other cells
					if (cells2.size() > 1) {
						
							System.out.println("currentCell is " + currentCell.getObject1AsURI().getFragment() + " - " + currentCell.getObject2AsURI().getFragment());

							//these are the cells that contain the same object1 or object2 as the current cell
							for (Cell c : cells2) {
								
								//only as long as their strengths are not equal (if their strengths are equal we omit both cells)
								//this is according to the CroMatcher paper, I don´t understand why they would potentially omit "good relations" this way...
								if (c.getStrength() != currentCell.getStrength()) {
								
								//check if the relations match
								if (c != currentCell && c.getRelation().equals(currentCell.getRelation())) {
									
									if (c != currentCell && c.getStrength() > currentCell.getStrength()) {
										toKeep.add(c);
										System.out.println("Adding " + c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " to toKeep");
										todel.add(currentCell);
									} else {
										toKeep.add(currentCell);
										System.out.println("Adding " + currentCell.getObject1AsURI().getFragment() + " - " + currentCell.getObject2AsURI().getFragment() + " to toKeep");
										todel.add(c);
									}
									
								}
								}
							}
						
					} else {
						//if the current cell is unique, that is, the only cell with this combination of object1 and object2, we keep it
						toKeep.add(currentCell);
					}
				}

			}
			//move all cells "to keep" over to the alignment
			for (Cell c : toKeep) {
				highestCorrAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().toString(), c.getStrength());
			}

			//remove duplicate cells if any
			highestCorrAlignment.normalise();

		return highestCorrAlignment;

	}*/
	

	
	

}
