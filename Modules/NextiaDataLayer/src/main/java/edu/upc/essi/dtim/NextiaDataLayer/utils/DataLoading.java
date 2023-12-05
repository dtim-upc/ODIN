package edu.upc.essi.dtim.NextiaDataLayer.utils;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.ApiRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.RelationalJDBCRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.*;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public class DataLoading {
    SparkConf conf;
    JavaSparkContext sc;
    SparkSession spark;
    String dataStorePath;

    public DataLoading(String dataStorePath) {
        conf = new SparkConf().setAppName("DataLoading").setMaster("local");
        sc = new JavaSparkContext(conf);
        spark = SparkSession.builder().appName("DataLoading").getOrCreate();
        sc.hadoopConfiguration().set("mapreduce.fileoutputcommitter.marksuccessfuljobs", "false");
        sc.hadoopConfiguration().set("parquet.enable.summary-metadata", "false");
        this.dataStorePath = dataStorePath;
    }

    public void uploadToLandingZone(Dataset d) {
        org.apache.spark.sql.Dataset<Row> df_bootstrap = generateBootstrappedDF(d);
        df_bootstrap.repartition(1).write().format("parquet").save(dataStorePath + "\\landingZone\\" + d.getUUID());
    }

    public org.apache.spark.sql.Dataset<Row> generateBootstrappedDF(Dataset d) {
        org.apache.spark.sql.Dataset<Row> df = null;
        if (d instanceof CsvDataset) {
            df = spark.read().option("header", true).csv(((CsvDataset) d).getPath());
        }
        else if (d instanceof JsonDataset) {
            df = spark.read().option("multiline","true").json(((JsonDataset) d).getPath());
        }
        else if (d instanceof APIDataset) {
            df = spark.read().option("multiline","true").json(((APIDataset) d).getJsonPath());
        }
        else if (d.getClass().equals(SQLDataset.class)) {
            SQLDataset sqld = (SQLDataset) d;
            RelationalJDBCRepository repo = (RelationalJDBCRepository) d.getRepository();
            Properties connectionProperties = new Properties();
            connectionProperties.put("user", sqld.getUsername());
            connectionProperties.put("password", sqld.getPassword());
            df = spark.read().jdbc(repo.getUrl(), sqld.getTableName(), connectionProperties);
        }
        // we use the name because the wrapper is expecting the sql table to have the name of the dataset
        assert df != null;
        df.createOrReplaceTempView(d.getDatasetName());
        return spark.sql(d.getWrapper());
    }

    public String reconstructFile(InputStream inputFile, String newFileDirectory) {
        Path diskPath = Path.of(dataStorePath + "\\tmp");
        Path destinationFile = diskPath.resolve(newFileDirectory); // Resolve the destination file path using the disk path and the modified filename
        try {
            Files.createDirectories(destinationFile.getParent()); // Create parent directories if they don't exist
            Files.copy(inputFile, destinationFile, StandardCopyOption.REPLACE_EXISTING); // Copy the input stream  to the destination file
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return destinationFile.toString(); // Return the absolute path of the stored file
    }

    public void loadFromAPI(Dataset d) {
        try {
            ApiRepository repo = (ApiRepository) d.getRepository();
            APIDataset ad = (APIDataset) d;
            // Connection parameters
            URL url = new URL(repo.getUrl() + ad.getEndpoint());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            int status = con.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) { // Success
                // Read the data from the API (json format) and store it in \tmp (json file)
                String jsonPath = reconstructFile(con.getInputStream(), d.getUUID() + ".json");

                // Upload .parquet to \tmp (We transform the json file into a parquet file for easier processing)
                org.apache.spark.sql.Dataset<Row> df;
                df = spark.read().option("header", true).json(jsonPath);
                df.createOrReplaceTempView(d.getUUID());
                spark.sql(d.getWrapper()).write().format("parquet").save(dataStorePath + "\\tmp\\" + d.getUUID()); // apply wrapper

            } else {
                System.out.println("GET request did not work.");
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadFromJDBC(Dataset d) {
        org.apache.spark.sql.Dataset<Row> df_bootstrap = generateBootstrappedDF(d);
        df_bootstrap.repartition(1).write().format("parquet").save(dataStorePath + "\\tmp\\" + d.getUUID());
    }

    public void close() {
        sc.close();
        spark.close();
    }
}
