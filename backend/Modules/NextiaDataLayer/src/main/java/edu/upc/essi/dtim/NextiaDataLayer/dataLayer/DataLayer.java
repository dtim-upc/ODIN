package edu.upc.essi.dtim.NextiaDataLayer.dataLayer;

import edu.upc.essi.dtim.NextiaCore.datasets.*;
import edu.upc.essi.dtim.NextiaCore.repositories.RelationalJDBCRepository;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Properties;

public abstract class DataLayer {
    SparkConf conf;
    JavaSparkContext sc;
    SparkSession spark;
    String dataStorePath;

    public DataLayer(String dataStorePath) {
        conf = new SparkConf().setAppName("DataLoading").setMaster("local");
        sc = new JavaSparkContext(conf);
        spark = SparkSession.builder().appName("DataLoading").getOrCreate();
        sc.hadoopConfiguration().set("mapreduce.fileoutputcommitter.marksuccessfuljobs", "false");
        sc.hadoopConfiguration().set("parquet.enable.summary-metadata", "false");
        this.dataStorePath = dataStorePath;
    }

    // ---------------- Interacting with the zones

    // ---- Landing & Temporal Landing
    public void uploadToLandingZone(Dataset d) {
        org.apache.spark.sql.Dataset<Row> df_bootstrap = generateBootstrappedDF(d);
        df_bootstrap.repartition(1).write().format("parquet").save(Paths.get(dataStorePath, "landingZone", d.getUUID()).toString());
    }

    public void uploadToTemporalLandingZone(Dataset d) {
        org.apache.spark.sql.Dataset<Row> df_bootstrap = generateBootstrappedDF(d);
        df_bootstrap.repartition(1).write().format("parquet").save(Paths.get(dataStorePath, "tmp", d.getUUID()).toString());
    }

    protected org.apache.spark.sql.Dataset<Row> generateBootstrappedDF(Dataset d) {
        org.apache.spark.sql.Dataset<Row> df = null;
        if (d instanceof CSVDataset) {
            df = spark.read().option("header", true).csv(((CSVDataset) d).getPath());
        }
        else if (d instanceof JSONDataset) {
            df = spark.read().option("multiline","true").json(((JSONDataset) d).getPath());
        }
        else if (d instanceof APIDataset) {
            df = spark.read().option("multiline","true").json(((APIDataset) d).getJsonPath());
        }
        else if (d.getClass().equals(SQLDataset.class)) {
            SQLDataset SQLDataset = (SQLDataset) d;
            RelationalJDBCRepository repo = (RelationalJDBCRepository) d.getRepository();
            Properties connectionProperties = new Properties();
            connectionProperties.put("user", SQLDataset.getUsername());
            connectionProperties.put("password", SQLDataset.getPassword());
            df = spark.read().jdbc(repo.getUrl(), SQLDataset.getTableName(), connectionProperties);
        }
        // we use the name because the wrapper is expecting the sql table to have the name of the dataset
        assert df != null;
        df.createOrReplaceTempView("`" + d.getDatasetName() + "`");
        return spark.sql(d.getWrapper());
    }

    // ---- Formatted

    public abstract void uploadToFormattedZone(Dataset d, String tableName) throws SQLException;
    public abstract void uploadToTemporalFormattedZone(Dataset d, String tableName) throws SQLException;
    public abstract void removeFromFormattedZone(String tableName);

    // ---- Exploitation & Temporal Exploitation

    public abstract void uploadToTemporalExploitationZone(String sql, String UUID);
    public abstract void removeFromExploitationZone(String tableName);
    public abstract void persistDataInTemporalExploitation(String UUID);

    // ---------------- Query execution

    public abstract ResultSet executeQuery(String sql, Dataset[] datasets);
    public abstract ResultSet executeQuery(String sql);
    public abstract void execute(String sql);

    // ---------------- Handling files

    public void deleteFilesFromDirectory(String directoryPath) {
        Path dir = Paths.get(directoryPath);
        try {
            Files.walk(dir).sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.delete(path);  //delete each file or directory
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String storeTemporalFile(String path, InputStream inputFile, String newFileDirectory) throws IOException {
        Path diskPath = Path.of(path);
        Path destinationFile = diskPath.resolve(newFileDirectory); // Resolve the destination file path using the disk path and the modified filename

        Files.createDirectories(destinationFile.getParent()); // Create parent directories if they don't exist
        Files.copy(inputFile, destinationFile, StandardCopyOption.REPLACE_EXISTING); // Copy the input stream to the destination file

        return destinationFile.toString(); // Return the absolute path of the stored file
    }

    public String storeTemporalFile(InputStream inputFile, String newFileDirectory) throws IOException {
        return storeTemporalFile(dataStorePath + "tmp", inputFile, newFileDirectory);
    }

    public abstract String materialize(String UUID, String zone, String format);

    // ---------------- Others

    public abstract void close();

    // Only for testing the data that is uploaded
    public void show(Dataset d) {
        String parquetPath = Paths.get(dataStorePath, "landingZone", d.getUUID()).toString();
        org.apache.spark.sql.Dataset<Row> df = spark.read().parquet(parquetPath);
        df.show();
    }

    // Mock call to start Spark
    public void initialize() {
        spark.sql("SHOW TABLES");
    }
}
