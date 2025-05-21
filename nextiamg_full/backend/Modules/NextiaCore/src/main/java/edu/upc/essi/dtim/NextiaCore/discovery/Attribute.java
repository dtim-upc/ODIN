package edu.upc.essi.dtim.NextiaCore.discovery;

import com.fasterxml.jackson.annotation.JsonIgnore;


import java.util.*;

public class Attribute {
	String id;
	@JsonIgnore
	Collection<Alignment> alignment;
	private String name;
	private String type;

	public Attribute() {}

	public Attribute(String name, String type) {
		this.name = name;
		this.type = type;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public Collection<Alignment> getAlignment() {
		return alignment;
	}
	public void setAlignment(Collection<Alignment> alignment) {
		this.alignment = alignment;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}