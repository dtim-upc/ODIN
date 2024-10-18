package edu.upc.essi.dtim.nextiadi.bootstraping;


import edu.upc.essi.dtim.nextiadi.bootstraping.metamodels.JSON_MM;
import edu.upc.essi.dtim.nextiadi.bootstraping.utils.JSON_Aux;
import edu.upc.essi.dtim.nextiadi.config.DataSourceVocabulary;
import edu.upc.essi.dtim.nextiadi.config.Formats;
import edu.upc.essi.dtim.nextiadi.jena.Graph;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import javax.json.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates an RDFS-compliant representation of a JSON's document schema
 * @author snadal
 */

public class JSONBootstrapSWJ extends DataSource{

	private int ObjectCounter = 0;
	private int ArrayCounter = 0;

	private Graph sigma;
	//SparkSQL query to get a 1NF view of the file
	private String wrapper;
	//List of pairs, where left is the IRI in the graph and right is the attribute in the wrapper (this will create sameAs edges)
	private List<Pair<String,String>> sourceAttributes;

	private List<Pair<String,String>> attributes;
	private List<Pair<String,String>> lateralViews;

	public JSONBootstrapSWJ(){
		super();
		reset();
	}

	public int getObjectCounter() {
		return ObjectCounter;
	}

	public void setObjectCounter(int objectCounter) {
		ObjectCounter = objectCounter;
	}

	public int getArrayCounter() {
		return ArrayCounter;
	}

	public void setArrayCounter(int arrayCounter) {
		ArrayCounter = arrayCounter;
	}

	public Graph getSigma() {
		return sigma;
	}

	public void setSigma(Graph sigma) {
		this.sigma = sigma;
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
		ObjectCounter = 0;
		ArrayCounter = 0;
	}


	public Model bootstrapSchema(String dataSourceName, String dataSourceID, String path) throws FileNotFoundException {
		reset();
		setPrefixesID(dataSourceID);
		id = dataSourceID;
		Document(path,dataSourceName);
		G_source.getModel().setNsPrefixes(prefixes);
//		G_source.write("/Users/javierflores/Documents/upc/projects/NextiaDI/source/source_schemas/source.ttl"  , Lang.TURTLE);

		productionRules_JSON_to_RDFS();

		String SELECT = attributesSWJ.entrySet().stream().map( p -> {
			if (p.getKey().equals(p.getValue().getKey())) return p.getValue().getPath();
			return  p.getValue().getPath() + " AS " + p.getValue().getLabel();
		}).collect(Collectors.joining(","));

		String FROM = dataSourceName;
		String LATERAL = lateralViews.stream().map(p -> "LATERAL VIEW explode("+p.getLeft()+") AS "+p.getRight()).collect(Collectors.joining("\n"));
		wrapper = "SELECT " + SELECT + " FROM " + dataSourceName + " " + LATERAL;

		addMetaData(dataSourceName, dataSourceID, path);

		return G_target.getModel().setNsPrefixes(prefixes);
	}

	/**
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
//			sigma.addLiteral(createIRI(p.getLeft() ), DataSourceVocabulary.ALIAS.val(), p.getRight() );
//		});
		sourceAttributes.forEach(p -> {
			sigma.addLiteral(createIRI(p.getLeft() ), DataSourceVocabulary.ALIAS.val(), p.getRight() );
		});
**/

	@Deprecated
	private String generateArrayAlias(String a) {
		return Arrays.stream(a.split("\\.")).filter(p -> !p.contains("Seq")).collect(Collectors.joining("_"));
	}

	@Deprecated
	private String removeSeqs(String a) {
		return Arrays.stream(a.split("\\.")).filter(p -> !p.contains("Seq")).collect(Collectors.joining("."));
	}

