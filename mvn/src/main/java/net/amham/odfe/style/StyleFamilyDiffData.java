package net.amham.odfe.style;

import net.amham.odfe.json.DiffType;

public class StyleFamilyDiffData {
	public DiffType type;
	public OdfeStyleFamily reference;
	public OdfeStyleFamily target;

	public StyleFamilyDiffData() {
	}
	public DiffType getType() {
		return type;
	}

	public void setType(DiffType type) {
		this.type = type;
	}

	public OdfeStyleFamily getReference() {
		return reference;
	}

	public void setReference(OdfeStyleFamily family) {
		this.reference = family;
	}

	public void addTarget(OdfeStyleFamily trgFam) {
		target = trgFam;
	}

}