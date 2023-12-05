package edu.upc.essi.dtim.nextiadi.bootstraping;

import edu.upc.essi.dtim.nextiadi.config.DataSourceVocabulary;
import edu.upc.essi.dtim.nextiadi.config.Formats;
import edu.upc.essi.dtim.nextiadi.jena.Graph;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import javax.json.*;
import java.io.*;
import java.util.stream.Collectors;

/**
 * Generates an RDFS-compliant representation of a CSV file schema
 * @author snadal
 */
public class CSVBootstrap extends DataSource{

//	Graph Σ;


	public CSVBootstrap(){
		super();
	}


	public Model bootstrapSchema(String id, String name, String path) throws IOException {
		G_source = new Graph();
		this.id = id;

		BufferedReader br = new BufferedReader(new FileReader(path));
		CSVParser parser = CSVParser.parse(br, CSVFormat.DEFAULT.withFirstRecordAsHeader());

		G_source.add(createIRI(name), RDF.type, RDFS.Class);
		G_source.addLiteral(createIRI(name), RDFS.label, name);
		parser.getHeaderNames().forEach(h -> {
			String h2 = h.replace("\"", "").trim();
//			System.out.println(h2);
			G_source.add(createIRI(h2),RDF.type,RDF.Property);
			G_source.add(createIRI(h2),RDFS.domain,createIRI(name));
			G_source.add(createIRI(h2),RDFS.range,XSD.xstring);
			G_source.addLiteral(createIRI(h2), RDFS.label,h2 );
		});

		String select =  parser.getHeaderNames().stream().map(a ->{ return  a +" AS "+ a.replace(".","_"); }).collect(Collectors.joining(","));
		wrapper = "SELECT " + select  + " FROM " + name;
		addMetaData( name, id, path);
		G_source.getModel().setNsPrefixes(prefixes);
		return G_source.getModel();
	}

	private void addMetaData(String name, String id, String path){
		String ds = DataSourceVocabulary.DataSource.val() +"/" + name;
		if (!id.equals("")){
			ds = DataSourceVocabulary.DataSource.val() +"/" + id;
			G_source.addLiteral( ds , DataSourceVocabulary.HAS_ID.val(), id);
		}
		addBasicMetaData(name, path, ds);
		G_source.addLiteral( ds , DataSourceVocabulary.HAS_FORMAT.val(), Formats.CSV.val());
		G_source.addLiteral( ds , DataSourceVocabulary.HAS_WRAPPER.val(), wrapper);
	}

	public void write(String file, String lang){
		G_source.write(file,lang);
	}

	public static void main(String[] args) throws IOException {

		String pathcsv = "/Users/javierflores/Documents/datasets/1/artworks.csv";
		CSVBootstrap csv = new CSVBootstrap();
		Model m =csv.bootstrapSchema("12","artworks", pathcsv);
		m.write(System.out, "Turtle");
	}


//	public static void main(String[] args) throws IOException {
//		BufferedReader br = new BufferedReader(new FileReader("src/main/resources/cities.csv"));
//		CSVParser parser = CSVParser.parse(br, CSVFormat.DEFAULT.withFirstRecordAsHeader());
//
//		Dataset Σ = DatasetFactory.createTxnMem() ;
//		Σ.begin(ReadWrite.WRITE);
//
////		String P = "/cities.csv";
////		addTriple(Σ,"G",new ResourceImpl(P), RDF.type, RDFS.Class);
////		parser.getHeaderNames().forEach(h -> {
////			addTriple(Σ,"G",new ResourceImpl(P+"."+h),RDF.type,RDF.Property);
////			addTriple(Σ,"G", new ResourceImpl(P+"."+h),RDFS.domain,new ResourceImpl(P));
////			addTriple(Σ,"G", new ResourceImpl(P+"."+h),RDFS.range,XSD.xstring);
////		});
//
//		Σ.getNamedModel("G").write(new FileWriter("src/main/resources/cities.ttl"), "TTL");
//	}

//	private static void addTriple(Dataset d, String namedGraph, Resource s, Property p, Resource o){
//		Txn.executeWrite(d, ()-> {
//			Model graph = d.getNamedModel(namedGraph);
//			graph.add(s, p, o);
//		});
//	}

}