	private void Document(String path, String D) {
		InputStream fis = null;
		try {
			fis = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		G_source.add(createIRI(D), RDF.type, JSON_MM.Document);

//		try {
			Object(Json.createReader(fis).readValue().asJsonObject(),new JSON_Aux(D,"",""));
//		} catch (ClassCastException e){
//			Array(Json.createReader(fis).readValue().asJsonArray(), new JSON_Aux(D,"",""));
//		}



	}

	private void DataType(JsonValue D, JSON_Aux p) {
		if (D.getValueType() == JsonValue.ValueType.OBJECT) Object((JsonObject)D, p);
		else if (D.getValueType() == JsonValue.ValueType.ARRAY) Array((JsonArray)D, p);
		else Primitive(D,p);
	}

	private void Object (JsonObject D, JSON_Aux p) {
		String u_prime = freshObject();
		String iri_u_prime = createIRI(u_prime);
		G_source.add(iri_u_prime,RDF.type,JSON_MM.Object);
		G_source.addLiteral(iri_u_prime, RDFS.label, p.getKey());
		D.forEach((k,v)-> {
			String k_prime = freshAttribute(k);
			resourcesLabelSWJ.add(k_prime);
			String iri_k = createIRI( k_prime );
			G_source.add(iri_k, RDF.type,JSON_MM.Key);
			if( v.getValueType() == JsonValue.ValueType.OBJECT || v.getValueType() == JsonValue.ValueType.ARRAY)
				G_source.addLiteral(iri_k, RDFS.label, "has "+k_prime);
			else {
				G_source.addLiteral(iri_k, RDFS.label, k_prime);
			}
			G_source.add(iri_u_prime,JSON_MM.hasKey,iri_k);
			String path_tmp = p.getPath() +"."+k;
			if(p.getPath().equals(""))
				path_tmp = k;

			DataType(v, new JSON_Aux(k, k_prime, path_tmp) );
		});
		G_source.add(createIRI(p.getLabel()),JSON_MM.hasValue, iri_u_prime);
	}

	private String replaceLast(String string, String toReplace, String replacement) {
		int pos = string.lastIndexOf(toReplace);
		if (pos > -1) {
			return string.substring(0, pos)
					+ replacement
					+ string.substring(pos + toReplace.length());
		} else {
			return string;
		}
	}

	private void Array (JsonArray D, JSON_Aux p) {
		String u_prime = freshArray();
		String iri_u_prime = createIRI(u_prime);
		G_source.add(iri_u_prime,RDF.type,JSON_MM.Array);
		G_source.addLiteral(iri_u_prime, RDFS.label, p.getKey());
		if (D.size() > 0) {
			DataType(D.get(0), new JSON_Aux(  u_prime, p.getLabel() ,  replaceLast(p.getPath(),p.getKey(), p.getKey()+"_view"  )  ));
		} else {
			// TODO: some ds have empty array, check below example images array
			G_source.add(createIRI(p.getKey()),JSON_MM.hasValue,JSON_MM.String);
		}
		lateralViews.add(Pair.of(p.getKey(), p.getKey()+"_view"));
		G_source.add(createIRI(p.getKey()),JSON_MM.hasValue,iri_u_prime);
//		G_source.add(createIRI(u_prime),JSON_MM.hasMember,createIRI(p));
	}

	private void Primitive (JsonValue D, JSON_Aux p) {
		resourcesLabelSWJ.add(p.getLabel());
		if (D.getValueType() == JsonValue.ValueType.NUMBER) {
			G_source.add(createIRI(p.getLabel()),JSON_MM.hasValue,JSON_MM.Number);
			attributesSWJ.put(p.getLabel(),p);
		}
		// Boolean does not exist in the library
		//else if (D.getValueType() == JsonValue.ValueType.BOOLEAN) {
		//			G_source.add(createIRI(p),JSON_MM.hasValue,JSON_MM.Boolean);
		//		}
		else {
			G_source.add(createIRI(p.getLabel()),JSON_MM.hasValue,JSON_MM.String);
			attributesSWJ.put(p.getLabel(),p);
		}
	}

	private void instantiateMetamodel() {
		G_source.add(JSON_MM.Number.getURI(), RDF.type, JSON_MM.Primitive);
		G_source.add(JSON_MM.String.getURI(), RDF.type, JSON_MM.Primitive);
		//G_source.add(JSON_MM.Boolean.getURI(), RDF.type, JSON_MM.Primitive);
	}

	private String freshObject() {
		ObjectCounter = ObjectCounter + 1;
		return "Object_" + ObjectCounter;
	}

	private String freshAttribute( String attribute ) {
		String att = attribute;
		int cont = 2;
		while(attributesSWJ.containsKey(att) || resourcesLabelSWJ.contains(att)) {
			att = attribute + cont;
			cont = cont +1;
		}
		return att;
	}

	private String freshArray() {
		ArrayCounter = ArrayCounter + 1;
		return "Array_" + ArrayCounter;
	}

	public void prueba(){}

	private void addMetaData(String name, String id, String path){
		String ds = DataSourceVocabulary.DataSource.val() +"/" + name;
		if (!id.equals("")){
			ds = DataSourceVocabulary.DataSource.val() +"/" + id;
			G_target.addLiteral( ds , DataSourceVocabulary.HAS_ID.val(), id);
		}
		addBasicMetaData(name, path, ds);
		G_target.addLiteral( ds , DataSourceVocabulary.HAS_FORMAT.val(), Formats.JSON.val());
		G_target.addLiteral( ds , DataSourceVocabulary.HAS_WRAPPER.val(), wrapper);
		//TODO fix for the queries
		//G_source.addLiteral( ds , DataSourceVocabulary.HAS_WRAPPER.val(), wrapper);
	}


	private void productionRules_JSON_to_RDFS() {
		// Rule 1. Instances of J:Object are translated to instances of rdfs:Class .
		G_source.runAQuery("SELECT ?o ?label WHERE { ?o <"+RDF.type+"> <"+JSON_MM.Object+">. ?o <"+RDFS.label+"> ?label }").forEachRemaining(res -> {
			G_target.add(res.getResource("o").getURI(),RDF.type,RDFS.Class);
			G_target.addLiteral(res.getResource("o").getURI(),RDFS.label, res.getLiteral("label") );
			System.out.println("#1 - "+res.getResource("o").getURI()+", "+RDF.type+", "+RDFS.Class);
		});

		// Rule 2. Instances of J:Array are translated to instances of rdfs:Class and rdf:Seq .
//		G_source.runAQuery("SELECT ?a WHERE { ?a <"+RDF.type+"> <"+JSON_MM.Array+"> }").forEachRemaining(res -> {
//			G_target.add(res.getResource("a").getURI(),RDF.type,RDFS.Class); System.out.println("#2 - "+res.getResource("a").getURI()+", "+RDF.type+", "+RDFS.Class);
//			G_target.add(res.getResource("a").getURI(),RDF.type,RDF.Seq); System.out.println("#2 - "+res.getResource("a").getURI()+", "+RDF.type+", "+RDF.Seq);
//		});

		// Rule 2. Instances of J:Key are translated to instances of rdf:Property . Additionally, this requires defining the rdfs:domain
		//of such newly defined instance of rdf:Property .
		G_source.runAQuery("SELECT ?o ?k ?label WHERE { ?o <"+JSON_MM.hasKey+"> ?k. ?k <"+RDFS.label+"> ?label   }").forEachRemaining(res -> {
			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property); System.out.println("#3 - "+res.getResource("k").getURI()+", "+RDF.type+", "+RDF.Property);
			G_target.addLiteral(res.getResource("k").getURI(),RDFS.label, res.getLiteral("label") );
			G_target.add(res.getResource("k").getURI(),RDFS.domain,res.getResource("o").getURI()); System.out.println("#3 - "+res.getResource("k").getURI()+", "+RDFS.domain+", "+res.getResource("o").getURI());
		});

		// Rule 3. Array keys are also ContainerMembershipProperty
		G_source.runAQuery("SELECT ?o ?k WHERE { ?o <"+JSON_MM.hasKey+"> ?k . ?k <"+JSON_MM.hasValue+"> ?a . ?a <"+RDF.type+"> <"+JSON_MM.Array+"> }").forEachRemaining(res -> {
			G_target.add(res.getResource("k").getURI(),RDF.type,RDFS.ContainerMembershipProperty);
		});

		//Rule 4. Range of primitives.
		G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+">+ <"+JSON_MM.String+"> . ?k <"+RDF.type+"> <"+JSON_MM.Key+"> }").forEachRemaining(res -> {
//			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property); System.out.println("#4 - "+res.getResource("k").getURI()+", "+RDF.type+", "+RDF.Property);
			G_target.add(res.getResource("k").getURI(),RDFS.range,XSD.xstring); System.out.println("#4 - "+res.getResource("k").getURI()+", "+RDFS.range+", "+XSD.xstring);
		});
		G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+">+ <"+JSON_MM.Number+"> . ?k <"+RDF.type+"> <"+JSON_MM.Key+"> }").forEachRemaining(res -> {
//			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property); System.out.println("#4 - "+res.getResource("k").getURI()+", "+RDF.type+", "+RDF.Property);
			G_target.add(res.getResource("k").getURI(),RDFS.range,XSD.xint); System.out.println("#4 - "+res.getResource("k").getURI()+", "+RDFS.range+", "+XSD.xint);
		});

		//Rule 5. Range of objects.
		G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+">+ ?v . ?k <"+RDF.type+"> <"+JSON_MM.Key+"> . ?v <"+RDF.type+"> <"+JSON_MM.Object+"> }").forEachRemaining(res -> {
//			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property); System.out.println("#5 - "+res.getResource("k").getURI()+", "+RDF.type+", "+RDF.Property);
			G_target.add(res.getResource("k").getURI(),RDFS.range,res.getResource("v")); System.out.println("#5 - "+res.getResource("k").getURI()+", "+RDFS.range+", "+res.getResource("v"));
		});

		//6- Range of arrays (objects)
