package edu.upc.essi.dtim.odin.integration;

import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaDI;
import edu.upc.essi.dtim.nextiadi.models.Alignment;
import edu.upc.essi.dtim.odin.NextiaGraphy.nextiaGraphyModuleImpl;
import edu.upc.essi.dtim.odin.NextiaGraphy.nextiaGraphyModuleInterface;
import edu.upc.essi.dtim.odin.NextiaGraphy.vocabulary.Namespaces;
import edu.upc.essi.dtim.odin.integration.pojos.JoinAlignment;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

import java.util.List;
import java.util.stream.Collectors;

public class integrationModuleImpl implements integrationModuleInterface{
    @Override
    public Graph integrate(Graph graphA, Graph graphB, List<Alignment> alignments) {
        Graph integratedGraph = CoreGraphFactory.createGraphInstance("normal");

        NextiaDI n = new NextiaDI();

        integratedGraph.setGraph(
                n.Integrate(retrieveSourceGraph(alignments, graphA), retrieveSourceGraph(alignments, graphB), alignments)
        );

        return integratedGraph;
    }

    @Override
    public List<Alignment> getUnused(Graph graphA, Graph graphB, List<Alignment> alignments) {
        NextiaDI n = new NextiaDI();
        n.Integrate(retrieveSourceGraph(alignments, graphA), retrieveSourceGraph(alignments, graphB), alignments);
        return n.getUnused();
    }

    @Override
    public Graph globalGraph(Graph graphA, Graph graphB, List<Alignment> alignments) {
        Graph globalGraph = CoreGraphFactory.createGraphInstance("normal");

        NextiaDI n = new NextiaDI();

        n.Integrate(retrieveSourceGraph(alignments, graphA), retrieveSourceGraph(alignments, graphB), alignments);

        globalGraph.setGraph(
                n.getMinimalGraph2()
        );

        return globalGraph;
    }

    @Override
    public Graph joinIntegration(Graph integratedGraph, List<JoinAlignment> joinAlignments) {
        Graph joinGraph = CoreGraphFactory.createGraphInstance("normal");
        Graph schemaIntegration = CoreGraphFactory.createGraphInstance("normal");
        NextiaDI n = new NextiaDI();

        for(JoinAlignment a : joinAlignments) {

            if(a.getRightArrow())
                schemaIntegration.setGraph(n.JoinIntegration(integratedGraph.getGraph(), a.getIriA(), a.getIriB(), a.getL(), a.getRelationship(), a.getDomainA(), a.getDomainB()));
            else
                schemaIntegration.setGraph(n.JoinIntegration(integratedGraph.getGraph(), a.getIriA(), a.getIriB(), a.getL(), a.getRelationship(), a.getDomainB(), a.getDomainA()));
        }

        joinGraph.setGraph(
                schemaIntegration.getGraph()
        );

        return joinGraph;
    }

