package compliancevalidator.misc;

import java.util.Comparator;

public class Cell {
	
	
	private String object1;
	private String object2;
	private double strength;
	private String relation;
	public Cell(String object1, String object2, String relation, double strength) {
		super();
		this.object1 = object1;
		this.object2 = object2;
		this.strength = strength;
		this.relation = relation;
	}
	public String getObject1() {
		return object1;
	}
	public void setObject1(String object1) {
		this.object1 = object1;
	}
	public String getObject2() {
		return object2;
	}
	public void setObject2(String object2) {
		this.object2 = object2;
	}
	public double getStrength() {
		return strength;
	}
	public void setStrength(double strength) {
		this.strength = strength;
	}
	public String getRelation() {
		return relation;
	}
	public void setRelation(String relation) {
		this.relation = relation;
	}
	
	public int compareTo(Cell c){
	    return Comparator.comparing(Cell::getObject1)
	              .thenComparing(Cell::getObject2)
	              .thenComparing(Cell::getRelation)
	              .thenComparingDouble(Cell::getStrength)
	              .compare(this, c);
	}
	
	public static void main(String[] args) {
		
		String e1 = "e1";
		String e2 = "e2";
		String equivalence = "=";

		
		Cell c1 = new Cell(e1, e2, equivalence, 0.8);
		Cell c2 = new Cell(e1, e2, equivalence, 0.8);
		
		if (c1.equals(c2)) {		
		System.out.println(c1 + " and " + c2 + " are similar");
		} else {
			System.out.println(c1 + " and " + c2 + " are not similar");
		}
	}
	
	
	
}