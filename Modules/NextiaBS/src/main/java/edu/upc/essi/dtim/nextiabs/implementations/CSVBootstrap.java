package edu.upc.essi.dtim.nextiabs.implementations;

import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataSourceVocabulary;
import edu.upc.essi.dtim.NextiaCore.vocabulary.Formats;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDF;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDFS;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataFrame_MM;
import edu.upc.essi.dtim.nextiabs.bootstrap.IBootstrap;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapODIN;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapResult;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

import static edu.upc.essi.dtim.nextiabs.utils.DF_MMtoRDFS.productionRulesDataframe_to_RDFS;

/**
 * Generates an RDFS-compliant representation of a CSV file schema
 * @author snadal
 */
public class CSVBootstrap extends DataSource implements IBootstrap<Graph>, BootstrapODIN {
	// Using DataFrame_MM and without Jena
	public String path;

	public CSVBootstrap(String id, String name, String path) {
		super();
		this.id = id;
		this.name = name;
		this.path = path;
	}

	@Override
	public Graph bootstrapSchema(Boolean generateMetadata) {
		G_target = (LocalGraph) CoreGraphFactory.createGraphInstance("local");
//		setPrefixes();

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		CSVParser parser;
		try {
			parser = CSVParser.parse(br, CSVFormat.DEFAULT.withFirstRecordAsHeader());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		G_target.addTriple(createIRI(name), RDF.type, DataFrame_MM.DataFrame);
		G_target.addTripleLiteral(createIRI(name), RDFS.label, name);
		parser.getHeaderNames().forEach(h -> {
			String h2 = h.replace("\"", "").trim();
			G_target.addTriple(createIRI(h2),RDF.type,DataFrame_MM.Data);
			G_target.addTripleLiteral(createIRI(h2), RDFS.label,h2 );
			G_target.addTriple(createIRI(name),DataFrame_MM.hasData,createIRI(h2));
			G_target.addTriple(createIRI(h2),DataFrame_MM.hasDataType,DataFrame_MM.String);
		});

		String select =  parser.getHeaderNames().stream().map(a ->  a + " AS `" + a.replace(".","_") + "`").collect(Collectors.joining(", "));
		wrapper = "SELECT " + select  + " FROM " + name;

		//TODO: implement metadata
//		if(generateMetadata)
//			generateMetadata();
//		G_target.setPrefixes(prefixes);

		G_target = productionRulesDataframe_to_RDFS(G_target);
		return G_target;
	}

	@Override
	public void generateMetadata(){
		String ds = DataSourceVocabulary.DataSource.getURI() + "/" + name;
		if (!id.isEmpty()) {
			ds = DataSourceVocabulary.DataSource.getURI() + "/" + id;
			G_target.addTripleLiteral(ds, DataSourceVocabulary.HAS_ID.getURI(), id);
		}
		G_target.addTriple(ds, RDF.type,  DataSourceVocabulary.DataSource.getURI());
		G_target.addTripleLiteral(ds, DataSourceVocabulary.HAS_PATH.getURI(), path);
		G_target.addTripleLiteral(ds, RDFS.label, name);

		G_target.addTripleLiteral(ds, DataSourceVocabulary.HAS_FORMAT.getURI(), Formats.CSV.val());
		G_target.addTripleLiteral(ds, DataSourceVocabulary.HAS_WRAPPER.getURI(), wrapper);
	}

	@Override
	public Graph bootstrapSchema() {
		return bootstrapSchema(false);
	}

	@Override
	public BootstrapResult bootstrapDataset(Dataset dataset) {
		bootstrapSchema();
        return new BootstrapResult(this.G_target, this.wrapper);
	}
}

