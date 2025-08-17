package edu.upc.essi.dtim.NextiaQR.rewriting;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaQR.rewriting.models.*;
import org.apache.jena.graph.Triple;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.algebra.op.OpJoin;
import org.apache.jena.sparql.algebra.op.OpProject;
import org.apache.jena.sparql.algebra.op.OpTable;
import org.apache.jena.sparql.core.BasicPattern;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the ODIN query rewriting algorithm as described in the paper.
 * This algorithm rewrites SPARQL queries over an integrated graph into conjunctive queries
 * over the underlying data sources using edge-based query rewriting.
 */
public class ODINQueryRewriting implements IQueryRewriting {

    // Namespaces used in the ODIN system
    private static final String RDFS_NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";
    private static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String OWL_NAMESPACE = "http://www.w3.org/2002/07/owl#";
    private static final String SCHEMA_NAMESPACE = "http://schema.org/";
    
    // Global graph predicates
    private static final String HAS_FEATURE = "http://www.dtim.upc.edu/odin/HAS_FEATURE";
    private static final String CONCEPT = "http://www.dtim.upc.edu/odin/CONCEPT";
    private static final String HAS_ATTRIBUTE = "http://www.dtim.upc.edu/odin/HAS_ATTRIBUTE";

    // Caching structures for optimization
    private static final Map<BasicPattern, Map<Set<Wrapper>, Boolean>> coveringCache = Maps.newHashMap();
    private static final Map<String, Set<Triple>> allTriplesPerWrapper = Maps.newHashMap();
    private static final Map<Wrapper, Set<String>> coveredIDsPerWrapperInQuery = Maps.newHashMap();
    private static final Map<String, String> featuresPerAttribute = Maps.newHashMap();
    private static final Map<WrapperFeaturePair, String> attributePerFeatureAndWrapper = Maps.newHashMap();
    private static final Map<String, Set<String>> featuresPerConceptInQuery = Maps.newHashMap();

    // Query-related data
    private static final Set<String> queriedIDs = Sets.newHashSet();

    /**
     * Checks if a string represents a wrapper
     */
    private static boolean isWrapper(String w) {
        return w.contains("Wrapper") || w.contains("DataSource");
    }

    /**
     * Adds a triple to a basic pattern
     */
    private static void addTriple(BasicPattern pattern, String s, String p, String o) {
        pattern.add(new Triple(
            new ResourceImpl(s).asNode(),
            new PropertyImpl(p).asNode(),
            new ResourceImpl(o).asNode()
        ));
    }

    /**
     * Adds a triple to a model
     */
    private static void addTriple(Model model, String s, String p, String o) {
        model.add(new ResourceImpl(s), new PropertyImpl(p), new ResourceImpl(o));
    }

    /**
     * Creates an ontology model from a basic pattern
     */
    private static OntModel ontologyFromPattern(BasicPattern PHI_p) {
        OntModel o = ModelFactory.createOntologyModel();
        PHI_p.getList().forEach(t ->
            addTriple(o, t.getSubject().getURI(), t.getPredicate().getURI(), t.getObject().getURI())
        );
        return o;
    }

