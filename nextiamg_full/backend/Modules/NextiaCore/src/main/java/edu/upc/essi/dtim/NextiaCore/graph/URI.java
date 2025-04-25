package edu.upc.essi.dtim.NextiaCore.graph;

import java.util.*;
public class URI {
	private String URI;

	public URI() {}
	public URI(String value) {
		this.URI = value;
	}

	public String getURI() {
		return this.URI;
	}
	public void setURI(String URI) {
		this.URI = URI;
	}

	@Override // We override the method so we can compare URIs
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		URI uri = (URI) o;
		return Objects.equals(URI, uri.URI);
	}

	@Override // We override the method so we can compare URIs
	public int hashCode() {
		return Objects.hash(URI);
	}
}