    public Graph generateGlobalGraph(Graph graph){
        Graph globalGraph = CoreGraphFactory.createGlobalGraph();

        NextiaDI n = new NextiaDI();
        globalGraph.setGraph(n.generateMinimalGraph(graph.getGraph()));

        nextiaGraphyModuleInterface visualLibInterface = new nextiaGraphyModuleImpl();
        //globalGraph.setGraphicalSchema("{\"nodes\":[{\"id\":\"Class1\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_2\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"shortType\":\"rdfs:Class\",\"type\":\"class\",\"label\":\"quiz\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Property3\",\"iri\":\"http://www.w3.org/2000/01/rdf-schema#subClassOf\",\"type\":\"object\",\"label\":\"subClassOf\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_4\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/xa_q1\",\"isIntegrated\":false,\"linkId\":\"Link1\"},{\"id\":\"Class2\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_4\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"shortType\":\"rdfs:Class\",\"type\":\"class\",\"label\":\"q1\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Class4\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/352/marcas\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"object\",\"label\":\"has marcas\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/352/Object_1\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/352/Object_2\",\"isIntegrated\":false,\"linkId\":\"Link2\"},{\"id\":\"Class5\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/options3\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"datatype\",\"label\":\"has options3\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_7\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"isIntegrated\":false,\"linkId\":\"Link3\"},{\"id\":\"Class7\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/question2\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"datatype\",\"label\":\"question2\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_6\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"isIntegrated\":false,\"linkId\":\"Link4\"},{\"id\":\"Class8\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/answer3\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"datatype\",\"label\":\"answer3\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_7\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"isIntegrated\":false,\"linkId\":\"Link5\"},{\"id\":\"Class9\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_6\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"shortType\":\"rdfs:Class\",\"type\":\"class\",\"label\":\"q1\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Class10\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/answer\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"datatype\",\"label\":\"answer\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_4\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"isIntegrated\":false,\"linkId\":\"Link6\"},{\"id\":\"Class11\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/question\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"datatype\",\"label\":\"question\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_4\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"isIntegrated\":false,\"linkId\":\"Link7\"},{\"id\":\"Class12\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/options\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"datatype\",\"label\":\"has options\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_4\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"isIntegrated\":false,\"linkId\":\"Link8\"},{\"id\":\"Class13\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/352/modelos\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"datatype\",\"label\":\"has modelos\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/352/Object_2\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"isIntegrated\":false,\"linkId\":\"Link9\"},{\"id\":\"Class14\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/maths\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"object\",\"label\":\"has maths\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_2\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_5\",\"isIntegrated\":false,\"linkId\":\"Link10\"},{\"id\":\"Class15\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/352/Object_2\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"shortType\":\"rdfs:Class\",\"type\":\"class\",\"label\":\"Array_1\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Class16\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/q1\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"object\",\"label\":\"has q1\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_3\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_4\",\"isIntegrated\":false,\"linkId\":\"Link11\"},{\"id\":\"Class17\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/sport\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"object\",\"label\":\"has sport\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_2\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_3\",\"isIntegrated\":false,\"linkId\":\"Link12\"},{\"id\":\"Class18\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_1\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"shortType\":\"rdfs:Class\",\"type\":\"class\",\"label\":\"as\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Class19\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_3\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"shortType\":\"rdfs:Class\",\"type\":\"class\",\"label\":\"sport\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Class20\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/question3\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"datatype\",\"label\":\"question3\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_7\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"isIntegrated\":false,\"linkId\":\"Link13\"},{\"id\":\"Class21\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/352/id\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"datatype\",\"label\":\"id\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/352/Object_2\",\"range\":\"http://www.w3.org/2001/XMLSchema#int\",\"isIntegrated\":false,\"linkId\":\"Link14\"},{\"id\":\"Class22\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/options2\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"datatype\",\"label\":\"has options2\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_6\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"isIntegrated\":false,\"linkId\":\"Link15\"},{\"id\":\"Class23\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/xa_q1\",\"iriType\":\"http://www.essi.upc.edu/DTIM/NextiaDI/IntegratedClass\",\"shortType\":\"nextia:IntegratedClass\",\"type\":\"class\",\"label\":\"xa_q1\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":true},{\"id\":\"Class24\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_5\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"shortType\":\"rdfs:Class\",\"type\":\"class\",\"label\":\"maths\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Class26\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_7\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"shortType\":\"rdfs:Class\",\"type\":\"class\",\"label\":\"q2\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Class27\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/answer2\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"datatype\",\"label\":\"answer2\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_6\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"isIntegrated\":false,\"linkId\":\"Link16\"},{\"id\":\"Property29\",\"iri\":\"http://www.w3.org/2000/01/rdf-schema#subClassOf\",\"type\":\"object\",\"label\":\"subClassOf\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/352/Object_1\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/xa_q1\",\"isIntegrated\":false,\"linkId\":\"Link17\"},{\"id\":\"Class28\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/352/Object_1\",\"iriType\":\"http://www.w3.org/2000/01/rdf-schema#Class\",\"shortType\":\"rdfs:Class\",\"type\":\"class\",\"label\":\"xa\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Class30\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/q2\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"object\",\"label\":\"has q2\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_5\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_7\",\"isIntegrated\":false,\"linkId\":\"Link18\"},{\"id\":\"Class31\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/352/nombre\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"datatype\",\"label\":\"nombre\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/352/Object_2\",\"range\":\"http://www.w3.org/2001/XMLSchema#string\",\"isIntegrated\":false,\"linkId\":\"Link19\"},{\"id\":\"Class32\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/quiz\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"object\",\"label\":\"has quiz\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_1\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_2\",\"isIntegrated\":false,\"linkId\":\"Link20\"},{\"id\":\"Class33\",\"iri\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/q12\",\"iriType\":\"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property\",\"shortType\":\"rdf:Property\",\"type\":\"object\",\"label\":\"has q12\",\"domain\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_5\",\"range\":\"http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/355/Object_6\",\"isIntegrated\":false,\"linkId\":\"Link21\"},{\"id\":\"Datatype34\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"shortType\":\"xsd:String\",\"type\":\"xsdType\",\"label\":\"string\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Datatype35\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"shortType\":\"xsd:String\",\"type\":\"xsdType\",\"label\":\"string\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Datatype36\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"shortType\":\"xsd:String\",\"type\":\"xsdType\",\"label\":\"string\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Datatype37\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"shortType\":\"xsd:String\",\"type\":\"xsdType\",\"label\":\"string\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Datatype38\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"shortType\":\"xsd:String\",\"type\":\"xsdType\",\"label\":\"string\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Datatype39\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"shortType\":\"xsd:String\",\"type\":\"xsdType\",\"label\":\"string\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Datatype40\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"shortType\":\"xsd:String\",\"type\":\"xsdType\",\"label\":\"string\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Datatype41\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"shortType\":\"xsd:String\",\"type\":\"xsdType\",\"label\":\"string\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Datatype42\",\"iri\":\"http://www.w3.org/2001/XMLSchema#int\",\"shortType\":\"xsd:String\",\"type\":\"xsdType\",\"label\":\"int\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Datatype43\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"shortType\":\"xsd:String\",\"type\":\"xsdType\",\"label\":\"string\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Datatype44\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"shortType\":\"xsd:String\",\"type\":\"xsdType\",\"label\":\"string\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false},{\"id\":\"Datatype45\",\"iri\":\"http://www.w3.org/2001/XMLSchema#string\",\"shortType\":\"xsd:String\",\"type\":\"xsdType\",\"label\":\"string\",\"domain\":\"\",\"range\":\"\",\"isIntegrated\":false}],\"links\":[{\"id\":\"Link1\",\"nodeId\":\"Property3\",\"source\":\"Class2\",\"target\":\"Class23\",\"label\":\"subClassOf\"},{\"id\":\"Link2\",\"nodeId\":\"Class4\",\"source\":\"Class28\",\"target\":\"Class15\",\"label\":\"has marcas\"},{\"id\":\"Link3\",\"nodeId\":\"Class5\",\"source\":\"Class26\",\"target\":\"Datatype34\",\"label\":\"has options3\"},{\"id\":\"Link4\",\"nodeId\":\"Class7\",\"source\":\"Class9\",\"target\":\"Datatype35\",\"label\":\"question2\"},{\"id\":\"Link5\",\"nodeId\":\"Class8\",\"source\":\"Class26\",\"target\":\"Datatype36\",\"label\":\"answer3\"},{\"id\":\"Link6\",\"nodeId\":\"Class10\",\"source\":\"Class2\",\"target\":\"Datatype37\",\"label\":\"answer\"},{\"id\":\"Link7\",\"nodeId\":\"Class11\",\"source\":\"Class2\",\"target\":\"Datatype38\",\"label\":\"question\"},{\"id\":\"Link8\",\"nodeId\":\"Class12\",\"source\":\"Class2\",\"target\":\"Datatype39\",\"label\":\"has options\"},{\"id\":\"Link9\",\"nodeId\":\"Class13\",\"source\":\"Class15\",\"target\":\"Datatype40\",\"label\":\"has modelos\"},{\"id\":\"Link10\",\"nodeId\":\"Class14\",\"source\":\"Class1\",\"target\":\"Class24\",\"label\":\"has maths\"},{\"id\":\"Link11\",\"nodeId\":\"Class16\",\"source\":\"Class19\",\"target\":\"Class2\",\"label\":\"has q1\"},{\"id\":\"Link12\",\"nodeId\":\"Class17\",\"source\":\"Class1\",\"target\":\"Class19\",\"label\":\"has sport\"},{\"id\":\"Link13\",\"nodeId\":\"Class20\",\"source\":\"Class26\",\"target\":\"Datatype41\",\"label\":\"question3\"},{\"id\":\"Link14\",\"nodeId\":\"Class21\",\"source\":\"Class15\",\"target\":\"Datatype42\",\"label\":\"id\"},{\"id\":\"Link15\",\"nodeId\":\"Class22\",\"source\":\"Class9\",\"target\":\"Datatype43\",\"label\":\"has options2\"},{\"id\":\"Link16\",\"nodeId\":\"Class27\",\"source\":\"Class9\",\"target\":\"Datatype44\",\"label\":\"answer2\"},{\"id\":\"Link17\",\"nodeId\":\"Property29\",\"source\":\"Class28\",\"target\":\"Class23\",\"label\":\"subClassOf\"},{\"id\":\"Link18\",\"nodeId\":\"Class30\",\"source\":\"Class24\",\"target\":\"Class26\",\"label\":\"has q2\"},{\"id\":\"Link19\",\"nodeId\":\"Class31\",\"source\":\"Class15\",\"target\":\"Datatype45\",\"label\":\"nombre\"},{\"id\":\"Link20\",\"nodeId\":\"Class32\",\"source\":\"Class18\",\"target\":\"Class1\",\"label\":\"has quiz\"},{\"id\":\"Link21\",\"nodeId\":\"Class33\",\"source\":\"Class24\",\"target\":\"Class9\",\"label\":\"has q12\"}]}");
        globalGraph.setGraphicalSchema(visualLibInterface.generateVisualGraph(globalGraph));

        return globalGraph;
    }


    public Model retrieveSourceGraph(List<Alignment> alignments, Graph graph) {
        // Todo think in a better way to do this. Maybe identifiers should be declared when loading data
        List<Alignment> aligId= alignments.stream().filter(x -> x.getType().contains("datatype")  ).collect(Collectors.toList());;

        Model sourceG = graph.getGraph();

        for ( Alignment a : aligId) {
            Resource rA = sourceG.createResource(a.getIriA());
            Resource rB = sourceG.createResource(a.getIriB());

            if (sourceG.containsResource(rA) ) {
                graph.addTriple(rA.getURI(), RDFS.subClassOf.getURI(),Namespaces.SCHEMA.val()+"identifier");
            } else {
                graph.addTriple(rB.getURI(), RDFS.subClassOf.getURI(), Namespaces.SCHEMA.val()+"identifier");
            }
        }

        sourceG = graph.getGraph();
        return  sourceG;
    }
}
