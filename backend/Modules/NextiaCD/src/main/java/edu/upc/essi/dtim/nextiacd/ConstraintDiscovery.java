package edu.upc.essi.dtim.nextiacd;

import edu.upc.essi.dtim.nextiacd.parser.DCParser;
import edu.upc.essi.dtim.NextiaCore.constraints.DenialConstraint;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;
import edu.upc.essi.dtim.nextiacd.service.DQServiceClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;


import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import java.util.List;

import java.sql.ResultSet;
import java.io.BufferedWriter;
import java.sql.ResultSetMetaData;
import java.util.StringJoiner;


public class ConstraintDiscovery implements IConstraintDiscovery {

    private final DataLayer dataLayer;
    private final DQServiceClient dqClient;

    public ConstraintDiscovery(DataLayer dataLayer, String dqServiceUrl) {
        this.dataLayer = dataLayer;
        this.dqClient = new DQServiceClient(dqServiceUrl);
    }

    public List<DenialConstraint> getDCs(Dataset dataset) throws IOException {

        try {
            // 1. Query full dataset
            String tableName = "for_" + dataset.getUUID();
            String query = "SELECT * FROM " + tableName;
            ResultSet rs = dataLayer.executeQuery(query, new Dataset[]{dataset});

            // 2. Write to temporary CSV
            Path tempFile = Files.createTempFile("dataset-", ".csv");
            try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                // Write header
                StringJoiner header = new StringJoiner(",");
                for (int i = 1; i <= columnCount; i++) {
                    header.add(meta.getColumnName(i));
                }
                writer.write(header.toString());
                writer.newLine();

                // Write rows
                while (rs.next()) {
                    StringJoiner row = new StringJoiner(",");
                    for (int i = 1; i <= columnCount; i++) {
                        row.add(rs.getString(i) != null ? rs.getString(i) : "");
                    }
                    writer.write(row.toString());
                    writer.newLine();
                }
            }

            // 3. Send to DQ rule discovery service
            File csvFile = tempFile.toFile();
            String response = dqClient.sendCsvFile(csvFile);

            // 4. Cleanup
            csvFile.delete();

            // 5. Parse JSON to extract the array
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            JsonNode dcArray = root.get("denial_constraints");

            StringBuilder rawDCs = new StringBuilder();
            for (JsonNode node : dcArray) {
                rawDCs.append(node.asText()).append("\n");
            }

            // 6. parse response
            return DCParser.parse(rawDCs.toString());

        } catch (Exception e) {
            throw new RuntimeException("Failed to run constraint discovery", e);
        }
    }

    public String getIdentifier(Dataset dataset) {
        try {
            File parquetFile = dataLayer.getFileFromLandingZone(dataset);
            String response = dqClient.getIdentifier(parquetFile);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to get identifier", e);
        }
    }
}