    /**
     * Executes a SPARQL query on a model and returns a ResultSet
     */
    private static ResultSet executeQuery(Model model, String queryString) {
        try (QueryExecution qExec = QueryExecutionFactory.create(QueryFactory.create(queryString), model)) {
            ResultSetRewindable results = ResultSetFactory.copyResults(qExec.execSelect());
            qExec.close();
            return results;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if a set of wrappers covers a basic pattern
     */
    private static boolean covering(Set<Wrapper> W, BasicPattern PHI_p) {
        if (coveringCache.containsKey(PHI_p) && coveringCache.get(PHI_p).containsKey(W)) {
            return coveringCache.get(PHI_p).get(W);
        }

        Set<Triple> coveredPattern = Sets.newHashSet();
        W.forEach(w -> {
            Set<Triple> wrapperTriples = allTriplesPerWrapper.get(w.getWrapper());
            if (wrapperTriples != null) {
                coveredPattern.addAll(wrapperTriples);
            }
        });

        coveringCache.putIfAbsent(PHI_p, Maps.newHashMap());
        boolean covers = coveredPattern.containsAll(Sets.newHashSet(PHI_p.getList()));
        coveringCache.get(PHI_p).put(W, covers);

        return covers;
    }

    /**
     * Checks if a set of wrappers is minimal for covering a pattern
     */
    private static boolean minimal(Set<Wrapper> W, BasicPattern PHI_p) {
        for (Wrapper w : W) {
            Set<Wrapper> WMinusW = Sets.difference(W, Sets.newHashSet(w));
            if (covering(WMinusW, PHI_p)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Populates optimized data structures for query rewriting
     */
    private static void populateOptimizedStructures(Model model, BasicPattern queryPattern) {
        // Populate allTriplesPerWrapper
        ResultSet wrapperResult = executeQuery(model, "SELECT DISTINCT ?g WHERE { GRAPH ?g { ?s ?p ?o } }");
        if (wrapperResult != null) {
            wrapperResult.forEachRemaining(w -> {
                String wrapper = w.get("g").asResource().getURI();
                if (isWrapper(wrapper)) {
                    BasicPattern triplesForW = new BasicPattern();
                    ResultSet tripleResult = executeQuery(model, "SELECT ?s ?p ?o WHERE { GRAPH <" + wrapper + "> { ?s ?p ?o } }");
                    if (tripleResult != null) {
                        tripleResult.forEachRemaining(res -> {
                            triplesForW.add(new Triple(
                                new ResourceImpl(res.get("s").toString()).asNode(),
                                new PropertyImpl(res.get("p").toString()).asNode(),
                                new ResourceImpl(res.get("o").toString()).asNode()
                            ));
                        });
                    }
                    allTriplesPerWrapper.put(wrapper, Sets.newHashSet(triplesForW.getList()));
                }
            });
        }

        // Populate coveredIDsPerWrapperInQuery and queriedIDs
        ResultSet idResult = executeQuery(model, "SELECT DISTINCT ?g ?f WHERE { GRAPH ?g {" +
                "?f <" + RDFS_NAMESPACE + "subClassOf> <" + SCHEMA_NAMESPACE + "identifier> } }");
        if (idResult != null) {
            idResult.forEachRemaining(gf -> {
                Wrapper w = new Wrapper(gf.get("g").asResource().getURI());
                if (isWrapper(w.getWrapper())) {
                    String ID = gf.get("f").asResource().getURI();
                    coveredIDsPerWrapperInQuery.putIfAbsent(w, Sets.newHashSet());
                    
                    boolean IDisInTheQuery = false;
                    for (Triple t : queryPattern.getList()) {
                        if (t.getObject().getURI().equals(ID)) {
                            IDisInTheQuery = true;
                            break;
                        }
                    }
                    
                    if (IDisInTheQuery) {
                        coveredIDsPerWrapperInQuery.get(w).add(ID);
                        queriedIDs.add(ID);
                    }
                }
            });
        }

        // Populate featuresPerAttribute
        ResultSet featureResult = executeQuery(model, "SELECT DISTINCT ?a ?f WHERE { GRAPH ?g {" +
                "?a <" + OWL_NAMESPACE + "sameAs> ?f } }");
        if (featureResult != null) {
            featureResult.forEachRemaining(af -> {
                featuresPerAttribute.put(af.get("a").asResource().getURI(), af.get("f").asResource().getURI());
            });
        }

        // Populate attributePerFeatureAndWrapper
        allTriplesPerWrapper.forEach((w, triples) -> {
            triples.stream()
                .filter(t -> t.getPredicate().getURI().equals(HAS_FEATURE))
                .map(t -> t.getObject().getURI())
                .forEach(f -> {
                    ResultSet attrResult = executeQuery(model, "SELECT ?a WHERE { GRAPH ?g { ?a <" + OWL_NAMESPACE + "sameAs> <" + f + "> . " +
                            "<" + w + "> <" + HAS_ATTRIBUTE + "> ?a } }");
                    if (attrResult != null) {
                        attrResult.forEachRemaining(a -> {
                            attributePerFeatureAndWrapper.put(new WrapperFeaturePair(new Wrapper(w), f), a.get("a").toString());
                        });
                    }
                });
        });

        // Populate featuresPerConceptInQuery
        queryPattern.forEach(t -> {
            if (t.getPredicate().getURI().equals(HAS_FEATURE)) {
                featuresPerConceptInQuery.putIfAbsent(t.getSubject().getURI(), Sets.newHashSet());
                featuresPerConceptInQuery.get(t.getSubject().getURI()).add(t.getObject().getURI());
            }
        });
    }

    /**
     * Combines sets of conjunctive queries
     */
    private static Set<ConjunctiveQuery> combineSetsOfCQs(Set<ConjunctiveQuery> CQ_A, Set<ConjunctiveQuery> CQ_B,
                                                          Set<Wrapper> edgeCoveringWrappers, BasicPattern PHI_p) {
        return Sets.cartesianProduct(CQ_A, CQ_B).stream()
                .filter(cp -> !Collections.disjoint(cp.get(0).getWrappers(), edgeCoveringWrappers) ||
                             !Collections.disjoint(cp.get(1).getWrappers(), edgeCoveringWrappers))
                .filter(cp -> minimal(Sets.union(cp.get(0).getWrappers(), cp.get(1).getWrappers()), PHI_p))
                .map(cp -> findJoins(cp.get(0), cp.get(1)))
                .collect(Collectors.toSet());
    }

    /**
     * Merges two conjunctive queries
     */
    private static ConjunctiveQuery mergeCQs(ConjunctiveQuery CQ_A, ConjunctiveQuery CQ_B) {
        ConjunctiveQuery mergedCQ = new ConjunctiveQuery();
        mergedCQ.getProjections().addAll(Sets.union(CQ_A.getProjections(), CQ_B.getProjections()));
        mergedCQ.getJoinConditions().addAll(Sets.union(CQ_A.getJoinConditions(), CQ_B.getJoinConditions()));
        mergedCQ.getWrappers().addAll(Sets.union(CQ_A.getWrappers(), CQ_B.getWrappers()));
        return mergedCQ;
    }

    /**
     * Finds join conditions between two conjunctive queries
     */
    private static ConjunctiveQuery findJoins(ConjunctiveQuery CQ_A, ConjunctiveQuery CQ_B) {
        Set<String> IDa = Sets.newHashSet();
        CQ_A.getWrappers().forEach(w -> {
            Set<String> ids = coveredIDsPerWrapperInQuery.get(w);
            if (ids != null) {
                IDa.addAll(ids);
            }
        });

        Set<String> IDb = Sets.newHashSet();
        CQ_B.getWrappers().forEach(w -> {
            Set<String> ids = coveredIDsPerWrapperInQuery.get(w);
            if (ids != null) {
                IDb.addAll(ids);
            }
        });

        Set<EquiJoin> joinConditions = Sets.newHashSet();
        Sets.intersection(IDa, IDb).forEach(ID -> {
            CQ_A.getWrappers().forEach(wA -> {
                CQ_B.getWrappers().forEach(wB -> {
                    WrapperFeaturePair pairA = new WrapperFeaturePair(wA, ID);
                    WrapperFeaturePair pairB = new WrapperFeaturePair(wB, ID);
                    
                    if (attributePerFeatureAndWrapper.containsKey(pairA) && 
                        attributePerFeatureAndWrapper.containsKey(pairB)) {
                        
                        String L = attributePerFeatureAndWrapper.get(pairA);
                        String R = attributePerFeatureAndWrapper.get(pairB);
                        
                        if (!L.equals(R) && !joinConditions.contains(new EquiJoin(L, R)) && 
                            !joinConditions.contains(new EquiJoin(R, L))) {
                            joinConditions.add(new EquiJoin(L, R));
                        }
                    }
                });
            });
        });
        
        ConjunctiveQuery CQ = mergeCQs(CQ_A, CQ_B);
        CQ.getJoinConditions().addAll(joinConditions);
        return CQ;
    }

    /**
     * Gets covering conjunctive queries for a concept
     */
    private static Set<ConjunctiveQuery> getConceptCoveringCQs(String c, InfModel PHI_o, Model model) {
        Map<Wrapper, Set<String>> attsPerWrapper = Maps.newHashMap();
        Set<String> F = Sets.newHashSet();
        
        ResultSet featureResult = executeQuery(PHI_o, "SELECT ?f WHERE {<" + c + "> <" + HAS_FEATURE + "> ?f }");
        if (featureResult != null) {
            featureResult.forEachRemaining(f -> F.add(f.get("f").asResource().getURI()));
        }
        
        // Case when the Concept has no data, we need to identify the wrapper using the concept
        if (F.isEmpty()) {
            ResultSet wrapperResult = executeQuery(model, "SELECT ?g WHERE { GRAPH ?g { <" + c + "> <" + RDF_NAMESPACE + "type" + "> <" + CONCEPT + "> } }");
            if (wrapperResult != null) {
                wrapperResult.forEachRemaining(wrapper -> {
                    String w = wrapper.get("g").toString();
                    if (isWrapper(w)) {
                        attsPerWrapper.putIfAbsent(new Wrapper(w), Sets.newHashSet());
                    }
                });
            }
        }
        
        // Unfold LAV mappings
        F.forEach(f -> {
            ResultSet W = executeQuery(PHI_o, "SELECT ?g WHERE { GRAPH ?g { <" + c + "> <" + HAS_FEATURE + "> <" + f + "> } }");
            if (W != null) {
                W.forEachRemaining(wRes -> {
                    String w = wRes.get("g").asResource().getURI();
                    if (isWrapper(w)) {
                        String attribute = attributePerFeatureAndWrapper.get(new WrapperFeaturePair(new Wrapper(w), f));
                        if (attribute != null) {
                            attsPerWrapper.putIfAbsent(new Wrapper(w), Sets.newHashSet());
                            attsPerWrapper.get(new Wrapper(w)).add(attribute);
                        }
                    }
                });
            }
        });

        Set<ConjunctiveQuery> candidateCQs = Sets.newHashSet();
        attsPerWrapper.keySet().forEach(w -> {
            ConjunctiveQuery Q = new ConjunctiveQuery(attsPerWrapper.get(w), Sets.newHashSet(), Sets.newHashSet(w));
            candidateCQs.add(Q);
        });

        Set<ConjunctiveQuery> coveringCQs = Sets.newHashSet();
        while (!candidateCQs.isEmpty()) {
            ConjunctiveQuery Q = candidateCQs.stream()
                .sorted((cq1, cq2) -> {
                    Set<String> features1 = cq1.getProjections().stream()
                        .map(a1 -> featuresPerAttribute.get(a1))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                    Set<String> features2 = cq2.getProjections().stream()
                        .map(a2 -> featuresPerAttribute.get(a2))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                    
                    Set<String> conceptFeatures = featuresPerConceptInQuery.get(c);
                    if (conceptFeatures == null) conceptFeatures = Sets.newHashSet();
                    
                    return Integer.compare(
                        Sets.intersection(conceptFeatures, features1).size(),
                        Sets.intersection(conceptFeatures, features2).size()
                    );
                })
                .reduce((first, second) -> second)
                .orElse(null);
            
            if (Q == null) break;
            
            candidateCQs.remove(Q);
            BasicPattern phi = new BasicPattern();
            F.forEach(f -> phi.add(new Triple(
                new ResourceImpl(c).asNode(),
                new PropertyImpl(HAS_FEATURE).asNode(),
                new ResourceImpl(f).asNode()
            )));
            
            getCoveringCQs(phi, Q, candidateCQs, coveringCQs);
        }
        
        return coveringCQs;
    }

    /**
     * Recursively finds covering conjunctive queries
     */
    private static void getCoveringCQs(BasicPattern G, ConjunctiveQuery currentCQ, 
                                      Set<ConjunctiveQuery> candidateCQs, Set<ConjunctiveQuery> coveringCQs) {
        if (covering(currentCQ.getWrappers(), G)) {
            coveringCQs.add(currentCQ);
        } else if (!candidateCQs.isEmpty()) {
            ConjunctiveQuery CQ = candidateCQs.iterator().next();
            
            Set<String> currentFeatures = currentCQ.getProjections().stream()
                .map(a -> featuresPerAttribute.get(a))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            Set<String> contributedFeatures = CQ.getProjections().stream()
                .map(a -> featuresPerAttribute.get(a))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            
            if (!Sets.union(currentFeatures, contributedFeatures).equals(currentFeatures)) {
                ConjunctiveQuery newCQ = findJoins(currentCQ, CQ);
                getCoveringCQs(G, newCQ, Sets.difference(candidateCQs, Sets.newHashSet(CQ)), coveringCQs);
            }
        }
    }

    /**
     * Gets wrappers that cover an edge
     */
    private static Set<Wrapper> getEdgeCoveringWrappers(String s, String t, String e, Model model) {
        Set<Wrapper> coveringWrappers = Sets.newHashSet();
        ResultSet W = executeQuery(model, "SELECT ?g WHERE { GRAPH ?g { <" + s + "> <" + e + "> <" + t + "> } }");
        if (W != null) {
            W.forEachRemaining(wRes -> {
                String w = wRes.get("g").asResource().getURI();
                if (isWrapper(w)) {
                    coveringWrappers.add(new Wrapper(w));
                }
            });
        }
        return coveringWrappers;
    }

    /**
     * Main method to rewrite a SPARQL query to a union of conjunctive queries
     */
    public static QueryRewritingResult rewriteToUnionOfConjunctiveQueries(QueryStructure queryStructure, Model model) {
        BasicPattern PHI_p = queryStructure.getBasicGraphPattern();
        populateOptimizedStructures(model, PHI_p);
        InfModel PHI_o = queryStructure.getOntologyModel();

        // Identify query-related concepts
        Graph<String, RelationshipEdge> conceptsGraph = new SimpleDirectedGraph<>(RelationshipEdge.class);
        
        PHI_p.getList().forEach(t -> {
            if (!t.getPredicate().getURI().equals(HAS_FEATURE) && 
                !t.getObject().getURI().equals(SCHEMA_NAMESPACE + "identifier")) {
                conceptsGraph.addVertex(t.getSubject().getURI());
                conceptsGraph.addVertex(t.getObject().getURI());
                conceptsGraph.addEdge(t.getSubject().getURI(), t.getObject().getURI(),
                    new RelationshipEdge(t.getPredicate().getURI()));
            }
        });
        
        // This is required when only one concept is queried, where all edges are hasFeature
        if (conceptsGraph.vertexSet().isEmpty()) {
            conceptsGraph.addVertex(PHI_p.getList().get(0).getSubject().getURI());
        }

        IntegrationGraph G = new IntegrationGraph();
        conceptsGraph.vertexSet().forEach(c -> {
            Set<ConjunctiveQuery> CQs = getConceptCoveringCQs(c, PHI_o, model);
            G.addVertex(new CQVertex(c, CQs));
        });
        
        conceptsGraph.edgeSet().forEach(e -> {
            CQVertex source = G.vertexSet().stream()
                .filter(v -> v.getLabel().equals(conceptsGraph.getEdgeSource(e)))
                .findFirst()
                .orElse(null);
            CQVertex target = G.vertexSet().stream()
                .filter(v -> v.getLabel().equals(conceptsGraph.getEdgeTarget(e)))
                .findFirst()
                .orElse(null);
            
            if (source != null && target != null) {
                Set<Wrapper> wrappers = getEdgeCoveringWrappers(source.getLabel(), target.getLabel(), e.getLabel(), model);
                G.addEdge(source, target, new IntegrationEdge(e.getLabel(), wrappers));
            }
        });

        // Define a data structure D: CQVertex --> BGP
        Map<CQVertex, BasicPattern> D = Maps.newHashMap();
        PHI_p.forEach(t -> {
            if (t.getPredicate().getURI().equals(HAS_FEATURE)) {
                CQVertex vertex = new CQVertex(t.getSubject().getURI(), Sets.newHashSet());
                D.putIfAbsent(vertex, new BasicPattern());
                D.get(vertex).add(t);
            }
        });

        // Main algorithm: merge vertices until no edges remain
        while (!G.edgeSet().isEmpty()) {
            IntegrationEdge e = G.edgeSet().iterator().next();
            CQVertex source = G.getEdgeSource(e);
            CQVertex target = G.getEdgeTarget(e);

            Set<Wrapper> edgeCoveringWrappers = e.getWrappers();
            Set<ConjunctiveQuery> Qs = source.getCQs();
            Set<ConjunctiveQuery> Qt = target.getCQs();

            BasicPattern both = new BasicPattern();
            both.addAll(D.get(source));
            both.addAll(D.get(target));
            
            // Add the edge connecting the concepts
            addTriple(both, source.getLabel(), e.getLabel(), target.getLabel());

            Set<ConjunctiveQuery> Q = combineSetsOfCQs(Qs, Qt, edgeCoveringWrappers, both);

            String newLabel = source.getLabel() + "-" + target.getLabel();
            CQVertex joinedVertex = new CQVertex(newLabel, Q);

            // Update D with the new label
            D.put(joinedVertex, both);

            // Remove the processed edge
            G.removeEdge(e);
            
            // Add the new vertex to the graph
            G.addVertex(joinedVertex);
            
            // Create edges to the new vertex from those neighbors of source and target
            Graphs.neighborSetOf(G, source).forEach(neighbor -> {
                if (!source.equals(neighbor)) {
                    if (G.containsEdge(source, neighbor)) {
                        IntegrationEdge connectingEdge = G.getEdge(source, neighbor);
                        G.removeEdge(connectingEdge);
                        G.addEdge(joinedVertex, neighbor, connectingEdge);
                    } else if (G.containsEdge(neighbor, source)) {
                        IntegrationEdge connectingEdge = G.getEdge(neighbor, source);
                        G.removeEdge(connectingEdge);
                        G.addEdge(neighbor, joinedVertex, connectingEdge);
                    }
                }
            });
            
            Graphs.neighborListOf(G, target).forEach(neighbor -> {
                if (!target.equals(neighbor)) {
                    if (G.containsEdge(target, neighbor)) {
                        IntegrationEdge connectingEdge = G.getEdge(target, neighbor);
                        G.removeEdge(connectingEdge);
                        G.addEdge(joinedVertex, neighbor, connectingEdge);
                    } else if (G.containsEdge(neighbor, target)) {
                        IntegrationEdge connectingEdge = G.getEdge(neighbor, target);
                        G.removeEdge(connectingEdge);
                        G.addEdge(neighbor, joinedVertex, connectingEdge);
                    }
                }
            });
            
            G.removeVertex(source);
            G.removeVertex(target);
        }

        // Return the final result
        if (!G.vertexSet().isEmpty()) {
            CQVertex finalVertex = G.vertexSet().iterator().next();
            return new QueryRewritingResult(1, finalVertex.getCQs());
        } else {
            return new QueryRewritingResult(0, Sets.newHashSet());
        }
    }

    /**
     * Parses a SPARQL query into its structure
     */
    public static QueryStructure parseSPARQL(String SPARQL, Model model) {
        // Compile the SPARQL using ARQ and generate its <pi,phi> representation
        Query q = QueryFactory.create(SPARQL);
        Op ARQ = Algebra.compile(q);

        Set<String> PI = Sets.newHashSet();
        if (ARQ instanceof OpProject) {
            OpProject opProject = (OpProject) ARQ;
            if (opProject.getSubOp() instanceof OpJoin) {
                OpJoin opJoin = (OpJoin) opProject.getSubOp();
                if (opJoin.getLeft() instanceof OpTable) {
                    OpTable opTable = (OpTable) opJoin.getLeft();
                    opTable.getTable().rows().forEachRemaining(r -> {
                        r.vars().forEachRemaining(v -> PI.add(r.get(v).getURI()));
                    });
                }
            }
        }

        BasicPattern PHI_p = new BasicPattern();
        if (ARQ instanceof OpProject) {
            OpProject opProject = (OpProject) ARQ;
            if (opProject.getSubOp() instanceof OpJoin) {
                OpJoin opJoin = (OpJoin) opProject.getSubOp();
                if (opJoin.getRight() instanceof OpBGP) {
                    OpBGP opBGP = (OpBGP) opJoin.getRight();
                    PHI_p = opBGP.getPattern();
                }
            }
        }

        OntModel PHI_o_ontmodel = ontologyFromPattern(PHI_p);
        Reasoner reasoner = ReasonerRegistry.getRDFSReasoner(); // RDFS entailment subclass+superclass
        InfModel PHI_o = ModelFactory.createInfModel(reasoner, PHI_o_ontmodel);

        return new QueryStructure(PI, PHI_p, PHI_o);
    }

    @Override
    public void generateQueryingStructures(IntegratedGraphJenaImpl IG, List<Dataset> datasetsInQuery) {
        // This method is called from the interface but the main logic is in the static methods above
        // The actual query rewriting is done through the static methods
        System.out.println("Query rewriting structures generated for " + datasetsInQuery.size() + " datasets");
    }

    // Helper class for wrapper-feature pairs
    private static class WrapperFeaturePair {
        private final Wrapper wrapper;
        private final String feature;

        public WrapperFeaturePair(Wrapper wrapper, String feature) {
            this.wrapper = wrapper;
            this.feature = feature;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WrapperFeaturePair that = (WrapperFeaturePair) o;
            return wrapper.equals(that.wrapper) && feature.equals(that.feature);
        }

        @Override
        public int hashCode() {
            return Objects.hash(wrapper, feature);
        }
    }
}