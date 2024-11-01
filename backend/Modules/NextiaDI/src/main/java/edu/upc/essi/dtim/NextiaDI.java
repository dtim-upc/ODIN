package edu.upc.essi.dtim;

import edu.upc.essi.dtim.nextiadi.config.Namespaces;
import edu.upc.essi.dtim.nextiadi.config.Vocabulary;
import edu.upc.essi.dtim.nextiadi.exceptions.NoDomainForPropertyException;
import edu.upc.essi.dtim.nextiadi.exceptions.NoRangeForPropertyException;
import edu.upc.essi.dtim.nextiadi.jena.Graph;
import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NextiaDI {

    Graph graphO;
    List<Alignment> unused;

    public NextiaDI(){
       reset();
    }

    public void reset(){

        graphO  = new Graph();
        unused = new ArrayList<>();
    }


    public Model Integrate(Model graphA, Model graphB, List<Alignment> alignments){

        List<Alignment> classes = alignments.stream().filter( a -> a.getType().toLowerCase().contains("class") ).collect(Collectors.toList());
        List<Alignment> datatypes = alignments.stream().filter( a -> a.getType().toLowerCase().contains("datatype") ).collect(Collectors.toList());
        List<Alignment> properties = alignments.stream().filter( a -> a.getType().toLowerCase().contains("object") ).collect(Collectors.toList());

        return Integrate(graphA, graphB, classes, datatypes, properties, new ArrayList<>());
    }


    public Model Integrate(Model graphA, Model graphB, List<Alignment> alignments, List<Alignment> unused){

        List<Alignment> classes = alignments.stream().filter( a -> a.getType().toLowerCase().contains("class") ).collect(Collectors.toList());
        List<Alignment> datatypes = alignments.stream().filter( a -> a.getType().toLowerCase().contains("datatype") ).collect(Collectors.toList());
        List<Alignment> properties = alignments.stream().filter( a -> a.getType().toLowerCase().contains("object") ).collect(Collectors.toList());

        return  Integrate(graphA, graphB, classes, datatypes, properties, unused);
    }

    public Model Integrate(Model graphA, Model graphB, List<Alignment> Ac, List<Alignment> ADT, List<Alignment> AO, List<Alignment> unused ) {
        reset();
        this.unused = unused;

        Model integratedGraph = graphA.union(graphB);
        graphO.setModel(integratedGraph);

        IntegrateClasses( Ac );
        IntegrateDatatypeProperties(ADT);
        IntegrateObjectProperties(AO);
        return graphO.getModel();
    }

    public List<Alignment> getUnused(){
        return unused;
    }

    public void IntegrateClasses( List<Alignment> Ac) {

        for (Alignment a : Ac) {

            if( graphO.isIntegratedClass(a.getIriA() ) & graphO.isIntegratedClass(a.getIriB()) ) {
                graphO.replaceIntegratedClass(a);
            } else if ( graphO.isIntegratedClass(a.getIriA()  ) ) {
                graphO.add(a.getIriB(), RDFS.subClassOf.getURI(), a.getIriA());
            } else if ( graphO.isIntegratedClass(a.getIriB() ) ) {
                graphO.add(a.getIriA(), RDFS.subClassOf.getURI(), a.getIriB());
            } else {
                graphO.add(a.getIriL(), RDF.type.getURI(), Vocabulary.IntegrationClass.val());
                graphO.add(a.getIriA(), RDFS.subClassOf.getURI(), a.getIriL());
                graphO.add(a.getIriB(), RDFS.subClassOf.getURI(), a.getIriL());
                graphO.addLiteral(a.getIriL(), RDFS.label.getURI(), a.getL());
            }
            unused = performConcordanceProperties( graphO.getUnusedPropertiesReadyToIntegrate(a.getIriL(), unused)) ;
        }

    }

    public List<Alignment> performConcordanceProperties(Map<String,List<Alignment>> map  ) {

        List<Alignment> a = map.get("datatype");

        IntegrateDatatypeProperties(a);
        a = map.get("object");
        return IntegrateObjectProperties(a);


    }


    public List<Alignment> IntegrateDatatypeProperties(List<Alignment> ADP) {

        for ( Alignment a: ADP ) {

            String domainA = null;
            String domainB = null;
            try {
                domainA = graphO.getSuperDomainFromProperty(a.getIriA());
                domainB = graphO.getSuperDomainFromProperty(a.getIriB());
            } catch (NoDomainForPropertyException e) {
                e.printStackTrace();
                System.exit(1);
            }


            if( graphO.isIntegratedClass(domainA) & domainA.equals(domainB) ) {

                if( graphO.isIntegratedDatatypeProperty(a.getIriA()) & graphO.isIntegratedDatatypeProperty(a.getIriB()) ) {
                    //TODO:update label
                    graphO.replaceIntegratedProperty(a);
                } else if ( graphO.isIntegratedDatatypeProperty(a.getIriA()) ) {
                    graphO.add(a.getIriB(), RDFS.subPropertyOf.getURI(), a.getIriA() );
                } else if ( graphO.isIntegratedDatatypeProperty(a.getIriB()) ) {
                    graphO.add(a.getIriA(), RDFS.subPropertyOf.getURI(), a.getIriB() );
                } else {
                    graphO.add(a.getIriL(), RDF.type.getURI(), Vocabulary.IntegrationDProperty.val());

                    graphO.add(a.getIriA(), RDFS.subPropertyOf.getURI(), a.getIriL());
                    graphO.add(a.getIriB(), RDFS.subPropertyOf.getURI(), a.getIriL());

                    // TODO: compare the two ranges and choose the more flexible. E.g. xsd:string
                    String range = graphO.getFlexibleRange(a);
                    graphO.add(a.getIriL(), RDFS.range.getURI(), range);
                    graphO.add(a.getIriL(), RDFS.domain.getURI(), domainA);
                    graphO.addLiteral(a.getIriL(), RDFS.label.getURI(), a.l);
                }

            } else {
                unused.add(a);
            }

        }
        return unused;
    }

    public Model JoinIntegration(String propertyA, String propertyB, String integratedLabel, String labelObject, String domainO, String rangeO) {

        Alignment a = new Alignment();
        a.setL(integratedLabel);
        a.setIriB(propertyB);
        a.setIriA(propertyA);

        // for this type, we don't verify if domains are integrated, as this is join.
        String usedIRI ="";
        usedIRI = a.getIriL();
        if( graphO.isIntegratedDatatypeProperty(a.getIriA()) & graphO.isIntegratedDatatypeProperty(a.getIriB()) ) {
            //TODO:update label
            graphO.replaceIntegratedProperty(a);
        } else if ( graphO.isIntegratedDatatypeProperty(a.getIriA()) ) {
            graphO.add(a.getIriB(), RDFS.subPropertyOf.getURI(), a.getIriA() );
            usedIRI = a.getIriA();
        } else if ( graphO.isIntegratedDatatypeProperty(a.getIriB()) ) {
            graphO.add(a.getIriA(), RDFS.subPropertyOf.getURI(), a.getIriB() );
            usedIRI = a.getIriB();
        } else {
            graphO.add(a.getIriL(), RDF.type.getURI(), Vocabulary.IntegrationDProperty.val());
            graphO.addLiteral(a.getIriL(), RDFS.label.getURI(), integratedLabel);
            graphO.add(a.getIriA(), RDFS.subPropertyOf.getURI(), a.getIriL());
            graphO.add(a.getIriB(), RDFS.subPropertyOf.getURI(), a.getIriL());
            String range = graphO.getFlexibleRange(a);
            graphO.add(a.getIriL(), RDFS.range.getURI(), range);
            graphO.add(a.getIriL(), RDFS.domain.getURI(), domainO);
        }
        // TODO: handle propagation
        // for easy handle in odin, we are typing to JoinObjectProperty
        graphO.add( Namespaces.NextiaDI.val() + labelObject, RDF.type.getURI(), Vocabulary.JoinObjectProperty.val());
        graphO.addLiteral( Namespaces.NextiaDI.val() + labelObject, RDFS.label.getURI(), labelObject);
        graphO.add( Namespaces.NextiaDI.val() + labelObject, RDFS.range.getURI(), rangeO);
        graphO.add( Namespaces.NextiaDI.val() + labelObject, RDFS.domain.getURI(), domainO);
        graphO.add( usedIRI, Vocabulary.JoinProperty.val() , Namespaces.NextiaDI.val() + labelObject  );



        return graphO.getModel();
    }

    public Model JoinIntegration(Model integrated, String propertyA, String propertyB, String integratedLabel, String labelObject, String domainO, String rangeO) {

        graphO.setModel(integrated);
        return JoinIntegration(propertyA,  propertyB,  integratedLabel,  labelObject,  domainO, rangeO);
    }

    public List<Alignment> IntegrateObjectProperties(List<Alignment> ADP) {

        for ( Alignment a: ADP ) {

            String domainA = null;
            String domainB = null;
            try {
                domainA = graphO.getSuperDomainFromProperty(a.getIriA());
                domainB = graphO.getSuperDomainFromProperty(a.getIriB());
            } catch (NoDomainForPropertyException e) {
                e.printStackTrace();
                System.exit(1);
            }
            String rangeA = null;
            String rangeB = null;
            try{
                rangeA = graphO.getSuperRangeFromProperty(a.getIriA());
                rangeB = graphO.getSuperRangeFromProperty(a.getIriB());
            } catch (NoRangeForPropertyException e){
                e.printStackTrace();
                System.exit(1);
            }



            if( graphO.isIntegratedClass(domainA) & domainA.equals(domainB) & graphO.isIntegratedClass(rangeA) & rangeA.equals(rangeB) ) {

                if( graphO.isIntegratedDatatypeProperty(a.getIriA()) & graphO.isIntegratedDatatypeProperty(a.getIriB()) ) {
                    graphO.replaceIntegratedProperty(a);
                } else if ( graphO.isIntegratedDatatypeProperty(a.getIriA()) ) {
                    graphO.add(a.getIriB(), RDFS.subPropertyOf.getURI(), a.getIriA() );
                } else if ( graphO.isIntegratedDatatypeProperty(a.getIriB()) ) {
                    graphO.add(a.getIriA(), RDFS.subPropertyOf.getURI(), a.getIriB() );
                } else {
                    graphO.add(a.getIriL(), RDF.type.getURI(), Vocabulary.IntegrationOProperty.val());
                    graphO.add(a.getIriA(), RDFS.subPropertyOf.getURI(), a.getIriL());
                    graphO.add(a.getIriB(), RDFS.subPropertyOf.getURI(), a.getIriL());

                    graphO.add(a.getIriL(), RDFS.range.getURI(), rangeA);
                    graphO.add(a.getIriL(), RDFS.domain.getURI(), domainA);
                }
            } else {
                unused.add(a);
            }



        }
        return unused;
    }

    public Model getOnlyIntegrationResources(){
        return graphO.generateOnlyIntegrations();
    }


    // This function generates the globalGraph (minimalGraph) from the integrated one or a previous one
    public Model generateMinimalGraph(Model integratedGraph){
        Graph minimalG = new Graph();

        minimalG.setModel(integratedGraph);
        minimalG.minimalOverClasses();
        minimalG.minimalOverDataProperties();
        return minimalG.getModel();
    }


//    public void findProperties(Class<?> concept) {
//        Method[] x = concept.getDeclaredMethods();
//        System.out.println(x.toString());
//
//        Class<?> superclass = concept.getSuperclass();
//
//        Class<?>[] interfaces = concept.getInterfaces();
//        System.out.println(interfaces.toString());
//    }


    public static void main(String[] args) throws FileNotFoundException {

        NextiaDI n = new NextiaDI();

        Model graphA = RDFDataMgr.loadModel("/Users/javierflores/Documents/upc/projects/NextiaDI/source/source_schemas/prueba1_haross.ttl") ;
        Model graphB = RDFDataMgr.loadModel("/Users/javierflores/Documents/upc/projects/NextiaDI/source/source_schemas/prueba2_haross.ttl") ;

        List<Alignment> al = new ArrayList<>();
        Alignment a = new Alignment();
        a.setType("class");
        a.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/359835e0cff94c5da6886eac2bb05992/prueba1");
        a.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/dbae6a8d27214912aaa7068d8008f321/prueba2");
        a.setL("IT_PRUEBA");
        al.add(a);
//        a.setIdentifier(true);

        Model integratedModel = n.Integrate(graphA, graphB, al);
        Model minimal = n.generateMinimalGraph(integratedModel);

        try {
            RDFDataMgr.write(new FileOutputStream("/Users/javierflores/Documents/upc/projects/NextiaDI/source/source_schemas/minimal.ttl"), minimal, Lang.TURTLE);
            System.out.println("file written temporal");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



//
//        IntegrationClass in = new IntegrationClass();
//        Class<?> proxy = in.getClass();
//
//        NextiaDI main = new NextiaDI();
//        main.findProperties(proxy);


//        Model m = ModelFactory.createDefaultModel();

//        JenaModel jena = new JenaModel();
//        jena.setModel(m);
//        jena.add("http://javi.com/s1","http://javi.com/p1","http://javi.com/o1" );
//        jena.add("http://javi.com/s1","http://javi.com/p2","http://javi.com/o2" );
//        jena.add("http://javi.com/s2","http://javi.com/p3","http://javi.com/s1" );
//        jena.deleteResource("http://javi.com/s1");
////        jena.addL("http://javi.com/s1","http://javi.com/p1","http://javi.com/o1" );
//
////        m.add(
////                new ResourceImpl("http://javi.com/s1"),
////                new PropertyImpl("http://javi.com/p1"),
////                new ResourceImpl("http://javi.com/o1"));
//
//
//        RDFDataMgr.write(new FileOutputStream("/Users/javierflores/Documents/UPC Projects/nuupdi/src/main/resources/prueba.ttl"), m, Lang.TURTLE);
////        m.write(new FileOutputStream("/Users/javierflores/Documents/UPC Projects/nuupdi/src/main/resources/prueba.ttl"), "RDF/XML-ABBREV");
//
//        System.out.println("hola");

    }
}
