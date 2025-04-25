package edu.upc.essi.dtim.nextiadi.bootstraping;


import edu.upc.essi.dtim.nextiadi.config.DataSourceVocabulary;
import edu.upc.essi.dtim.nextiadi.config.Formats;
import edu.upc.essi.dtim.nextiadi.jena.Graph;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import javax.json.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates an RDFS-compliant representation of a JSON's document schema
 * @author snadal
 */

public class JSONBootstrap extends DataSource{

	private int ObjectCounter = 1;
	private int SeqCounter = 1;
	private int CMPCounter = 1;

	private Graph G_source;
	//SparkSQL query to get a 1NF view of the file
	private String wrapper;
	//List of pairs, where left is the IRI in the graph and right is the attribute in the wrapper (this will create sameAs edges)
	private List<Pair<String,String>> sourceAttributes;

	private List<Pair<String,String>> attributes;
	private List<Pair<String,String>> lateralViews;

	public JSONBootstrap(){
		super();
		reset();
	}

	public int getObjectCounter() {
		return ObjectCounter;
	}

	public void setObjectCounter(int objectCounter) {
		ObjectCounter = objectCounter;
	}

	public int getSeqCounter() {
		return SeqCounter;
	}

	public void setSeqCounter(int seqCounter) {
		SeqCounter = seqCounter;
	}

	public int getCMPCounter() {
		return CMPCounter;
	}

	public void setCMPCounter(int CMPCounter) {
		this.CMPCounter = CMPCounter;
	}

	public Graph getG_source() {
		return G_source;
	}

	public void setG_source(Graph g_source) {
		G_source = g_source;
	}

	public String getWrapper() {
		return wrapper;
	}

	public void setWrapper(String wrapper) {
		this.wrapper = wrapper;
	}

	public List<Pair<String, String>> getSourceAttributes() {
		return sourceAttributes;
	}

	public void setSourceAttributes(List<Pair<String, String>> sourceAttributes) {
		this.sourceAttributes = sourceAttributes;
	}

	public List<Pair<String, String>> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<Pair<String, String>> attributes) {
		this.attributes = attributes;
	}

	public List<Pair<String, String>> getLateralViews() {
		return lateralViews;
	}

	public void setLateralViews(List<Pair<String, String>> lateralViews) {
		this.lateralViews = lateralViews;
	}

	private void reset(){
		init();
		ObjectCounter = 1;
		SeqCounter = 1;
		CMPCounter = 1;
	}


	public Model bootstrapSchema(String dataSourceName, String dataSourceID, String path) throws FileNotFoundException {
		reset();
		setPrefixesID(dataSourceID);
		id = dataSourceID;
		bootstrap(dataSourceName, path).setNsPrefixes(prefixes);
		addMetaData(dataSourceName, dataSourceID, path);
		return G_source.getModel();

	}

	public Model bootstrapSchema(String dataSourceName, String path) throws FileNotFoundException {
		reset();
		bootstrap(dataSourceName, path).setNsPrefixes(prefixes);
		addMetaData(dataSourceName, "", path);
		return G_source.getModel();
	}

	public Model bootstrapSchema(String iri, InputStream fis) throws FileNotFoundException {
		reset();
		JsonValue phi = Json.createReader(fis).readValue();

		Value(phi,iri,iri);
		return G_source.getModel();
	}

	private Model bootstrap(String D, String path) throws FileNotFoundException {
		InputStream fis = new FileInputStream(path);

		JsonValue phi = Json.createReader(fis).readValue();

		Value(phi,D,D);

		String SELECT = attributes.stream().map(p -> {
			if (p.getLeft().equals(p.getRight())) return p.getLeft();
			else if (p.getLeft().contains("ContainerMembershipProperty")) return p.getRight();
			return p.getRight() + " AS " + p.getRight().replace(".","_");
		}).collect(Collectors.joining(","));
		String FROM = D;
		String LATERAL = lateralViews.stream().map(p -> "LATERAL VIEW explode("+p.getLeft()+") AS "+p.getRight()).collect(Collectors.joining("\n"));
		wrapper = "SELECT " + SELECT + " FROM " + D + " " + LATERAL;

		attributes.stream().forEach(p -> {
			if (p.getLeft().equals(p.getRight())) sourceAttributes.add(Pair.of(p.getRight(),p.getRight()));
			else if (p.getLeft().contains("ContainerMembershipProperty")) sourceAttributes.add(Pair.of(p.getLeft(),p.getRight()));
			else sourceAttributes.add(Pair.of(p.getLeft(),p.getRight().replace(".","_")));
		});

//		Stream.concat(sourceAttributes.stream(), lateralViews.stream()) .forEach(p -> {
//			G_source.addLiteral(createIRI(p.getLeft() ), DataSourceVocabulary.ALIAS.val(), p.getRight() );
//		});
		sourceAttributes.forEach(p -> {
			G_source.addLiteral(createIRI(p.getLeft() ), DataSourceVocabulary.ALIAS.val(), p.getRight() );
		});

		return G_source.getModel();
	}

	private String generateArrayAlias(String a) {
		return Arrays.stream(a.split("\\.")).filter(p -> !p.contains("Seq")).collect(Collectors.joining("_"));
	}

	private String removeSeqs(String a) {
		return Arrays.stream(a.split("\\.")).filter(p -> !p.contains("Seq")).collect(Collectors.joining("."));
	}

