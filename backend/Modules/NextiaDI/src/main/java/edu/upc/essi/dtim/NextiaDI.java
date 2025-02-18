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

        Model graphA = RDFDataMgr.loadModel("/Users/anbipa/Desktop/DTIM/Cyclops-UC3/mitender_bs_1.ttl") ;
        Model graphB = RDFDataMgr.loadModel("/Users/anbipa/Desktop/DTIM/Cyclops-UC3/mitender_bs_2.ttl") ;

        List<Alignment> al = new ArrayList<>();

        Alignment a1 = new Alignment();
        a1.setType("class");
        a1.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/Object_1");
        a1.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/Object_1");
        a1.setL("IT_OBJECT_1");
        al.add(a1);

        Alignment a2 = new Alignment();
        a2.setType("datatype");
        a2.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/notice_issue_date");
        a2.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/notice_issue_date");
        a2.setL("IT_NOTICE_ISSUE_DATE");
        al.add(a2);

        Alignment a3 = new Alignment();
        a3.setType("datatype");
        a3.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/awarded_tender_tax_exclusive_amount");
        a3.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/awarded_tender_tax_exclusive_amount");
        a3.setL("IT_AWARDED_TENDER_TAX_EXCLUSIVE_AMOUNT");
        al.add(a3);

        Alignment a4 = new Alignment();
        a4.setType("datatype");
        a4.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/tax_exclusive_amount");
        a4.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/tax_exclusive_amount");
        a4.setL("IT_TAX_EXCLUSIVE_AMOUNT");
        al.add(a4);

        Alignment a5 = new Alignment();
        a5.setType("datatype");
        a5.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/title");
        a5.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/title");
        a5.setL("IT_TITLE");
        al.add(a5);

        Alignment a6 = new Alignment();
        a6.setType("datatype");
        a6.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/contracting_party_name");
        a6.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/contracting_party_name");
        a6.setL("IT_CONTRACTING_PARTY_NAME");
        al.add(a6);

        Alignment a7 = new Alignment();
        a7.setType("datatype");
        a7.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/contract_type_code");
        a7.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/contract_type_code");
        a7.setL("IT_CONTRACT_TYPE_CODE");
        al.add(a7);

        Alignment a8 = new Alignment();
        a8.setType("datatype");
        a8.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/procedure_code");
        a8.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/procedure_code");
        a8.setL("IT_PROCEDURE_CODE");
        al.add(a8);

        Alignment a9 = new Alignment();
        a9.setType("datatype");
        a9.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/urgency_name");
        a9.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/urgency_name");
        a9.setL("IT_URGENCY_NAME");
        al.add(a9);

        Alignment a10 = new Alignment();
        a10.setType("datatype");
        a10.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/lot_bool");
        a10.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/lot_bool");
        a10.setL("IT_LOT_BOOL");
        al.add(a10);

        Alignment a11 = new Alignment();
        a11.setType("datatype");
        a11.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/contract_type_name");
        a11.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/contract_type_name");
        a11.setL("IT_CONTRACT_TYPE_NAME");
        al.add(a11);

        Alignment a12 = new Alignment();
        a12.setType("datatype");
        a12.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/procedure_name");
        a12.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/procedure_name");
        a12.setL("IT_PROCEDURE_NAME");
        al.add(a12);

        Alignment a13 = new Alignment();
        a13.setType("datatype");
        a13.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/tender_submission_deadline_end_date");
        a13.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/tender_submission_deadline_end_date");
        a13.setL("IT_TENDER_SUBMISSION_DEADLINE_END_DATE");
        al.add(a13);

        Alignment a14 = new Alignment();
        a14.setType("datatype");
        a14.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/tenderers_number");
        a14.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/tenderers_number");
        a14.setL("IT_TENDERERS_NUMBER");
        al.add(a14);

        Alignment a15 = new Alignment();
        a15.setType("datatype");
        a15.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/tender_submission_deadline");
        a15.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/tender_submission_deadline");
        a15.setL("IT_TENDER_SUBMISSION_DEADLINE");
        al.add(a15);

        Alignment a16 = new Alignment();
        a16.setType("datatype");
        a16.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/expedient_number");
        a16.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/expedient_number");
        a16.setL("IT_EXPEDIENT_NUMBER");
        al.add(a16);

        Alignment a17 = new Alignment();
        a17.setType("datatype");
        a17.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/tax_exclusive_amount_in_millions");
        a17.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/tax_exclusive_amount_in_millions");
        a17.setL("IT_TAX_EXCLUSIVE_AMOUNT_IN_MILLIONS");
        al.add(a17);

        Alignment a18 = new Alignment();
        a18.setType("datatype");
        a18.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/lots_number");
        a18.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/lots_number");
        a18.setL("IT_LOTS_NUMBER");
        al.add(a18);

        Alignment a19 = new Alignment();
        a19.setType("datatype");
        a19.setIriA("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset1/subtraction_budget_awarded");
        a19.setIriB("http://www.essi.upc.edu/DTIM/NextiaDI/DataSource/Schema/mitenderDataset2/subtraction_budget_awarded");
        a19.setL("IT_SUBTRACTION_BUDGET_AWARDED");
        al.add(a19);

        Model integratedModel = n.Integrate(graphA, graphB, al);
        Model minimal = n.generateMinimalGraph(integratedModel);

        try {
            RDFDataMgr.write(new FileOutputStream("/Users/anbipa/Desktop/DTIM/Cyclops-UC3/minimal_mitender.ttl"), minimal, Lang.TURTLE);
            System.out.println("file written temporal");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // store integratedModel
        try {
            RDFDataMgr.write(new FileOutputStream("/Users/anbipa/Desktop/DTIM/Cyclops-UC3/integrated_mitender.ttl"), integratedModel, Lang.TURTLE);
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