//		G_source.runAQuery("SELECT ?k ?x WHERE { ?k <"+JSON_MM.hasValue+"> ?v . ?v <"+JSON_MM.hasValue+">+ ?x . " +
//				"?k <"+RDF.type+"> <"+JSON_MM.Key+"> . ?v <"+RDF.type+"> <"+JSON_MM.Array+"> . " +
//				"?x <"+RDF.type+"> <"+JSON_MM.Object+">}").forEachRemaining(res -> {
//			G_target.add(res.getResource("k").getURI(),RDFS.range,res.getResource("x")); System.out.println("#6 - "+res.getResource("k").getURI()+", "+RDFS.range+", "+res.getResource("v"));
//		});

/**
		// Array of primitives

		// Array of arrays


		G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+"> ?v . ?v <"+RDF.type+"> <"+JSON_MM.Array+"> . ?k <"+RDF.type+"> <"+JSON_MM.Key+"> }").forEachRemaining(res -> {
//			G_target.add(res.getResource("k").getURI(),RDF.type,RDF.Property); System.out.println("#5 - "+res.getResource("k").getURI()+", "+RDF.type+", "+RDF.Property);
//?			G_target.add(res.getResource("k").getURI(),RDFS.range,res.getResource("v")); System.out.println("#5 - "+res.getResource("k").getURI()+", "+RDFS.range+", "+res.getResource("v"));
		});

		//Arrays of objects


		//Rule 6. Instances of J:Primitive which are members of an instance of J:Array are connected to its corresponding
		//counterpart in the xsd vocabulary using the rdfs:member property. We show the case for instances of J:String whose
		//counterpart is xsd:string . The procedure for instances of J:Number and J:Boolean is similar using their pertaining type.
		G_source.runAQuery("SELECT ?d ?a WHERE { ?a <"+JSON_MM.hasValue+"> <"+JSON_MM.String+"> . ?a <"+RDF.type+"> <"+JSON_MM.Array+"> }").forEachRemaining(res -> {
			//G_target.add(XSD.xstring.getURI(),RDFS.member,res.getResource("a").getURI());
			G_target.add(res.getResource("a").getURI(),RDFS.member,XSD.xstring); System.out.println("#6 - "+res.getResource("a").getURI()+", "+RDFS.member+", "+XSD.xstring);
		});
		//G_source.runAQuery("SELECT ?d ?a WHERE { ?a <"+JSON_MM.hasValue+"> ?d . ?d <"+RDF.type+"> <"+JSON_MM.Number+"> }").forEachRemaining(res -> {
		//	G_target.add(XSD.xint.getURI(),RDFS.member,res.getResource("a").getURI());
		//});

		//Rule 7. Instances of J:Object or J:Array which are members of an instance of J:Array are connected via the rdfs:member
		//property.
		G_source.runAQuery("SELECT ?k ?v WHERE { ?k <"+JSON_MM.hasValue+"> ?a . ?k <"+RDF.type+"> <"+JSON_MM.Key+"> . ?a <"+RDF.type+"> <"+JSON_MM.Array+"> " +
				". ?a <"+JSON_MM.hasValue+"> ?v }").forEachRemaining(res -> {
			G_target.add(res.getResource("k").getURI(),RDFS.member,res.getResource("v")); System.out.println("#7 - "+res.getResource("k").getURI()+", "+RDFS.member+", "+res.getResource("v"));
		});
//		G_source.runAQuery("SELECT ?d ?a WHERE { ?a <"+JSON_MM.hasValue+"> ?d . ?d <"+RDF.type+"> <"+JSON_MM.Array+"> }").forEachRemaining(res -> {
//			G_target.add(res.getResource("a").getURI(),RDFS.member,res.getResource("d")); System.out.println("#7 - "+res.getResource("a").getURI()+", "+RDFS.member+", "+res.getResource("d"));
//		});
**/
	}


	public static void main(String[] args) throws IOException {
		JSONBootstrapSWJ j = new JSONBootstrapSWJ();
		String D = "stations.json";

//		Model M = j.bootstrapSchema("ds1", D,"/Users/javierflores/Documents/upc/projects/newODIN/datasources/survey_prueba/selected/tate_artist_picasso-pablo-1767.json");
		Model M = j.bootstrapSchema("stations", D,"src/main/resources/prueba_presentacion.json");

		Graph G = new Graph();
		G.setModel(M);
		java.nio.file.Path temp = Files.createTempFile("bootstrap",".ttl");
		System.out.println("Graph written to "+temp);
		G.write(temp.toString(), Lang.TURTLE);

		System.out.println("Attributes");
		System.out.println(j.getAttributes());

		System.out.println("Source attributes");
		System.out.println(j.getSourceAttributes());

		System.out.println("Lateral views");
		System.out.println(j.getLateralViews());


		List<Pair<String, String>> attributes = j.getAttributes();
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

//		j.getG_source().write("src/main/resources/stations_source2.ttl", Lang.TURTLE);
//		j.getG_target().write("src/main/resources/stations_target2.ttl", Lang.TURTLE);
	}
}

