package net.amham.odfe.difference;

public class DocumentDifference {
	//this can be done like the enum radius and something example

	public enum DiffType {CHANGED, ADDED, MISSING};
	private DiffType difftype = DiffType.CHANGED;
	

	protected String name;
	protected String detail;

	public DocumentDifference(DiffType type) {
		difftype = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public DiffType getType() {
		return difftype;
	}
}
