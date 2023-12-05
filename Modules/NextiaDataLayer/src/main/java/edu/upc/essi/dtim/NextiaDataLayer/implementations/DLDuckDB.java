package edu.upc.essi.dtim.NextiaDataLayer.implementations;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLoading;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.sql.*;
import java.util.Comparator;

public class DLDuckDB implements DataLayer {
    Connection conn;
    Statement stmt;
    String dataStorePath;
    DataLoading dl;

    public DLDuckDB(String dataStorePath) throws SQLException, ClassNotFoundException, IOException {
        dl = new DataLoading(dataStorePath);
        this.dataStorePath = dataStorePath;
        this.conn = getConnection();
        this.stmt = conn.createStatement();
    }

    @Override
    public DataLoading getDataLoading() {
        return this.dl;
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.duckdb.DuckDBDriver");
        // Create directory if it does not exist
        Files.createDirectories(Paths.get(dataStorePath + "\\DuckDBDataLake"));
        return DriverManager.getConnection("jdbc:duckdb:" + dataStorePath + "\\DuckDBDataLake\\database");
    }

    @Override
    public void uploadToFormattedZone(Dataset d, String tableName) throws SQLException {
        String parquetPath = dataStorePath + "\\landingZone\\" + d.getUUID();
        File directoryPath = new File(parquetPath);
        String fileName = getParquetFile(directoryPath);
        stmt.execute("CREATE TABLE " + tableName + " AS SELECT * FROM read_parquet('" + directoryPath + "\\" +  fileName + "')");
    }

    @Override
    public void RemoveFromFormattedZone(String tableName) throws SQLException {
        stmt.execute("DROP TABLE " + tableName);
    }

    public void uploadToTemporalFormattedZone(Dataset d, String tableName) throws SQLException {
        String parquetPath = dataStorePath + "\\tmp\\" + d.getUUID();
        File directoryPath = new File(parquetPath);
        String fileName = getParquetFile(directoryPath);
        stmt.execute("CREATE TEMP TABLE " + tableName + " AS SELECT * FROM read_parquet('" + directoryPath + "\\" +  fileName + "')");
    }

    private String getParquetFile(File directoryPath) {
        String[] contents = directoryPath.list();
        String fileName = "";
        assert contents != null;
        for (String content : contents) {
            if ((content).endsWith("parquet")) {
                fileName = content;
            }
        }
        return fileName;
    }

    @Override
    public ResultSet executeQuery(String sql, Dataset[] datasets) throws SQLException {
        collectVirtualizedTables(datasets);
        return stmt.executeQuery(sql);
    }

    private void collectVirtualizedTables(Dataset[] datasets) throws SQLException {
        // If the dataset is virtualized we have to go fetch the data. Otherwise, we do not need to do anything
        for (Dataset dataset: datasets) {
            DataRepository repo = dataset.getRepository();
            if (repo.getVirtual()) {
                // First, we check if the table has already been virtualized. If that is the case, we don't do anything
                ResultSet rs = stmt.executeQuery("SHOW TABLES");
                boolean tableExists = false;
                while (rs.next()) {
                    if (rs.getString(1).equals(dataset.getUUID())) {
                        tableExists = true;
                    }
                }
                if (!tableExists) {
                    if (repo.getRepositoryType().equals("ApiRepository")) {
                        dl.loadFromAPI(dataset);
                    }
                    else if (repo.getRepositoryType().equals("RelationalJDBCRepository")) {
                        dl.loadFromJDBC(dataset);
                    }
                    // file is now into /tmp in parquet format
                    uploadToTemporalFormattedZone(dataset, dataset.getUUID()); // create temporal table in db
                }
            }
        }
    }

    @Override
    public void close() throws SQLException {
        stmt.close();
        conn.close();

        // Remove all the files in the temporal zone (/tmp)
        Path dir = Paths.get(dataStorePath + "\\tmp");
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

}
