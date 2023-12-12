package edu.upc.essi.dtim.NextiaDataLayer.dataLayer;

import edu.upc.essi.dtim.NextiaCore.datasources.dataRepository.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.dataCollectors.DataCollector;
import edu.upc.essi.dtim.NextiaDataLayer.dataCollectors.DataCollectorAPI;
import edu.upc.essi.dtim.NextiaDataLayer.dataCollectors.DataCollectorSQL;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class DLDuckDB extends DataLayer {
    Connection conn;
    Statement stmt;

    public DLDuckDB(String dataStorePath) throws SQLException, ClassNotFoundException, IOException {
        super(dataStorePath);
        this.conn = getConnection();
        this.stmt = conn.createStatement();
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException, IOException {
        Class.forName("org.duckdb.DuckDBDriver");
        // Create directory if it does not exist
        Files.createDirectories(Paths.get(dataStorePath + "\\DuckDBDataLake"));
        return DriverManager.getConnection("jdbc:duckdb:" + dataStorePath + "\\DuckDBDataLake\\database");
    }

    @Override
    public void uploadToFormattedZone(Dataset d, String tableName) throws SQLException {
        String parquetPath = dataStorePath + "landingZone\\" + d.getUUID();
        File directoryPath = new File(parquetPath);
        String fileName = getParquetFile(directoryPath);
        stmt.execute("CREATE TABLE " + tableName + " AS SELECT * FROM read_parquet('" + directoryPath + "\\" +  fileName + "')");
    }

    public void uploadToTemporalFormattedZone(Dataset d, String tableName) throws SQLException {
        String parquetPath = dataStorePath + "\\tmp\\" + d.getUUID();
        File directoryPath = new File(parquetPath);
        String fileName = getParquetFile(directoryPath);
        stmt.execute("CREATE TEMP TABLE " + tableName + " AS SELECT * FROM read_parquet('" + directoryPath + "\\" +  fileName + "')");
    }

    @Override
    public void removeFromFormattedZone(String tableName) throws SQLException {
        stmt.execute("DROP TABLE " + tableName);
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
                    DataCollector dc = getDataCollector(repo);
                    dc.uploadDataToTemporalFolder(dataset);
                    // Data is now into /tmp in whichever format is needed (e.g. api calls are stored as json files)
                    uploadToTemporalLandingZone(dataset);
                    // Data is now into /tmp, under a folder and with parquet format
                    uploadToTemporalFormattedZone(dataset, dataset.getUUID()); // create temporal table in db
                }
            }
        }
    }

    @NotNull
    private DataCollector getDataCollector(DataRepository repo) {
        DataCollector dc;
        if (repo.getRepositoryType().equals("ApiRepository")) {
            dc = new DataCollectorAPI(dataStorePath);
        }
        else if (repo.getRepositoryType().equals("RelationalJDBCRepository")) {
            dc = new DataCollectorSQL(dataStorePath);
        }
        else {
            throw new IllegalArgumentException("Unsupported data collector");
        }
        return dc;
    }

    @Override
    public void close() throws SQLException {
        stmt.close();
        conn.close();
        // Remove all the files in the temporal zone (/tmp)
        deleteFilesFromDirectory(dataStorePath + "\\tmp");
    }
}
