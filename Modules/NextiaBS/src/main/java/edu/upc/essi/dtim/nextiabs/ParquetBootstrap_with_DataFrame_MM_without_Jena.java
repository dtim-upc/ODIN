package edu.upc.essi.dtim.nextiabs;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.ParquetDataset;
import edu.upc.essi.dtim.NextiaCore.graph.CoreGraphFactory;
import edu.upc.essi.dtim.NextiaCore.graph.Graph;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDF;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDFS;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataFrame_MM;
import edu.upc.essi.dtim.nextiabs.temp.PrintGraph;
import edu.upc.essi.dtim.nextiabs.utils.BootstrapResult;
import edu.upc.essi.dtim.nextiabs.utils.DF_MMtoRDFS;
import edu.upc.essi.dtim.nextiabs.utils.DataSource;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.schema.MessageType;

import java.io.IOException;


/**
 * Generates an RDFS-compliant representation of a Parquet file schema
 * @author Juane Olivan
 */
public class ParquetBootstrap_with_DataFrame_MM_without_Jena extends DataSource implements IBootstrap<Graph>, NextiaBootstrapInterface {

	public String path;

	public ParquetBootstrap_with_DataFrame_MM_without_Jena(String id, String name, String path) {
		super();
		this.id = id;
		this.name = name;
		this.path = path;
	}

	public ParquetBootstrap_with_DataFrame_MM_without_Jena() {
	}

	@Override
	public Graph bootstrapSchema() throws IOException {
		return bootstrapSchema(false);
	}

	@Override
	public Graph bootstrapSchema(Boolean generateMetadata) throws IOException {
		G_target = CoreGraphFactory.createGraphInstance("local");
//		setPrefixes();

		G_target.addTriple(createIRI(name), RDF.type, DataFrame_MM.DataFrame);
		G_target.addTripleLiteral(createIRI(name), RDFS.label, name);


		@Deprecated
		ParquetMetadata metadata = ParquetFileReader.readFooter(new Configuration(), new Path(path));
		MessageType messageType = metadata.getFileMetaData().getSchema();
		System.out.println(messageType.toString());

		for(int i = 0; i < messageType.getFieldCount(); i++) {
			String col = messageType.getFields().get(i).toString().split(" ")[2];
			String type = messageType.getFields().get(i).toString().split(" ")[3];
			System.out.println(col +" "+type);
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

		DF_MMtoRDFS translate = new DF_MMtoRDFS();
		G_target = translate.productionRulesDataframe_to_RDFS(G_target);
		return G_target;
	}

	private String getType(String type) {
		if(type.contains("INT")) return DataFrame_MM.Number;
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


	public static void main(String[] args) throws IOException {
		String pathcsv = "src/main/resources/artwork.parquet";
		ParquetBootstrap_with_DataFrame_MM_without_Jena csv = new ParquetBootstrap_with_DataFrame_MM_without_Jena("12","artworks", pathcsv);
		Graph m =csv.bootstrapSchema(true);
		PrintGraph.printGraph(m);
	}

	@Override
	public BootstrapResult bootstrap(Dataset dataset) {
		Graph bootstrapG = CoreGraphFactory.createGraphInstance("normal");

		ParquetBootstrap_with_DataFrame_MM_without_Jena parquet = new ParquetBootstrap_with_DataFrame_MM_without_Jena(dataset.getId(), dataset.getDatasetName(), ((ParquetDataset) dataset).getPath());

		return new BootstrapResult(bootstrapG, this.wrapper);
	}

	@Override
	public Graph bootstrapGraph(Dataset dataset) {
		Graph bootstrapG;

		ParquetBootstrap_with_DataFrame_MM_without_Jena parquet = new ParquetBootstrap_with_DataFrame_MM_without_Jena(dataset.getId(), dataset.getDatasetName(), ((ParquetDataset) dataset).getPath());

		try {
			bootstrapG = parquet.bootstrapSchema();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return bootstrapG;
	}
}

