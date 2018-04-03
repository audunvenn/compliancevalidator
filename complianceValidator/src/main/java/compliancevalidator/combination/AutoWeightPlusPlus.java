package compliancevalidator.combination;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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


public class AutoWeightPlusPlus {

	public static void main(String[] args) throws AlignmentException, URISyntaxException {
		File aFile = new File("./files/test/M1.rdf");
		File bFile = new File("./files/test/M2.rdf");
		File cFile = new File("./files/test/M3.rdf");

		AlignmentParser parser = new AlignmentParser();

		//parse the alignment file
		BasicAlignment a = (BasicAlignment)parser.parse(aFile.toURI().toString());
		BasicAlignment b = (BasicAlignment)parser.parse(bFile.toURI().toString());
		BasicAlignment c = (BasicAlignment)parser.parse(cFile.toURI().toString());

		
		ArrayList<BasicAlignment> initialAlignments = new ArrayList<BasicAlignment>();

		initialAlignments.add(a);
		initialAlignments.add(b);
		initialAlignments.add(c);


		BasicAlignment finalAlignment = runAutoweightPlusPlus(initialAlignments);

	}

	public static BasicAlignment runAutoweightPlusPlus(ArrayList<BasicAlignment> initialAlignments) throws AlignmentException {
		
		BasicAlignment finalAlignment = new URIAlignment();
		
		ArrayList<BasicAlignment> highestCorrs = new ArrayList<BasicAlignment>();
		
		ArrayList<BasicAlignment> initialsClone = new ArrayList<BasicAlignment>(initialAlignments);
		
		//clone the initialAlignments list, otherwise the references are messed up
		for (BasicAlignment b : initialsClone) {
			BasicAlignment bClone = (BasicAlignment) b.clone();
			highestCorrs.add(getHighestCorrespondencesFromSingleAlignment(bClone));
		}

		//test print highest correspondences for each alignment
//		int count = 1;
//		
//		for (BasicAlignment ba : highestCorrs) {
//			System.out.println("Alignment " + count++);
//			for (Cell c : ba) {
//				System.out.println(c.getObject1() + " - " + c.getObject2() + " : " + c.getStrength());
//			}
//		}
		//-> now we have an ArrayList of all highest correspondence alignments

		//get all cells that are highest correspondence (for comparison only)
		Map<Cell, Double> highCorrCoefficients = getHighestCorrespondencesCoefficient(highestCorrs);

//		System.out.println("\nTest: The highest correspondence coefficients are: ");
//		for (Entry<Cell, Double> e : highCorrCoefficients.entrySet()) {
//			System.out.println(e.getKey().getObject1AsURI().getFragment() + " - " + e.getKey().getObject2AsURI().getFragment() + ": " + e.getValue());
//		}

		//calculate importance coefficient for each matcher
		Map<BasicAlignment, Double> matcherCoefficient = getMatcherCoefficient(highCorrCoefficients, initialAlignments);

		//calculate matcher weight
		Map<BasicAlignment, Double> matcherWeight = getMatcherWeight(matcherCoefficient);
		
//		System.out.println("Test: The matcher weights are: ");
//		
//		for (Entry<BasicAlignment, Double> e : matcherWeight.entrySet()) {
//			System.out.println(e.getKey() + ": " + e.getValue());
//		}


		//produce final alignment
		double weight = 0;

		ArrayList<BasicAlignment> initialAlignmentsWeighted = new ArrayList<BasicAlignment>();
		

		
//		System.out.println("Number of alignments: " + initialAlignments.size());
//		System.out.println("Printing initial alignments");
//		for (BasicAlignment b : initialAlignments) {
//			System.out.println("Alignment " + b + " contains " + b.nbCells() + " cells");
//			for (Cell c : b) {
//				System.out.println(c.getObject1() + " - " + c.getObject2()  + " - " + c.getRelation().getRelation()  + " - " + c.getStrength());
//			}
//		}

		
		//revise the strength for each cell in the initial alignment according to the matcher weight
		for (BasicAlignment b : initialAlignments) {
			//create a new alignment holding the weighted cells and add this alignment to initialAlignmentsWeighted
			BasicAlignment newAlignment = new URIAlignment();

			//get the matcher weight for this particular matcher/alignment
			weight = matcherWeight.get(b);
			
			for (Cell c : b) {
				newAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength()*weight);
			}
			initialAlignmentsWeighted.add(newAlignment);
	
		}

		
//		System.out.println("Printing initialalignmentsweighted");
//		for (BasicAlignment ra : initialAlignmentsWeighted) {
//			System.out.println("\nAlignment");
//			for (Cell cell : ra) {
//				System.out.println(cell.getObject1() + " - " + cell.getObject2() + " - " + cell.getStrength());
//			}
//		}
				
