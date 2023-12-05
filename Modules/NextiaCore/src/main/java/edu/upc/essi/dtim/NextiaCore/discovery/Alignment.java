package edu.upc.essi.dtim.NextiaCore.discovery;

public class Alignment {

	Attribute attributeA;
	Attribute attributeB;
	private String label;
	private float similarity;

	public Alignment() {
	}

	public Alignment(Attribute attributeA, Attribute attributeB, String label, float similarity) {
		this.attributeA = attributeA;
		this.attributeB = attributeB;
		this.label = label;
		this.similarity = similarity;
	}


	public Attribute getAttributeA() {
		return attributeA;
	}

	public void setAttributeA(Attribute attributeA) {
		this.attributeA = attributeA;
	}

	public Attribute getAttributeB() {
		return attributeB;
	}

	public void setAttributeB(Attribute attributeB) {
		this.attributeB = attributeB;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public float getSimilarity() {
		return similarity;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}

	////////////////////////////////////////////////////////////////////////////////////////

	public String iriA;
	public String iriB;
	public String labelA;
	public String labelB;
	public String l;
	public String type;
	public Boolean identifier;
//    String score?

	public Alignment(String iriA, String iriB, String l) {
		this.iriA = iriA;
		this.iriB = iriB;
		this.l = l;
	}

	public Alignment(String iriA, String iriB, String l, String type) {
		this.iriA = iriA;
		this.iriB = iriB;
		this.l = l;
		this.type = type;
	}

	public Alignment(String iriA, String iriB, String l, String type, Boolean identifier) {
		this.iriA = iriA;
		this.iriB = iriB;
		this.l = l;
		this.type = type;
		this.identifier = identifier;
	}


	public String getIriL() {
		return "http://www.essi.upc.edu/DTIM/NextiaDI/" + l;
	}

	public String getIriA() {
		return iriA;
	}

	public void setIriA(String iriA) {
		this.iriA = iriA;
	}

	public String getIriB() {
		return iriB;
	}

	public void setIriB(String iriB) {
		this.iriB = iriB;
	}

	public String getLabelA() {
		return labelA;
	}

	public void setLabelA(String labelA) {
		this.labelA = labelA;
	}

	public String getLabelB() {
		return labelB;
	}

	public void setLabelB(String labelB) {
		this.labelB = labelB;
	}

	public String getL() {
		return l;
	}

	public void setL(String l) {
		this.l = l;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getIdentifier() {
		return identifier;
	}

	public void setIdentifier(Boolean identifier) {
		this.identifier = identifier;
	}
	//////////////////////////////////////////////////////////////////////////////////////////////
}