package compliancevalidator.combination;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import compliancevalidator.misc.StringUtilities;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

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
		File aFile = new File("./files/test/Matcher1.rdf");
		File bFile = new File("./files/test/Matcher2.rdf");
		File cFile = new File("./files/test/Matcher3.rdf");

		AlignmentParser parser = new AlignmentParser();

		//parse the alignment file
		BasicAlignment origAlignment = (BasicAlignment)parser.parse(origFile.toURI().toString());
		BasicAlignment a = (BasicAlignment)parser.parse(aFile.toURI().toString());
		BasicAlignment b = (BasicAlignment)parser.parse(bFile.toURI().toString());
		BasicAlignment c = (BasicAlignment)parser.parse(cFile.toURI().toString());
		
		ArrayList<Alignment> initialAlignments = new ArrayList<Alignment>();
		
		System.out.println("Adding alignments a:" + a + ", b: " + b + " , and c: " + c + " to initialAlignments");
		
		initialAlignments.add(a);
		initialAlignments.add(b);
		initialAlignments.add(c);
		

//		System.out.println("The original alignment contains " + origAlignment.nbCells() + " cells");
//		for (Cell cl : origAlignment) {
//			System.out.println(cl.getObject1AsURI().getFragment() + "-" + cl.getObject2AsURI().getFragment());
//		}


		Alignment aHigh = getHighestCorrespondencesFromSingleAlignment(a);
		Alignment bHigh = getHighestCorrespondencesFromSingleAlignment(b);
		Alignment cHigh = getHighestCorrespondencesFromSingleAlignment(c);

		//		System.out.println("Highest correspondences:");
		//		for (Cell cl : aHigh) {
		//			System.out.println(cl.getObject1AsURI().getFragment() + "-" + cl.getObject2AsURI().getFragment());
		//		}




		System.out.println("TESTING HIGHEST CORRESPONDENCE COEFFICIENT:");
		List<Alignment> aHighList = new ArrayList<Alignment>();
		aHighList.add(aHigh);
		aHighList.add(bHigh);
		aHighList.add(cHigh);

		Map<Cell, Double> highCorrCoefficient = getHighestCorrespondencesCoefficient(aHighList);

//		//test print
//		for (Entry<Cell, Double> e : highCorrCoefficient.entrySet()) {
//			System.out.println("Cell: " + e.getKey().getObject1AsURI().getFragment() + " - " + e.getKey().getObject2AsURI().getFragment() + ", Highest Correspondence Coefficient: " + e.getValue());
//
//		}

//		System.out.println("TESTING MATCHER COEFFICIENT:");

		Map<Alignment, Double> matcherCoefficient = getMatcherCoefficient(highCorrCoefficient, aHighList);

