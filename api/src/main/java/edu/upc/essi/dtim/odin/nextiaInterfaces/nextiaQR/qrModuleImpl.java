package edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaQR;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upc.essi.dtim.NextiaCore.datasets.Dataset;
import edu.upc.essi.dtim.NextiaCore.graph.IntegratedGraph;
import edu.upc.essi.dtim.NextiaCore.graph.jena.IntegratedGraphJenaImpl;
import edu.upc.essi.dtim.NextiaDataLayer.dataLayer.DataLayer;
import edu.upc.essi.dtim.NextiaQR.rewriting.IQueryRewriting;
import edu.upc.essi.dtim.NextiaQR.rewriting.QueryRewriting;
import edu.upc.essi.dtim.odin.config.AppConfig;
import edu.upc.essi.dtim.odin.nextiaInterfaces.nextiaDataLayer.DataLayerSingleton;
import edu.upc.essi.dtim.odin.query.pojos.QueryDataSelection;
import edu.upc.essi.dtim.odin.query.pojos.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import static edu.upc.essi.dtim.odin.utils.Utils.generateUUID;
import java.sql.ResultSet;

public class qrModuleImpl implements qrModuleInterface {
    @Autowired
    private static AppConfig appConfig;
    @Override
    public QueryResult makeQuery(IntegratedGraph integratedGraph, List<Dataset> integratedDatasets, QueryDataSelection body) {
        String UUID = generateUUID();
        // -- Query rewriting algorithm --
        // 1) From an integrated graph we automatically compute the data structures that are required for the
        //    rewriting algorithm (i.e., global graph --already computed--, source graphs, and mappings graph --implicit--)
        IQueryRewriting qrAlgorithm = new QueryRewriting(DataLayerSingleton.getInstance(appConfig));
        qrAlgorithm.generateQueryingStructures(integratedGraph,integratedDatasets);

        // This is a mock of the NextiaQR call: create a new dataset in the formatted zone (the dataset should already be created by ODIN)
        // and call the uploadToTemporalExploitationZone with the necessary sql (this is what nextiaQR does). This is
        // done as so to prevent having to handle a result set, and instead directly storing the result of the query.
        DataLayer dl = DataLayerSingleton.getInstance(appConfig);
        dl.execute("CREATE TABLE IF NOT EXISTS for_titanic AS SELECT * FROM read_csv_auto('C:\\Work\\Files\\titanic1.csv')");
        dl.uploadToTemporalExploitationZone("SELECT PassengerId, Survived, Name FROM for_titanic", UUID);
        // End of the mock
        // The result of the query is now im the temporal exploitation zone, ready to be persisted if the user wants.
        // First, however, we sent a sample to the frontend
        ResultSet rs = dl.executeQuery("SELECT * FROM tmp_exp_" + UUID);
        QueryResult res = new QueryResult();
        try {
            ResultSetMetaData metaData = rs.getMetaData();
            List<String> columnNames = new ArrayList<>();
            // Set the columns
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                columnNames.add(metaData.getColumnName(i));
            }
            res.setColumns(columnNames);
            // Set the rows
            List<String> adaptedRows = new ArrayList<>();
            int count = 0;
            while (rs.next() && count < 20) {
                Map<String, String> adaptedRow = new HashMap<>();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    adaptedRow.put(res.getColumns().get(i - 1), rs.getString(i));
                }
                String rowJson = convertMapToJsonString(adaptedRow);
                adaptedRows.add(rowJson);
                ++count;
            }
            res.setRows(adaptedRows);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        res.setDataProductUUID(UUID);
        return res;
    }

    private String convertMapToJsonString(Map<String, String> map) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
