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
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.util.stream.Collectors;

import static edu.upc.essi.dtim.nextiabs.utils.DF_MMtoRDFS.productionRulesDataframe_to_RDFS;
import static edu.upc.essi.dtim.nextiabs.utils.Utils.reformatName;

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

		CSVParser parser;
		try {
			char delimiter = detectDelimiter(path); // Detect the delimiter of the file
			BufferedReader br = new BufferedReader(new FileReader(path));
			parser = CSVParser.parse(br, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(delimiter));
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		G_target.addTriple(createIRI(name), RDF.type, DataFrame_MM.DataFrame);
		G_target.addTripleLiteral(createIRI(name), RDFS.label, name);
		parser.getHeaderNames().forEach(h -> {
			String clean = h.replaceAll("\\(.*\\)", "").trim(); //added
			String h2 = reformatName(h);
			G_target.addTriple(createIRI(h2),RDF.type,DataFrame_MM.Data);
			G_target.addTripleLiteral(createIRI(h2), RDFS.label,h2 );
			G_target.addTriple(createIRI(name),DataFrame_MM.hasData,createIRI(h2));
			G_target.addTriple(createIRI(h2),DataFrame_MM.hasDataType,DataFrame_MM.String);
		});
		// changed implementation of the wrapper
		//String select =  parser.getHeaderNames().stream().map(a ->  "\"" + a + "\" AS " + reformatName(a)).collect(Collectors.joining(", "));
		String select = parser.getHeaderNames().stream()
				.map(a -> "`" + a + "` AS " + reformatName(a))
				.collect(Collectors.joining(", "));


		wrapper = "SELECT " + select  + " FROM `" + name + "`";

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

	private char detectDelimiter(String path) throws IOException {
		char[] delimiters = {';', ',', '\t'};
		BufferedReader br = new BufferedReader(new FileReader(path));

		for (char delimiter : delimiters) {
			// Parsing the CSV file with current delimiter
			CSVFormat csvFormat = CSVFormat.DEFAULT.withDelimiter(delimiter);
			CSVParser csvParser = new CSVParser(br, csvFormat);

			Iterable<CSVRecord> records = csvParser.getRecords(); // Get the first record
			if (records.iterator().hasNext()) {
				CSVRecord firstRecord = records.iterator().next();
				// If the record contains more than 1 column, we assume it's the correct delimiter
				if (firstRecord.size() > 1) {
					csvParser.close();
					return delimiter;
				}
			}
			csvParser.close(); // Close the parser
			br = new BufferedReader(new FileReader(path)); // Reset the reader to start from the beginning of the file
		}
		return ','; // Return null if no delimiter is detected
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