//		//test print
//		for (Entry<Alignment, Double> e : matcherCoefficient.entrySet()) {
//			System.out.println("Alignment: " + e.getKey() + ", Matcher Coefficient: " + e.getValue());
//
//		}
		
		
		Alignment finalAlignment = runAutoweightPlusPlus(initialAlignments);
		

	}
	

	public static Alignment runAutoweightPlusPlus (ArrayList<Alignment> initialAlignments) throws AlignmentException {

		BasicAlignment finalAlignment = new URIAlignment();

		//compute highest correspondences for each alignment, and remove all correspondences below threshold
		ArrayList<Alignment> highCorrsAll = new ArrayList<Alignment>();

		System.out.println("Alignments from runAutoweightPlusPlus:");
		for (Alignment a : initialAlignments) {
			System.out.println(a);
			highCorrsAll.add(getHighestCorrespondencesFromSingleAlignment(a));
		}
		
		System.out.println("High corrs alignments:");
		
		for (Alignment a : highCorrsAll) {
			System.out.println(a);
		}

		//determine highest correspondences coefficient for each correspondence
		Map<Cell, Double> highCorrsCoefficient = getHighestCorrespondencesCoefficient(highCorrsAll);

		//determine importance coefficient for each matcher/initial alignment
		Map<Alignment, Double> matcherCoefficient = getMatcherCoefficient(highCorrsCoefficient, highCorrsAll);
		
		System.out.println("Matcher coefficient alignments:");
		
		for (Entry<Alignment, Double> e : matcherCoefficient.entrySet()) {
			System.out.println(e.getKey());
		}

		//determine matcher/initial alignment weight
		Map<Alignment, Double> matcherWeight = getMatcherWeight(matcherCoefficient);
		
		System.out.println("Matcher weight alignments:");
		
		for (Entry<Alignment, Double> e : matcherWeight.entrySet()) {
			System.out.println(e.getKey());
		}

		//produce final alignment
		double weight = 0;

		ArrayList<Alignment> initialAlignmentsWeighted = new ArrayList<Alignment>();

		//revise the strength for each cell in the initial alignment according to the matcher weight
		for (Alignment a : initialAlignments) {
			
			System.out.println("weight from alignment " + a);
			weight = matcherWeight.get(a);
			for (Cell c : a) {
				c.setStrength(c.getStrength()*weight);
			}
			initialAlignmentsWeighted.add(a);
		}


		//produce final alignment
		finalAlignment = computeFinalAlignment(initialAlignmentsWeighted);


		return finalAlignment;

	}


	public static Map<Alignment, Double> getMatcherCoefficient(Map<Cell, Double> highCorrCoefficient, List<Alignment> matchers) throws AlignmentException {

		Map<Alignment, Double> matcherCoefficients = new HashMap<Alignment, Double>();

		//produce the matcher coefficient for each matcher/alignment, where the coefficient is the sum of all highest correspondences (across all matchers) produced by the particular matcher
		for (Alignment a : matchers) {
			double matcherSum = 0;
			for (Cell c : a) {
				for (Entry<Cell, Double> e : highCorrCoefficient.entrySet()) {
					if (c.equals(e.getKey())) {
						matcherSum+=e.getValue();
					} 
				}	

			}	
			matcherCoefficients.put(a, matcherSum);
		}

		return matcherCoefficients;

	}

	public static Map<Alignment, Double> getMatcherWeight(Map<Alignment, Double> matcherCoefficient) {

		//get the total coefficient sum
		double sumCoefficients = 0;
		for (Entry<Alignment, Double> e : matcherCoefficient.entrySet()) {
			sumCoefficients += e.getValue();
		}

		Map<Alignment, Double> matcherWeightMap = new HashMap<Alignment, Double>();

		for (Entry<Alignment, Double> e : matcherCoefficient.entrySet()) {
			matcherWeightMap.put(e.getKey(), e.getValue()/sumCoefficients);
		}



		return matcherWeightMap;
	}



	/**
	 * 
	 * @param aSet a set of alignments from several matchers
	 * @return a map of 'Highest Correspondences' as key and with an important coefficient as value
	 * @throws AlignmentException 
	 */
	public static Map<Cell, Double> getHighestCorrespondencesCoefficient(List<Alignment> aSet) throws AlignmentException {

		//how many matchers (alignments) are involved?
		int numMatchers = aSet.size();

		List<Cell> allCells = new ArrayList<Cell>();

		//get all cells from all alignments into a single list
		for (Alignment a : aSet) {
			for (Cell c : a) {
				allCells.add(c);
			}
		}

		//get a Map to hold the highest correspondence coefficients
		Map<Cell, Double> highestCorrsCoefficient = new HashMap<Cell, Double>();

		int counter = 0;

		for (Cell c : allCells) {
			//count how many times (i.e. in how many alignments) correspondence c occurs
			counter = Collections.frequency(allCells, c);

			//if correspondence c is in all alignments (all matchers), it should be omitted
			if (counter != numMatchers) {
				//the coefficient is calculated as how many times correspondence c occurs / total number of alignments (matchers)
				highestCorrsCoefficient.put(c, (double)counter/numMatchers);
			}
		}

		return highestCorrsCoefficient;

	}


	/**
	 * Returns an alignment containing only highest correspondences, only correspondences having strength above 0.3 are returned
	 * @param a input alignment
	 * @return an alignment holding highest correspondences
	 * @throws AlignmentException
	 */
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

	/**
	 * Helper method that sorts the cells in an alignment based on their strenght
	 * @param a input alignment
	 * @return list of cells sorted by strength
	 * @throws AlignmentException
	 */
	private static List<Cell> sortAlignmentCellsByStrength(Alignment a) throws AlignmentException {

		List<Cell> sortedCellList = new ArrayList<Cell>();

		for (Cell c : a) {
			sortedCellList.add(c);
		}

		Collections.sort(sortedCellList);

		return sortedCellList;

	}


	public static BasicAlignment computeFinalAlignment(ArrayList<Alignment> inputAlignments) throws AlignmentException {

		BasicAlignment finalAlignment = new URIAlignment();

		ArrayList<Cell> allCells = new ArrayList<Cell>();

		// get all cells in all alignments and put them in a set
		for (Alignment a : inputAlignments) {
			// for all cells C in each input alignment
			for (Cell c : a) {
				allCells.add(c);
			}
		}

		int numAlignments = inputAlignments.size();

		ArrayList<Cell> todel = new ArrayList<Cell>();
		BasicAlignment toKeep = new URIAlignment();

		double sumStrength = 0;

		for (Cell currentCell : allCells) {

			if (!todel.contains(currentCell)) {

				// get all cells that has the same object1 as c1
				ArrayList<Cell> sameObj1 = new ArrayList<Cell>();				
				for (Cell c : allCells) {
					if (c.getObject1().equals(currentCell.getObject1())) {
						sameObj1.add(c);
					}
				}

				if (sameObj1.size() > 1) {

					// placeholder for cells that contains the same object1 and
					// object 2 as c1 AND that has the same relation type as currentCell
					ArrayList<Cell> toCheck = new ArrayList<Cell>();

					Object o2 = currentCell.getObject2();
					Relation rCurrent = currentCell.getRelation();

					//checking if the cells in sameObj1 also have the same object 2 as "currentCell", AND that their relation type is the same -> if so add the cells to "toCheck"
					for (Cell c2 : sameObj1) {
						if (o2.equals(c2.getObject2()) && rCurrent.equals(c2.getRelation())) {
							toCheck.add(c2);
						}

					}

					double sum = 0;
					//if all alignments have this cell
					if (toCheck.size() == numAlignments) {

						for (Cell c : toCheck) {
							sumStrength += c.getStrength();
							//checking that c (this cell) in fact is not currentCell
							if (c != currentCell) {

								c.setStrength(sumStrength);

								toKeep.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
								todel.add(currentCell);

							}
						}
					}

					//if not, we need to create an average of those cells that have it
					else {
						for (Cell c : toCheck) {
							sum += c.getStrength()/toCheck.size();

							if (c != currentCell) {

								c.setStrength(sumStrength);

								toKeep.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
								todel.add(currentCell);

							}


						}
					}

				} else {

				}
			}
		}


		for (Cell c : toKeep) {
			finalAlignment.addAlignCell(c.getObject1(), c.getObject2(), StringUtilities.validateRelationType(c.getRelation().getRelation()),
					c.getStrength());
		}


		return finalAlignment;
	}




}
