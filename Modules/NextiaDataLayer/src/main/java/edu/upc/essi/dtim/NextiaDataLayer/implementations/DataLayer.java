package edu.upc.essi.dtim.NextiaDataLayer.implementations;

import edu.upc.essi.dtim.NextiaCore.datasources.dataset.Dataset;
import edu.upc.essi.dtim.NextiaDataLayer.utils.DataLoading;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface DataLayer {

    void uploadToFormattedZone(Dataset d, String tableName) throws SQLException;

    void RemoveFromFormattedZone(String tableName) throws SQLException;

    ResultSet executeQuery(String sql, Dataset[] datasets) throws SQLException;

    void close() throws SQLException;

    DataLoading getDataLoading();
}
