package edu.upc.essi.dtim.NextiaCore.graph;

public class Triple {
	private final URI subject;
	private final URI predicate;
	private final URI object;

	public Triple(URI subject, URI predicate, URI object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	@Override
	public String toString() {
			return subject.toString() + " " + predicate.toString() + " " + object + " .";
	}
}