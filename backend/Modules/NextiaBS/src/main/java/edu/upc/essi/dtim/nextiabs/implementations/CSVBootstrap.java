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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
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
	private static final int SAMPLE_SIZE = 100; // Sample fewer rows for performance

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

		Map<String, String> columnTypes = inferColumnTypes(parser);

		parser.getHeaderNames().forEach(h -> {
			String clean = h.replaceAll("\\(.*\\)", "").trim(); //added
			String h2 = reformatName(h);
			G_target.addTriple(createIRI(h2),RDF.type,DataFrame_MM.Data);
			G_target.addTripleLiteral(createIRI(h2), RDFS.label,h2 );
			G_target.addTriple(createIRI(name),DataFrame_MM.hasData,createIRI(h2));
			String dataType = columnTypes.getOrDefault(h, DataFrame_MM.String);
			G_target.addTriple(createIRI(h2), DataFrame_MM.hasDataType, dataType);

			// added source attribute name as metadata
			G_target.addTripleLiteral(createIRI(h2), DataFrame_MM.hasSourceName, h);
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

	/**
	 * Simple type inference - checks first few non-empty values
	 */
	private Map<String, String> inferColumnTypes(CSVParser parser) {
		Map<String, String> columnTypes = new HashMap<>();

		try {
			// Reset parser and sample data
			BufferedReader br = new BufferedReader(new FileReader(path));
			char delimiter = detectDelimiter(path);
			CSVParser sampleParser = CSVParser.parse(br, CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(delimiter));

			Map<String, List<String>> samples = new HashMap<>();
			int rowCount = 0;

			for (CSVRecord record : sampleParser) {
				if (rowCount >= SAMPLE_SIZE) break;

				for (String header : sampleParser.getHeaderNames()) {
					String value = record.get(header);
					if (value != null && !value.trim().isEmpty()) {
						samples.computeIfAbsent(header, k -> new ArrayList<>()).add(value.trim());
					}
				}
				rowCount++;
			}
			sampleParser.close();

			// Analyze each column
			for (String column : samples.keySet()) {
				List<String> values = samples.get(column);
				columnTypes.put(column, inferColumnType(values));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return columnTypes;
	}

	/**
	 * Simple type inference logic
	 */
	private String inferColumnType(List<String> values) {
		if (values.isEmpty()) {
			return DataFrame_MM.String;
		}

		// Take first 10 non-empty values for quick analysis
		List<String> sample = values.stream().limit(10).collect(Collectors.toList());

		// Test for boolean first (most specific)
		if (isBoolean(sample)) {
			return DataFrame_MM.Boolean;
		}

		// Test for integer
		if (isInteger(sample)) {
			return DataFrame_MM.Number;
		}

		// Test for decimal
		if (isDecimal(sample)) {
			return DataFrame_MM.Decimal;
		}

		// Test for date
		if (isDate(sample)) {
			return DataFrame_MM.Date;
		}

		// Default to string
		return DataFrame_MM.String;
	}

	private boolean isBoolean(List<String> values) {
		Set<String> booleanValues = Set.of("true", "false", "1", "0", "yes", "no");
		return values.stream()
				.allMatch(v -> booleanValues.contains(v.toLowerCase()));
	}

	private boolean isInteger(List<String> values) {
		return values.stream().allMatch(v -> {
			try {
				Long.parseLong(v.replaceAll("[,\\s]", ""));
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		});
	}

	private boolean isDecimal(List<String> values) {
		return values.stream().allMatch(v -> {
			try {
				Double.parseDouble(v.replaceAll("[,\\s]", ""));
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		});
	}

	private boolean isDate(List<String> values) {
		// Simple date patterns
		String[] patterns = {"yyyy-MM-dd", "dd/MM/yyyy", "MM/dd/yyyy"};

		for (String pattern : patterns) {
			try {
				java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern(pattern);
				boolean allMatch = values.stream().allMatch(v -> {
					try {
						LocalDate.parse(v, formatter);
						return true;
					} catch (DateTimeParseException e) {
						return false;
					}
				});
				if (allMatch) return true;
			} catch (Exception e) {
				// Continue to next pattern
			}
		}
		return false;
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

