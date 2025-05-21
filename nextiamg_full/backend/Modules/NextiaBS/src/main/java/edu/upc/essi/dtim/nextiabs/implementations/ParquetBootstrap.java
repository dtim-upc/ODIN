package edu.upc.essi.dtim.nextiabs.implementations;

import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.graph.LocalGraph;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDF;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDFS;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataFrame_MM;
import edu.upc.essi.dtim.nextiabs.bootstrap.IBootstrap;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapODIN;
import edu.upc.essi.dtim.nextiabs.bootstrap.BootstrapResult;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.MessageType;

import java.io.IOException;

import static edu.upc.essi.dtim.nextiabs.utils.DF_MMtoRDFS.productionRulesDataframe_to_RDFS;


/**
 * Generates an RDFS-compliant representation of a Parquet file schema
 * @author Juane Olivan
 */
public class ParquetBootstrap extends DataSource implements IBootstrap<Graph>, BootstrapODIN {
	// Using DataFrame_MM and without Jena
	public String path;

	public ParquetBootstrap(String id, String name, String path) {
		super();
		this.id = id;
		this.name = name;
		this.path = path;
	}

	@Override
	public Graph bootstrapSchema(Boolean generateMetadata) {
		G_target = (LocalGraph) CoreGraphFactory.createGraphInstance("local");
//		setPrefixes();

		G_target.addTriple(createIRI(name), RDF.type, DataFrame_MM.DataFrame);
		G_target.addTripleLiteral(createIRI(name), RDFS.label, name);

		ParquetMetadata metadata;
		try {
			metadata = ParquetFileReader.readFooter(new Configuration(), new Path(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		MessageType messageType = metadata.getFileMetaData().getSchema();
		System.out.println(messageType.toString());

		for (int i = 0; i < messageType.getFieldCount(); i++) {
			String col = messageType.getFields().get(i).toString().split(" ")[2];
			String type = messageType.getFields().get(i).toString().split(" ")[3];
			System.out.println(col + " " +type);
			G_target.addTriple(createIRI(col),RDF.type,DataFrame_MM.Data);
			G_target.addTripleLiteral(createIRI(col), RDFS.label,col );
			G_target.addTriple(createIRI(name),DataFrame_MM.hasData,createIRI(col));
			G_target.addTriple(createIRI(col),DataFrame_MM.hasDataType, getType(type));
		}

		//TODO: implement wrapper and metadata
//		String select =  parser.getHeaderNames().stream().map(a ->{ return  a +" AS "+ a.replace(".","_"); }).collect(Collectors.joining(","));
//		wrapper = "SELECT " + select  + " FROM " + name;

//		if(generateMetadata)
//			generateMetadata();
//		G_target.setPrefixes(prefixes);

		G_target = productionRulesDataframe_to_RDFS(G_target);
		return G_target;
	}

	private String getType(String type) {
		if (type.contains("INT")) return DataFrame_MM.Number;
		return DataFrame_MM.String;
	}

	@Override
	public void generateMetadata(){
//		String ds = DataSourceVocabulary.DataSource.getURI() +"/" + name;
//		if (!id.equals("")){
//			ds = DataSourceVocabulary.DataSource.getURI() +"/" + id;
//			G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_ID.getURI(), id);
//		}
//		G_target.addTriple( ds , RDF.type,  DataSourceVocabulary.DataSource.getURI() );
//		G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_PATH.getURI(), path);
//		G_target.addTripleLiteral( ds , RDFS.label,  name );
//
//		G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_FORMAT.getURI(), Formats.CSV.val());
//		G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_WRAPPER.getURI(), wrapper);
	}

	@Override
	public Graph bootstrapSchema() {
		return bootstrapSchema(false);
	}

	@Override
	public BootstrapResult bootstrapDataset(Dataset dataset) {
		bootstrapSchema();
		// Shouldn't we operate over G_source??
		return new BootstrapResult(G_target, wrapper);
	}
}

