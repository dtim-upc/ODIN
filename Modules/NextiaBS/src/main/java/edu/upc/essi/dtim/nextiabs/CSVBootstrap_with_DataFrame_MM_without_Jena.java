package edu.upc.essi.dtim.nextiabs;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.CsvDataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.SQLDataset;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.XmlDataset;
import edu.upc.essi.dtim.NextiaCore.graph.*;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataSourceVocabulary;
import edu.upc.essi.dtim.NextiaCore.vocabulary.Formats;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDF;
import edu.upc.essi.dtim.NextiaCore.vocabulary.RDFS;
import edu.upc.essi.dtim.NextiaCore.vocabulary.DataFrame_MM;
import edu.upc.essi.dtim.nextiabs.utils.BootstrapResult;
import edu.upc.essi.dtim.nextiabs.utils.DF_MMtoRDFS;
import edu.upc.essi.dtim.nextiabs.utils.DataSource;
import edu.upc.essi.dtim.nextiabs.utils.PostgresSQLImpl;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

import edu.upc.essi.dtim.nextiabs.temp.PrintGraph;

/**
 * Generates an RDFS-compliant representation of a CSV file schema
 * @author snadal
 */
public class CSVBootstrap_with_DataFrame_MM_without_Jena extends DataSource implements IBootstrap<Graph>, NextiaBootstrapInterface {

	public String path;

	public CSVBootstrap_with_DataFrame_MM_without_Jena(String id, String name, String path) {
		super();
		this.id = id;
		this.name = name;
		this.path = path;
	}

	public CSVBootstrap_with_DataFrame_MM_without_Jena() {
	}

	@Override
	public Graph bootstrapSchema() throws IOException {
		return bootstrapSchema(false);
	}

	@Override
	public Graph bootstrapSchema(Boolean generateMetadata) throws IOException {
		G_target = CoreGraphFactory.createGraphInstance("local");
		this.id = id;
//		setPrefixes();

		BufferedReader br = new BufferedReader(new FileReader(path));
		CSVParser parser = CSVParser.parse(br, CSVFormat.DEFAULT.withFirstRecordAsHeader());

		G_target.addTriple(createIRI(name), RDF.type, DataFrame_MM.DataFrame);
		G_target.addTripleLiteral(createIRI(name), RDFS.label, name);
		parser.getHeaderNames().forEach(h -> {
			String h2 = h.replace("\"", "").trim();
//			System.out.println(h2);
			G_target.addTriple(createIRI(h2),RDF.type,DataFrame_MM.Data);
			G_target.addTripleLiteral(createIRI(h2), RDFS.label,h2 );
			G_target.addTriple(createIRI(name),DataFrame_MM.hasData,createIRI(h2));
			G_target.addTriple(createIRI(h2),DataFrame_MM.hasDataType,DataFrame_MM.String);

		});

		String select =  parser.getHeaderNames().stream().map(a ->{ return  "`" + a + "` AS `" + a.replace(".","_") + "`"; }).collect(Collectors.joining(","));
		wrapper = "SELECT " + select  + " FROM `" + name + "`";

		//TODO: implement metadata
//		if(generateMetadata)
//			generateMetadata();
//		G_target.setPrefixes(prefixes);

		DF_MMtoRDFS translate = new DF_MMtoRDFS();
		G_target = translate.productionRulesDataframe_to_RDFS(G_target);
		return G_target;
	}

	@Override
	public void generateMetadata(){
		String ds = DataSourceVocabulary.DataSource.getURI() +"/" + name;
		if (!id.equals("")){
			ds = DataSourceVocabulary.DataSource.getURI() +"/" + id;
			G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_ID.getURI(), id);
		}
		G_target.addTriple( ds , RDF.type,  DataSourceVocabulary.DataSource.getURI() );
		G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_PATH.getURI(), path);
		G_target.addTripleLiteral( ds , RDFS.label,  name );

		G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_FORMAT.getURI(), Formats.CSV.val());
		G_target.addTripleLiteral( ds , DataSourceVocabulary.HAS_WRAPPER.getURI(), wrapper);
	}

	public static void main(String[] args) throws IOException {

		String pathcsv = "src/main/resources/artworks.csv";
		CSVBootstrap_with_DataFrame_MM_without_Jena csv = new CSVBootstrap_with_DataFrame_MM_without_Jena("12","artworks", pathcsv);
		Graph m =csv.bootstrapSchema(true);
		PrintGraph.printGraph(m);
	}

	@Override
	public BootstrapResult bootstrap(Dataset dataset) {
		Graph bootstrapG = CoreGraphFactory.createGraphInstance("normal");
		String wrapperG;

		CSVBootstrap_with_DataFrame_MM_without_Jena csv = new CSVBootstrap_with_DataFrame_MM_without_Jena(dataset.getId(), dataset.getDatasetName(), ((CsvDataset) dataset).getPath());
		try {
			bootstrapG = csv.bootstrapSchema();
			wrapperG = csv.wrapper;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

        return new BootstrapResult(bootstrapG, wrapperG);
	}

	@Override
	public Graph bootstrapGraph(Dataset dataset) {
		Graph bootstrapG = CoreGraphFactory.createGraphInstance("normal");
		String wrapperG;

		CSVBootstrap_with_DataFrame_MM_without_Jena csv = new CSVBootstrap_with_DataFrame_MM_without_Jena(dataset.getId(), dataset.getDatasetName(), ((CsvDataset) dataset).getPath());
		try {
			bootstrapG = csv.bootstrapSchema();
			wrapperG = csv.wrapper;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return bootstrapG;
	}
}