//	public static void main(String[] args) throws IOException {
//
//		JsonObject JSONFile = Json.createReader(JSONBootstrap.class.getResourceAsStream("/bikes.json")).readObject();
//		Dataset G_source = DatasetFactory.createTxnMem() ;
//		G_source.begin(ReadWrite.WRITE);
//		JSON("/bikes.json",JSONFile, G_source);
//		G_source.getNamedModel("G").write(new FileWriter("src/main/resources/bikes.ttl"), "TTL");
//	}

//	private static void JSON(String D, JsonObject phi,  Dataset G_source) {
//		Value(phi,G_source,D);
//	}
	public void write(String file, String lang){
		G_source.write(file,lang);
	}

	private void Value(JsonValue phi, String P, String implP) {
		if (phi.getValueType() == JsonValue.ValueType.STRING) LiteralString((JsonString)phi, P, implP);
		else if (phi.getValueType() == JsonValue.ValueType.NUMBER) LiteralNumber((JsonNumber)phi, P, implP);
		else if (phi.getValueType() == JsonValue.ValueType.OBJECT) Object((JsonObject)phi, P, implP);
		else if (phi.getValueType() == JsonValue.ValueType.ARRAY) Array((JsonArray)phi, P, "Object", implP);
	}

	private void Object (JsonObject phi, String P, String implP) {
		phi.keySet().forEach(k -> {
			JsonValue v = phi.get(k);

//			G_source.add(P+".has_"+k, RDF.type, RDF.Property);
//			G_source.add( P+".has_"+k, RDFS.domain, new ResourceImpl(P) );

//			addTriple(G_source,"G",new ResourceImpl(P+".has_"+k), RDF.type, RDF.Property);
//			addTriple(G_source,"G",new ResourceImpl(P+".has_"+k), RDFS.domain, new ResourceImpl(P));

			String label = "has_" +k;
			String property = P + "." + label;

			if (v.getValueType() == JsonValue.ValueType.STRING) {
				label = k;
				property = P + "." +k;
				LiteralString((JsonString)v, property, implP+"."+k);
			} else if (v.getValueType() == JsonValue.ValueType.NUMBER) {
				label = k;
				property = P + "." +k;
				LiteralNumber((JsonNumber) v, property, implP+"."+k);
			} else if (v.getValueType() == JsonValue.ValueType.OBJECT) {
				String u = k; ObjectCounter++;
				G_source.add(createIRI(property), RDFS.range, createIRI(P+"."+u));
				if(P.contains("Seq") && !P.equals(implP)) {
					// the array contains another object.
					Object((JsonObject) v, P + "." + u, implP + "." + u);
				}else {
					Object((JsonObject) v, P + "." + u, P + "." + u);
				}
			}
			else if (v.getValueType() == JsonValue.ValueType.ARRAY) {
				String labelSeq = "Seq"+ SeqCounter;
				String u = P +"." + labelSeq ; SeqCounter++;
				G_source.add(createIRI(u), RDF.type, RDF.Seq);
				G_source.addLiteral(createIRI(u), RDFS.label,labelSeq );
				G_source.add( createIRI(P + ".has_" + k), RDFS.range, createIRI(u));
				Array((JsonArray) v, u, k, k);
			} else if ( v.getValueType() == JsonValue.ValueType.NULL ) {
				// I added v.getValueType() == JsonValue.ValueType.NULL because of this data https://github.com/cmoa/collection/blob/master/cmoa/00078d13-d7c7-4f1b-bbe2-b9afcc25fcbd.json
				label = k;
				property = P + "." +k;
				LiteralString( property, implP+"."+k);
			}

			G_source.add(createIRI(property), RDF.type, RDF.Property);
			G_source.addLiteral(createIRI(property), RDFS.label, label);
			G_source.add( createIRI(property), RDFS.domain, new ResourceImpl( createIRI(P) ) );
		});
		G_source.add( createIRI(P) , RDF.type, RDFS.Class);
		G_source.addLiteral( createIRI(P) , RDFS.label, P.substring(P.lastIndexOf('.') + 1));
	}

	private void Array (JsonArray phi, String P, String key, String implP) {
		String label = "ContainerMembershipProperty"+CMPCounter;
		String uu = P+"."+label; CMPCounter++;
		String uuIRI = createIRI(uu);
		G_source.add(uuIRI, RDF.type, RDFS.ContainerMembershipProperty);
		G_source.addLiteral(uuIRI, RDFS.label, label);
		G_source.add(uuIRI, RDFS.domain, createIRI(P));
		// TODO: some ds have empty array, check below example images array
//		https://github.com/cmoa/collection/blob/master/cmoa/00078d13-d7c7-4f1b-bbe2-b9afcc25fcbd.json
		if(phi.size() > 0) {
			JsonValue v = phi.get(0);
			if (v.getValueType() == JsonValue.ValueType.STRING || v.getValueType() == JsonValue.ValueType.NUMBER) {
				Value(phi.get(0), uu, generateArrayAlias(P+"."+key));
			} else if (v.getValueType() == JsonValue.ValueType.OBJECT) {
				String u = key; ObjectCounter++;
				G_source.add( uuIRI, RDFS.range, createIRI(P + "." + u) );
				Value(phi.get(0), P + "." + u, generateArrayAlias(P+"."+key));
			}
			lateralViews.add(Pair.of(removeSeqs(P+"."+key),generateArrayAlias(P+"."+key)));
		} else {
			// we assume arrays of strings
			LiteralString(uu, generateArrayAlias(P+"."+key));
			lateralViews.add(Pair.of(removeSeqs(P+"."+key),generateArrayAlias(P+"."+key)));
		}

	}

	private void LiteralString ( String P, String implP) {
		G_source.add(createIRI(P),RDFS.range,XSD.xstring);
		attributes.add(Pair.of(P,implP));
	}

	private void LiteralString (JsonString phi,  String P, String implP) {
		G_source.add(createIRI(P),RDFS.range,XSD.xstring);
		attributes.add(Pair.of(P,implP));
	}

	private void LiteralNumber (JsonNumber phi, String P, String implP) {
		G_source.add( createIRI(P), RDFS.range, XSD.integer);
		attributes.add(Pair.of(P,implP));
	}

