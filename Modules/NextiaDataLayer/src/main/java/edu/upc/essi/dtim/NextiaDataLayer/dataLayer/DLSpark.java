package edu.upc.essi.dtim.NextiaDataLayer.dataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.dataCollectors.DataCollector;
import edu.upc.essi.dtim.NextiaDataLayer.utils.ResultSetSpark;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Comparator;

public class DLSpark extends DataLayer {
    SparkConf conf = new SparkConf().setAppName("Spark").setMaster("local");
    JavaSparkContext sc = new JavaSparkContext(conf);
    SparkSession spark = SparkSession.builder().config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension").config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog").appName("Spark").getOrCreate();
    DataCollector dl;
    public DLSpark(String dataStorePath) throws SQLException, IOException, ClassNotFoundException {
        super(dataStorePath);
//        this.dl = new DataCollector(dataStorePath);
    }

    @Override
    public void uploadToFormattedZone(Dataset d, String tableName) throws SQLException {
        String path = dataStorePath + "\\landingZone\\" + d.getUUID();

        org.apache.spark.sql.Dataset<Row> df = spark.read().parquet(path);
        df.write().format("delta").save(dataStorePath + "\\DeltaLake\\formattedZone\\" + tableName);
    }

    @Override
    public void removeFromFormattedZone(String tableName) throws SQLException {
        Path dir = Paths.get(dataStorePath + "\\DeltaLake\\formattedZone\\" + tableName);
        try {
            Files.walk(dir).sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    System.out.println("Deleting: " + path);
                    Files.delete(path);  //delete each file or directory
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public ResultSet executeQuery(String sql, Dataset[] datasets) throws SQLException {
        return new ResultSetSpark(spark.sql(sql));
    }


    @Override
    public void close() throws SQLException {
        sc.close();
        spark.close();

//        Path dir = Paths.get(dataStorePath + "\\tmp");
//        try {
//            Files.walk(dir).sorted(Comparator.reverseOrder()).forEach(path -> {
//                try {
//                    System.out.println("Deleting: " + path);
//                    Files.delete(path);  //delete each file or directory
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            });
//            Files.delete(dir);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }
}
