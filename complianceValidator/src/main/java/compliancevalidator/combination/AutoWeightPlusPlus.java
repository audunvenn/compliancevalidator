package compliancevalidator.combination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;


public class AutoWeightPlusPlus {

	public static BasicAlignment runAutoweightPlusPlus(ArrayList<BasicAlignment> initialAlignments) throws AlignmentException {

		BasicAlignment finalAlignment = new URIAlignment();

		ArrayList<BasicAlignment> highestCorrs = new ArrayList<BasicAlignment>();

		ArrayList<BasicAlignment> initialsClone = new ArrayList<BasicAlignment>(initialAlignments);

		//clone the initialAlignments list, otherwise the references are messed up
		for (BasicAlignment b : initialsClone) {
			BasicAlignment bClone = (BasicAlignment) b.clone();
			highestCorrs.add(getHighestCorrespondencesFromSingleAlignment(bClone));
		}

		//get all cells that are highest correspondence (for comparison only)
		Map<Cell, Double> highCorrCoefficients = getHighestCorrespondencesCoefficient(highestCorrs);

		//calculate importance coefficient for each matcher
		Map<BasicAlignment, Double> matcherCoefficient = getMatcherCoefficient(highCorrCoefficients, initialAlignments);

		//calculate matcher weight
		Map<BasicAlignment, Double> matcherWeight = getMatcherWeight(matcherCoefficient);

		System.out.println("Test: The matcher weights are: ");

		for (Entry<BasicAlignment, Double> e : matcherWeight.entrySet()) {
			System.out.println(e.getKey() + ": " + e.getValue());
		}


		//produce final alignment
		double weight = 0;

		ArrayList<BasicAlignment> initialAlignmentsWeighted = new ArrayList<BasicAlignment>();

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


		BasicAlignment ca = createCommonAlignment(initialAlignmentsWeighted);

		System.out.println("Test: Common Alignment:");
		for (Cell c : ca) {
			System.out.println(c.getObject1AsURI().getFragment() + " - " + c.getObject2AsURI().getFragment() + " - " + c.getRelation().getRelation() + " : " + c.getStrength());
		}

		//produce final alignment
		BasicAlignment commonAlignment = new URIAlignment();

		for (BasicAlignment ra : initialAlignmentsWeighted) {
			for (Cell cell : ra) {
				commonAlignment.addAlignCell(cell.getObject1(), cell.getObject2(), cell.getRelation().getRelation(), cell.getStrength());
			}
		}
		
		//testing with manually produced common alignment
		finalAlignment = computeFinalAlignment(commonAlignment);

				System.out.println("Printing final alignment: ");
				for (Cell cell : finalAlignment) {
					System.out.println(cell.getObject1AsURI().getFragment() + " - " + cell.getObject2AsURI().getFragment() + " - " + cell.getRelation().getRelation() + " : " + cell.getStrength());
				}

		//remove correspondences below threshold
		finalAlignment.cut(0.30);

		return finalAlignment;
	}

	private static BasicAlignment createCommonAlignment(ArrayList<BasicAlignment> initialAlignmentsWeighted) throws AlignmentException {

		BasicAlignment commonAlignment = new URIAlignment();
		ArrayList<String> cellsList = new ArrayList<String>();

		int numAlignments = initialAlignmentsWeighted.size();

		//put all cells (objects and relation should suffice) in an ArrayList
		for (BasicAlignment ba : initialAlignmentsWeighted) {
			for (Cell c : ba) {
				cellsList.add(c.getObject1() + "-" + c.getObject2() + "-" + c.getRelation().getRelation());
			}
		}

		//count occurrences of each cell
		Map<String, Integer> cellCountMap = new HashMap<String, Integer>();

		int occurrences = 0;

		for (String s : cellsList) {
			occurrences = Collections.frequency(cellsList, s);
			cellCountMap.put(s, occurrences);
		}

		//adding a temporary alignment in order to fetch cells
		BasicAlignment tempAlignment = new URIAlignment();

		for (BasicAlignment ba : initialAlignmentsWeighted) {
			for (Cell c : ba) {
				tempAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
			}
		}

		System.out.println("tempAlignment contains" + tempAlignment.nbCells() + " cells");

		//relations not identified by a matcher should be given a strength equal to the average strength of those matchers that have identified it
		int counter = 0;

		double avgStrength = 0;

		for (BasicAlignment ba : initialAlignmentsWeighted) {
			for (Cell c : ba) {
				double sumStrength = 0;
				counter = cellCountMap.get(c.getObject1() + "-" + c.getObject2() + "-" + c.getRelation().getRelation());

				//if all alignments have the cell
				if (counter == numAlignments) {
					//get all cells with these two objects
					Set<Cell> cellSetAll = tempAlignment.getAlignCells(c.getObject1(), c.getObject2());

					for (Cell cell : cellSetAll) {
						
						//sum all their strengths
						sumStrength += cell.getStrength();
						if (sumStrength > 1.0) {
							sumStrength = 1.0;
						}
					}
					c.setStrength(sumStrength);
					commonAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
				} else {
					//get all cells with these two objects
					Set<Cell> cellSet = tempAlignment.getAlignCells(c.getObject1(), c.getObject2());
					for (Cell cs : cellSet) {
						System.out.println("Strength: " + cs.getStrength());
						sumStrength += cs.getStrength();
					}
					avgStrength = sumStrength / numAlignments;

					if (avgStrength > 1.0) {
						avgStrength = 1.0;
					}
					c.setStrength(avgStrength);
					commonAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
				}

			}
		}

		return commonAlignment;
	}