//	private static void addTriple(Dataset d, String namedGraph, Resource s, Property p, Resource o){
//		Txn.executeWrite(d, ()-> {
//			Model graph = d.getNamedModel(namedGraph);
//			graph.add(s, p, o);
//		});
//	}

//	private add

	private void addMetaData(String name, String id, String path){
		String ds = DataSourceVocabulary.DataSource.val() +"/" + name;
		if (!id.equals("")){
			ds = DataSourceVocabulary.DataSource.val() +"/" + id;
			G_source.addLiteral( ds , DataSourceVocabulary.HAS_ID.val(), id);
		}
		addBasicMetaData(name, path, ds);
		G_source.addLiteral( ds , DataSourceVocabulary.HAS_FORMAT.val(), Formats.JSON.val());
		G_source.addLiteral( ds , DataSourceVocabulary.HAS_WRAPPER.val(), wrapper);
	}


	public static void main(String[] args) throws IOException {
//		JSONBootstrap j = new JSONBootstrap();
//		String D = "ds2";
//
//		Model M = j.bootstrapSchema(D,"/Users/javierflores/Documents/upc/projects/newODIN/datasources/survey_prueba/selected/tate_artist_picasso-pablo-1767.json");
//
//		Graph G = new Graph();
//		G.setModel(M);
//		G.write("/Users/javierflores/Documents/upc/projects/NextiaDI/source/source_schemas/per.ttl",org.apache.jena.riot.Lang.TTL);
		JSONBootstrap j = new JSONBootstrap();
		String D = "stations";

		Model M = j.bootstrapSchema(D,"src/main/resources/stations.json");

		Graph G = new Graph();
		G.setModel(M);
//		java.nio.file.Path temp = Files.createTempFile("bootstrap",".ttl");
//		System.out.println("Graph written to "+temp);
		G.write("src/main/resources/stations_old.ttl",org.apache.jena.riot.Lang.TTL);

		System.out.println("Attributes");
		System.out.println(j.getAttributes());

		System.out.println("Source attributes");
		System.out.println(j.getSourceAttributes());

		System.out.println("Lateral views");
		System.out.println(j.getLateralViews());


		List<Pair<String,String>> attributes = j.getAttributes();
		List<Pair<String,String>> lateralViews = j.getLateralViews();

		String SELECT = attributes.stream().map(p -> {
			if (p.getLeft().equals(p.getRight())) return p.getLeft();
			else if (p.getLeft().contains("ContainerMembershipProperty")) return p.getRight();
			return p.getRight() + " AS " + p.getRight().replace(".","_");
		}).collect(Collectors.joining(","));
		String FROM = D;
		String LATERAL = lateralViews.stream().map(p -> "LATERAL VIEW explode("+p.getLeft()+") AS "+p.getRight()).collect(Collectors.joining("\n"));

		String impl = "SELECT " + SELECT + " FROM " + D + " " + LATERAL;
		System.out.println(impl);
	}
}

