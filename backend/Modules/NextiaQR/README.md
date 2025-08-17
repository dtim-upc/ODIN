# ODIN Query Rewriting Algorithm

This module implements the ODIN query rewriting algorithm as described in the research paper. The algorithm rewrites SPARQL queries over an integrated graph into conjunctive queries over the underlying data sources using edge-based query rewriting.

## Overview

The ODIN query rewriting algorithm is designed to handle queries over heterogeneous data sources by:
1. Parsing SPARQL queries into their structural components
2. Identifying concepts and relationships in the query
3. Finding covering conjunctive queries for each concept
4. Combining queries through edge-based merging
5. Generating the final union of conjunctive queries

## Key Components

### Core Classes

- **`ODINQueryRewriting`**: Main implementation of the query rewriting algorithm
- **`ConjunctiveQuery`**: Represents a conjunctive query with projections, join conditions, and wrappers
- **`Wrapper`**: Represents a data source wrapper
- **`EquiJoin`**: Represents equi-join conditions between attributes
- **`QueryStructure`**: Contains the parsed query structure (projections, basic graph pattern, ontology model)

### Graph Models

- **`IntegrationGraph`**: The main graph used in the algorithm
- **`CQVertex`**: Vertices in the integration graph that contain conjunctive queries
- **`IntegrationEdge`**: Edges in the integration graph that contain wrappers
- **`RelationshipEdge`**: Edges in the concepts graph

### Result Models

- **`QueryRewritingResult`**: Contains the final result of the query rewriting process

## Usage

### Basic Usage

```java
// Create a dataset with your data
Dataset dataset = createYourDataset();

// Define your SPARQL query
String sparqlQuery = "SELECT ?x ?y WHERE { " +
        "?x <http://www.dtim.upc.edu/odin/HAS_FEATURE> ?f1 . " +
        "?y <http://www.dtim.upc.edu/odin/HAS_FEATURE> ?f2 . " +
        "?x <http://example.org/relatedTo> ?y " +
        "}";

// Parse the query
QueryStructure queryStructure = ODINQueryRewriting.parseSPARQL(sparqlQuery, dataset);

// Rewrite the query
QueryRewritingResult result = ODINQueryRewriting.rewriteToUnionOfConjunctiveQueries(queryStructure, dataset);

// Access the results
System.out.println("Number of rewritings: " + result.getNumberOfRewritings());
System.out.println("Conjunctive queries: " + result.getConjunctiveQueries());
```

### Using the Interface

```java
// Create an instance
ODINQueryRewriting queryRewriting = new ODINQueryRewriting();

// Generate querying structures
queryRewriting.generateQueryingStructures(integratedGraph, datasets);
```

## Algorithm Steps

1. **Query Parsing**: Parse SPARQL query into projections (PI), basic graph pattern (PHI_p), and ontology model (PHI_o)

2. **Structure Population**: Populate optimized data structures including:
   - All triples per wrapper
   - Covered IDs per wrapper in query
   - Features per attribute mappings
   - Attribute per feature and wrapper mappings

3. **Concept Identification**: Identify query-related concepts and build a concepts graph

4. **Covering Query Generation**: For each concept, generate covering conjunctive queries

5. **Integration Graph Construction**: Build the integration graph with CQVertices and IntegrationEdges

6. **Edge-based Merging**: Iteratively merge vertices until no edges remain, combining conjunctive queries

7. **Result Generation**: Return the final union of conjunctive queries

## Dependencies

- **Apache Jena**: For RDF/SPARQL processing
- **JGraphT**: For graph operations
- **Google Guava**: For collections and utilities

## Configuration

The algorithm uses several namespaces and predicates that should be configured according to your ODIN setup:

- `HAS_FEATURE`: Predicate for concept-feature relationships
- `CONCEPT`: Type for concept resources
- `HAS_ATTRIBUTE`: Predicate for wrapper-attribute relationships
- Various RDF/RDFS/OWL namespaces for standard semantic web constructs

## Example

See `QueryRewritingExample.java` for a complete working example of how to use the algorithm.

## Notes

- The algorithm is designed to work with the ODIN system's specific data model and ontology
- Performance optimizations include caching of covering relationships
- The algorithm handles both single-concept and multi-concept queries
- Edge-based merging ensures that all relationships in the original query are preserved in the rewritten queries

## References

This implementation is based on the research paper describing the ODIN query rewriting algorithm. The original implementation can be found in the ODIN-origin repository.