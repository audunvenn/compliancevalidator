package compliancevalidator.combination;

import org.semanticweb.owl.align.Cell;

public class ExtendedCell {
	
	private Cell cell;
	private double importance;
	
	public ExtendedCell(Cell cell, double importance) {
		super();
		this.cell = cell;
		this.importance = importance;
	}

	public Cell getCell() {
		return cell;
	}

	public void setCell(Cell cell) {
		this.cell = cell;
	}

	public double getImportance() {
		return importance;
	}

	public void setImportance(double importance) {
		this.importance = importance;
	}
	
	
	

}
