package edu.upc.essi.dtim.NextiaDataLayer.dataLayer;

import edu.upc.essi.dtim.NextiaCore.repositories.DataRepository;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.dataCollectors.DataCollector;
import edu.upc.essi.dtim.NextiaDataLayer.dataCollectors.DataCollectorAPI;
import edu.upc.essi.dtim.NextiaDataLayer.dataCollectors.DataCollectorSQL;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

public class DLDuckDB extends DataLayer {
    Connection conn;
    Statement stmt;

    // ---------------- DuckDB database management

    public DLDuckDB(String dataStorePath) {
        super(dataStorePath);
        this.conn = getConnection();
        try {
            this.stmt = conn.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() {
        try {
            Class.forName("org.duckdb.DuckDBDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        // Create directory if it does not exist
        try {
            Files.createDirectories(Paths.get(dataStorePath + "DuckDBDataLake"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            return DriverManager.getConnection("jdbc:duckdb:" + Paths.get(dataStorePath, "DuckDBDataLake","database").toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Interacting with the zones

    // ---- Formatted

    @Override
    public void uploadToFormattedZone(Dataset d, String tableName) {
        String parquetPath = Paths.get(dataStorePath, "landingZone", d.getUUID()).toString();
        File directoryPath = new File(parquetPath);
        String fileName = getParquetFile(directoryPath);
        try {
            stmt.execute("CREATE TABLE for_" + tableName + " AS SELECT * FROM read_parquet('" + Paths.get(directoryPath.toString(),fileName) + "')");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uploadToTemporalFormattedZone(Dataset d, String tableName) {
        String parquetPath = Paths.get(dataStorePath, "tmp", d.getUUID()).toString();
        File directoryPath = new File(parquetPath);
        String fileName = getParquetFile(directoryPath);
        try {
            stmt.execute("CREATE TEMP TABLE for_" + tableName + " AS SELECT * FROM read_parquet('" + Paths.get(directoryPath.toString(), fileName) + "')");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeFromFormattedZone(String tableName) {
        try {
            stmt.execute("DROP TABLE IF EXISTS for_" + tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

    // ---- Exploitation & Temporal Exploitation

    @Override
    public void persistDataInTemporalExploitation(String UUID) {
        try {
            stmt.execute("CREATE TABLE exp_" + UUID + " AS SELECT * FROM tmp_exp_" + UUID);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void uploadToTemporalExploitationZone(String sql, String UUID) {
        try {
            stmt.execute("CREATE TEMP TABLE tmp_exp_" + UUID + " AS (" + sql + ")");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeFromExploitationZone(String tableName) {
        try {
            stmt.execute("DROP TABLE IF EXISTS exp_" + tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // ---------------- Query execution

    @Override
    public ResultSet executeQuery(String sql, Dataset[] datasets) {
        try {
            collectVirtualizedTables(datasets);
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResultSet executeQuery(String sql) {
        try {
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void execute(String sql) {
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void collectVirtualizedTables(Dataset[] datasets) {
        // If the dataset is virtualized we have to go fetch the data. Otherwise, we do not need to do anything
        for (Dataset dataset: datasets) {
            DataRepository repo = dataset.getRepository();
            if (repo != null && repo.getVirtual()) { // If there is no repository (dataProduct generation), we do not do this
                // First, we check if the table has already been virtualized. If that is the case, we don't do anything
                ResultSet rs;
                boolean tableExists = false;
                try {
                    rs = stmt.executeQuery("SHOW TABLES");
                    while (rs.next()) {
                        // We have to check if the data is the formatted zone
                        // (if it is in the exp zone we don't have to materialize anything)
                        if (rs.getString(1).equals("for_" + dataset.getUUID())) {
                            tableExists = true;
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
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

    // ---------------- Handling files

    // TODO: extend this to different formats and zones
    @Override
    public String materialize(String UUID, String zone, String format) {
        String csvFilePath = Paths.get(dataStorePath, "tmp", UUID + ".csv").toString();
        // As of now, we assume that it is always a csv
        // String extension = "." + format;
        try {
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + zone + "_" + UUID);
            try (FileWriter writer = new FileWriter(csvFilePath)) {
                // Header
                int columnCount = rs.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    writer.append(rs.getMetaData().getColumnName(i));
                    if (i < columnCount) {
                        writer.append(";");
                    }
                }
                writer.append("\n");
                // Value
                while (rs.next()) {
                    for (int i = 1; i <= columnCount; i++) {
                        Object value = rs.getObject(i);
                        writer.append(value != null ? value.toString() : "null");
                        if (i < columnCount) {
                            writer.append(";");
                        }
                    }
                    writer.append("\n");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Paths.get(System.getProperty("user.dir"), csvFilePath).toString(); // Absolute path
    }

    // ---------------- Others

    @Override
    public void close() {
        try {
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        deleteFilesFromDirectory(dataStorePath + "tmp"); // Remove all the files in the temporal zone (/tmp)
    }
}