	private static Map<BasicAlignment, Double> getMatcherCoefficient(Map<Cell, Double> highCorrCoefficient, List<BasicAlignment> matchers) throws AlignmentException {

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

	private static Map<BasicAlignment, Double> getMatcherWeight(Map<BasicAlignment, Double> matcherCoefficient) {

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
	 * @param aSet a set of alignment with highest correspondences from several matchers
	 * @return a map of 'Highest Correspondences' as key and with an important coefficient as value
	 * @throws AlignmentException 
	 */
	private static Map<Cell, Double> getHighestCorrespondencesCoefficient(List<BasicAlignment> highestCorrespondences) throws AlignmentException {

		Map<Cell, Double> highestCorrsCoefficient = new HashMap<Cell, Double>();

		//how many matchers (alignments) are involved?
		int numMatchers = highestCorrespondences.size();

		BasicAlignment tempAlignment = new URIAlignment();

		//get all cells from the initial list of highestCorrespondences and put them in a temporary alignment
		for (BasicAlignment a : highestCorrespondences) {
			for (Cell c : a) {
				tempAlignment.addAlignCell(c.getObject1(), c.getObject2(), c.getRelation().getRelation(), c.getStrength());
			}
		}


		Set<Cell> toDel = new HashSet<Cell>();

		//get a sorted (by strength) representation of the cells in alignment a
		List<Cell> sortedCells = sortAlignmentCellsByStrength(tempAlignment);

		for (Cell d : sortedCells) {
			System.out.println(d.getObject1() + " - " + d.getObject2() + " - " + d.getStrength());
		}

		for (Cell c : sortedCells) {

			if (!toDel.contains(c)) {

				Set<Cell> simCells = tempAlignment.getAlignCells(c.getObject1(), c.getObject2());

				if (simCells.size() > 0) {

					for (Cell simCell : simCells) {

						System.out.println("Test simCell: " + simCell.getObject1() + " - " + simCell.getObject2());

						//check if their relations match
						if (simCell.getRelation().equals(c.getRelation())) {

							//As long as not all matchers have detected this cell, we add it to the coefficient map
							if (simCells.size() != numMatchers) {

								highestCorrsCoefficient.put(c, (double)1/simCells.size());
								toDel.add(c);
							} else {
								toDel.add(c);
							}
						}
					}
				}
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
	private static BasicAlignment getHighestCorrespondencesFromSingleAlignment(BasicAlignment a) throws AlignmentException {

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
	 * Helper method that sorts the cells in an alignment based on their strength
	 * @param a input alignment
	 * @return list of cells sorted by strength
	 * @throws AlignmentException
	 */
	private static ArrayList<Cell> sortAlignmentCellsByStrength(BasicAlignment a) throws AlignmentException {

		ArrayList<Cell> sortedCellList = new ArrayList<Cell>();

		for (Cell c : a) {
			sortedCellList.add(c);
		}

		Collections.sort(sortedCellList);

		return sortedCellList;

	}

	private static BasicAlignment computeFinalAlignment(BasicAlignment commonAlignment) throws AlignmentException {

		BasicAlignment finalAlignment = getHighestCorrespondencesFromSingleAlignment(commonAlignment);

		return finalAlignment;
	}

	


}
