package edu.upc.essi.dtim.NextiaCore.graph.jena;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.*;

public class GraphJenaImpl implements Graph {

    /*
    public GraphJenaImpl(String id, String name, Model triples) {
        this.graph = ModelFactory.createDefaultModel();
        this.graphName = (name != null) ? "http://example/" + name : "null";
    }*/

	public GraphJenaImpl(String graphNameA){
		this.graph = ModelFactory.createDefaultModel();
		this.graphName = graphNameA;
	}
	public GraphJenaImpl() {
		//this.graphName = "http://example/"+ UUID.randomUUID().toString();
		this.graph = ModelFactory.createDefaultModel();
	}

	private String graphName;

	private String graphicalSchema;

	public String getGraphName() {
		return graphName;
	}

	/**
	 * @param subject Subject
	 * @param predicate Predicate
	 * @param object Object
	 */
	@Override
	public void addTriple(String subject, String predicate, String object) {
		Resource r = graph.createResource(subject);
		r.addProperty(graph.createProperty(predicate), graph.createResource(object));
	}


	/**
	 * @param subject Subject
	 * @param predicate Predicate
	 * @param literal literal
	 */
	@Override
	public void addTripleLiteral(String subject, String predicate, String literal) {
		Resource r = graph.createResource(subject);
		r.addProperty(graph.createProperty(predicate), literal);
	}

	/**
	 * @param subject Subject
	 * @param predicate Predicate
	 * @param object Object
	 */
	@Override
	public void deleteTriple(String subject, String predicate, String object) {
		graph.removeAll(new ResourceImpl(subject), new PropertyImpl(predicate), new ResourceImpl(object));
	}

	/**
	 * @param sparql query
	 * @return List
	 */
	@Override
	public List<Map<String, Object>> query(String sparql) {
		List<Map<String, Object>> resultsList = new ArrayList<>();

		try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(sparql), graph)) {
			ResultSetRewindable results = ResultSetFactory.copyResults(qExec.execSelect());
			qExec.close();

			while (results.hasNext()) {
				QuerySolution soln = results.nextSolution();
				Map<String, Object> row = new HashMap<>();

				for (String var : results.getResultVars()) {
					RDFNode node = soln.get(var);

					// Convert RDFNode to a more general data type if possible
					if (node.isLiteral()) {
						row.put(var, node.asLiteral().getValue());
					} else if (node.isResource()) {
						row.put(var, node.asResource().getURI());
					} else {
						row.put(var, node.toString());
					}
				}
				resultsList.add(row);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultsList;


		//return null;
	}

	public void setGraphName(String graphName) {
		this.graphName = graphName;
	}

	public Model getGraph() {
		return graph;
	}
	public void setGraph(Model graph) {
		this.graph = graph;
	}

	@JsonIgnore
	private Model graph;

	public String getGraphicalSchema() {
		return graphicalSchema;
	}

	public void setGraphicalSchema(String graphicalSchema) {
		this.graphicalSchema = graphicalSchema;
	}

	/**
	 * @return iter
	 */
	@Override
	public ResIterator retrieveSubjects() {
		List<String> subjects = new ArrayList<>();

		ResIterator iter = graph.listSubjects();
		return iter;


		/*while (iter.hasNext()) {
			Resource resource = iter.nextResource();
			subjects.add(resource.getURI());
		}
		return subjects;
		*/
	}

	/**
	 * @return list string
	 */
	@Override
	public List<String> retrievePredicates() {
		List<String> predicates = new ArrayList<>();

		StmtIterator iter = graph.listStatements();
		while (iter.hasNext()) {
			Statement statement = iter.nextStatement();
			Property property = statement.getPredicate();
			predicates.add(property.getURI());
		}

		return predicates;
	}

	/**
	 * @param file file
	 */
	@Override
	public void write(String file) {
		try {
			RDFDataMgr.write(new FileOutputStream(file), graph, Lang.TURTLE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param propertyIRI propertyIRI
	 * @return string
	 */
	@Override
	public String getDomainOfProperty(String propertyIRI) {
        /*
        String query = " SELECT ?domain WHERE { <"+propertyIRI+"> <"+ RDFS.domain.toString()+"> ?domain. }";

        List<Map<String, Object>> res = query(query);

        if(!res.isEmpty()){
            return res.get(0).get("domain").toString();
        }
*/
		return null;


	}

	/**
	 * @param resourceIRI resourceIRI
	 * @return string
	 */
	@Override
	public String getRDFSLabel(String resourceIRI) {
        /*
        String query = " SELECT ?label WHERE { <"+resourceIRI+"> <"+ RDFS.label.toString()+"> ?label. }  ";

        List<Map<String, Object>> res = query(query);

        if(!res.isEmpty()){
            return res.get(0).get("label").toString();
        }
        */
		return null;


	}
}
