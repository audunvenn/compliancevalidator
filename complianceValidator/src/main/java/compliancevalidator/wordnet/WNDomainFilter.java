package compliancevalidator.wordnet;

import java.io.File;
import java.io.FileNotFoundException;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import net.didion.jwnl.JWNLException;

/**
 * @author audunvennesland
 * 2. okt. 2017 
 */
public class WNDomainFilter {
	
	public static void main(String[] args) throws AlignmentException, FileNotFoundException, JWNLException {
		
		//import alignment
		File af1 = new File("./files/wndomainexperiment/biblio-bibo/refalign.rdf");
		AlignmentParser parser = new AlignmentParser();
		BasicAlignment a1 = (BasicAlignment) parser.parse(af1.toURI().toString());
		
		
		
		for (Cell c : a1) {
			String s1 = c.getObject1AsURI().getFragment();
			String s2 = c.getObject2AsURI().getFragment();
			
			if (s1 != null && s2 != null) {
			
			if (WNDomain.sameDomain(s1, s2) == true) {
				System.out.println(s1 + " and " + s2 + " are from the same domain");
			} else {
				System.out.println(s1 + " and " + s2 + " are not from the same domain");
			}
		}
		}
		
		//if they are increase the confidence, if they are not reduce their confidence
		
		//put the refined cells into a new alignment
	}

}
