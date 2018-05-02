package compliancevalidator.combination;

import java.util.ArrayList;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import compliancevalidator.misc.StringUtilities;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;


/**
 * This class represents a parallel composition, that is, it compares the set of input alignments, and uses different methods to compute a final alignment. 
 * @author audunvennesland
 * 2. feb. 2017
 */

public class ParallelComposition {

	/**
	 * Creates an alignment that includes correspondences that are computed by n-x matchers (e.g. 3 of 4 matchers)
	 * @param inputAlignments A list of all alignments produced by the matchers involved
	 * @return an alignment that includes the "voted" set of correspondences 
	 * @throws AlignmentException
	 */
	public static BasicAlignment simpleVote(ArrayList<Alignment> inputAlignments) throws AlignmentException {

		BasicAlignment simpleVoteAlignment = new URIAlignment();

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
		ArrayList<Cell> toKeep = new ArrayList<Cell>();

		for (Cell currentCell : allCells) {
			
			if (!todel.contains(currentCell)) {
				
				// get all cells that has the same object1 as c1
				ArrayList<Cell> sameObj1 = new ArrayList<Cell>();				
				for (Cell c : allCells) {
					if (c.getObject1().equals(currentCell.getObject1())) {
						sameObj1.add(c);
					}
				}
							
				//why bigger than 1 and not?
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
										
					//if the number of cells in toCheck (those that have the same object1 and object 2 as currentCell) is represented by numAlignments-1 (e.g.3 of 4 alignments)
					if (toCheck.size() >= (numAlignments - 2)) {

						for (Cell c : toCheck) {

							//checking that c (this cell) in fact is not currentCell
							if (c != currentCell) {
								toKeep.add(c);
								todel.add(currentCell);
								
							}
						}
					}

				} else {
					
				}
			}
		}
		

		for (Cell c : toKeep) {
			simpleVoteAlignment.addAlignCell(c.getObject1(), c.getObject2(), StringUtilities.validateRelationType(c.getRelation().getRelation()),
					c.getStrength());
		}
		
		simpleVoteAlignment.normalise();


		return simpleVoteAlignment;
	}
	


	
}
