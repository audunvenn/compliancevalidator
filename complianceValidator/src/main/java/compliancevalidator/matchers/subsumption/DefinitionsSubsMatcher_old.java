package compliancevalidator.matchers.subsumption;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import compliancevalidator.misc.Jaccard;
import compliancevalidator.misc.StringUtilities;

public class DefinitionsSubsMatcher_old {
	
	public static void main(String[] args) throws IOException {
		
		/**relevant matchers AIXM - AIRM
		 * If equal words in both definitions are above a certain threshold, let the entity with most words < entity with fewest words
		 * If the entities are domain in object properties having the same range, they share some similarity
		 * If entity 1 is a compound containing a part of entity 2, the common part of entity 1 and entity 2 is often the first part of the compound (e.g. CloudLayer < Cloud), contrary to previous compound strategies
		 * where the compound head has been the last part of the compound (e.g. electronicBook < Book)
		 * But, the normal compound strategy can also work (e.g. "AerodromeSurfaceWind" < "Wind")
		 */
		
		
		String name1 = "CloudLayer";
		String name2 = "Cloud";
		
		String def1 = "An aggregation of runway visual range conditions for a single runway, typically reported together at an aerodrome";
		String def2 = "The range over which the pilot of an aircraft on the centre line of a runway can see the runway surface markings or the lights delineating the runway or identifying its centre line.";
		
		definitionsSubsMatcher(def1, def2);
	}
	
	public static double definitionsSubsMatcher(String s1, String s2) throws IOException {
		double score = 0;
		double same = 0;
		double threshold = 2;
		
		String s1WOStopwords = StringUtilities.removeStopWordsFromString(s1);
		String s2WOStopwords = StringUtilities.removeStopWordsFromString(s2);
		
		String[] s1Array = s1WOStopwords.split(" ");
		String[] s2Array = s2WOStopwords.split(" ");
		
		//transform the arrays to set to avoid duplicates
		Set<String> s1Set = new HashSet<String>(Arrays.asList(s1Array));
		Set<String> s2Set = new HashSet<String>(Arrays.asList(s2Array));
		
		System.out.println("Words in set 1");
		for (String a : s1Set) {
			System.out.println(a);
		}
		
		System.out.println("Words in set 2");
		for (String a : s2Set) {
			System.out.println(a);
		}
		
		double jaccard = Jaccard.jaccardSetSim(s1Set, s2Set);
		
		System.out.println("Jaccard is " + jaccard);

		
		ArrayList<String> similarTokens = new ArrayList<String>();
		
		for (String s1S : s1Set) {
			for (String s2S : s2Set) {
				if (s1S.equals(s2S)) {
					same+=1;
					similarTokens.add(s1S);
				}
			}
		}
		
		System.out.println("Same = " + same);
		
		System.out.println("Same words: ");
		
		for (String s : similarTokens) {
			System.out.println(s);
		}
		
		if (same >= threshold) {
			if (s1Array.length > s2Array.length) {
				System.out.println("S1 < S2");
			} else {
				System.out.println("S1 < S2");
			}
		}
		
		return score;
		
		
	}

}