		BasicAlignment ca = createCommonAlignment(initialAlignmentsWeighted);

		//produce final alignment
		BasicAlignment commonAlignment = new URIAlignment();

		for (BasicAlignment ra : initialAlignmentsWeighted) {
			for (Cell cell : ra) {
				commonAlignment.addAlignCell(cell.getObject1(), cell.getObject2(), cell.getRelation().getRelation(), cell.getStrength());
			}
		}

		finalAlignment = computeFinalAlignment(commonAlignment);
		
		System.out.println("Printing final alignment: ");
		for (Cell cell : finalAlignment) {
			System.out.println(cell.getObject1AsURI().getFragment() + " - " + cell.getObject2AsURI().getFragment() + " - " + cell.getRelation().getRelation() + " : " + cell.getStrength());
		}

		//remove correspondences below threshold
		//finalAlignment.cut(0.22);

		return finalAlignment;
	}
	
	public static BasicAlignment createCommonAlignment(ArrayList<BasicAlignment> initialAlignmentsWeighted) throws AlignmentException {
		
		BasicAlignment commonAlignment = new URIAlignment();
		ArrayList<String> cellsList = new ArrayList<String>();
		
		int numAlignments = initialAlignmentsWeighted.size();
		
		//put all cells (objects and relation should suffice) in an ArrayList
		for (BasicAlignment ba : initialAlignmentsWeighted) {
			for (Cell c : ba) {
			cellsList.add(c.getObject1() + "-" + c.getObject2() + "-" + c.getRelation().getRelation());
			}
		}
		
//		System.out.println("Printing cellsList:");
//		for (String s : cellsList) {
//			System.out.println(s);
//		}
		
		//count occurrences of each cell
		Map<String, Integer> cellCountMap = new HashMap<String, Integer>();
		
		int occurrences = 0;
		
		for (String s : cellsList) {
			occurrences = Collections.frequency(cellsList, s);
			cellCountMap.put(s, occurrences);
		}
		
//		System.out.println("PRinting cellCountMap");
//		for (Entry<String, Integer> e : cellCountMap.entrySet()) {
//			System.out.println(e);
//		}
		
		//adding a temporary alignment in order to fetch cells
		BasicAlignment tempAlignment = new URIAlignment();
		
		for (BasicAlignment ba : initialAlignmentsWeighted) {
			for (Cell c : ba) {
				tempAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
			}
		}
		
		//relations not identified by a matcher should be given a strength equal to the average strength of those matchers that have identified it
		int counter = 0;
		
		double avgStrength = 0;
		
		for (BasicAlignment ba : initialAlignmentsWeighted) {
			for (Cell c : ba) {
				double sumStrength = 0;
				counter = cellCountMap.get(c.getObject1() + "-" + c.getObject2() + "-" + c.getRelation().getRelation());
				if (counter != numAlignments) {
					//get all cells with these two objects
					Set<Cell> cellSet = tempAlignment.getAlignCells(c.getObject1(), c.getObject2());
					for (Cell cs : cellSet) {
						sumStrength += cs.getStrength();
					}
					avgStrength = sumStrength / numAlignments;
					//System.out.println("Average strength for " + c.getObject1() + " - " + c.getObject2() + " is " + avgStrength + " (sumStrength is " + sumStrength + "), and (numAlignments are " + numAlignments + ")");
				}
				if (avgStrength > 1.0) {
					avgStrength = 1.0;
				}
				c.setStrength(avgStrength);
			}
		}

		return commonAlignment;
	}

	public static Map<BasicAlignment, Double> getMatcherCoefficient(Map<Cell, Double> highCorrCoefficient, List<BasicAlignment> matchers) throws AlignmentException {

		//using LinkedHashMap to keep the alignments in the right order
		Map<BasicAlignment, Double> matcherCoefficients = new LinkedHashMap<BasicAlignment, Double>();

		//produce the matcher coefficient for each matcher/alignment, where the coefficient is the sum of all highest correspondences (across all matchers) produced by the particular matcher
		for (BasicAlignment a : matchers) {
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

	public static Map<BasicAlignment, Double> getMatcherWeight(Map<BasicAlignment, Double> matcherCoefficient) {

		//get the total coefficient sum
		double sumCoefficients = 0;
		for (Entry<BasicAlignment, Double> e : matcherCoefficient.entrySet()) {
			sumCoefficients += e.getValue();
		}

		//using LinkedHashMap to keep the alignments in the right order
		Map<BasicAlignment, Double> matcherWeightMap = new LinkedHashMap<BasicAlignment, Double>();

		for (Entry<BasicAlignment, Double> e : matcherCoefficient.entrySet()) {
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
	public static Map<Cell, Double> getHighestCorrespondencesCoefficient(List<BasicAlignment> aSet) throws AlignmentException {

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
	public static BasicAlignment getHighestCorrespondencesFromSingleAlignment(BasicAlignment a) throws AlignmentException {

		//get a sorted (by strength) representation of the cells in alignment a
		List<Cell> sortedCells = sortAlignmentCellsByStrength(a);

		double confidenceThreshold = 0.30;

		Set<Cell> todel = new HashSet<Cell>();
		Set<Cell> toKeep = new HashSet<Cell>();
		BasicAlignment highestCorrAlignment = new URIAlignment();

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

		//move all cells "to keep" over to the alignment if they are above the confidence threshold (0.3)
		for (Cell c : toKeep) {
			if (c.getStrength() > confidenceThreshold) {
			highestCorrAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
			}
		}

		//if two cells hold either of the same two objects and their strengths are equal, we omit them from the set
		Set<Cell> cellSet = new HashSet<Cell>();
		for (Cell c : highestCorrAlignment) {
			cellSet.add(c);
		}
		
		Set<Cell> duplicates = new HashSet<Cell>();
		
		//remove cells having the same object 1 and similar strength
		for (Cell c : cellSet) {
			Set<Cell> sameObjects = highestCorrAlignment.getAlignCells1(c.getObject1());
			sameObjects.addAll(highestCorrAlignment.getAlignCells2(c.getObject2()));
			
			if (sameObjects.size() > 1) {
				
				duplicates.addAll(sameObjects);

			}
		}

		for (Cell c : duplicates) {
			highestCorrAlignment.remCell(c);
		}

		return highestCorrAlignment;

	}

	/**
	 * Helper method that sorts the cells in an alignment based on their strenght
	 * @param a input alignment
	 * @return list of cells sorted by strength
	 * @throws AlignmentException
	 */
	private static List<Cell> sortAlignmentCellsByStrength(BasicAlignment a) throws AlignmentException {

		List<Cell> sortedCellList = new ArrayList<Cell>();

		for (Cell c : a) {
			sortedCellList.add(c);
		}

		Collections.sort(sortedCellList);

		return sortedCellList;

	}

	public static BasicAlignment computeFinalAlignment(BasicAlignment commonAlignment) throws AlignmentException {
		
		BasicAlignment finalAlignment = getHighestCorrespondencesFromSingleAlignment(commonAlignment);
		
		finalAlignment.cut(0.22);
		
		return finalAlignment;
	}

	/*public static BasicAlignment computeFinalAlignment(ArrayList<BasicAlignment> inputAlignments) throws AlignmentException {

		BasicAlignment finalAlignment = new URIAlignment();

		ArrayList<Cell> allCells = new ArrayList<Cell>();

		// get all cells in all alignments and put them in a set
		for (BasicAlignment a : inputAlignments) {
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

								//tweak to avoid out of bounds exception from Alignment API (does not allow strengths above 1.0)
								if (c.getStrength() > 1.0) {
									c.setStrength(1.0);
								}

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

								//tweak to avoid out of bounds exception from Alignment API (does not allow strengths above 1.0)
								if (c.getStrength() > 1.0) {
									c.setStrength(1.0);
								}

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

		finalAlignment.normalise();
		return finalAlignment;
	}*/


}
