package edu.upc.essi.dtim.nextiabs.databaseConnection;

import org.apache.jena.atlas.lib.Pair;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PostgresSQLImpl implements IDatabaseSystem {
    Connection connection;

    @Override
    public void connect(String hostname, String port, String username, String password, String databaseName) {
        String connectionUrl = "jdbc:postgresql://" + hostname + ":" + port + "/" + databaseName;

        try {
            connection = DriverManager.getConnection(connectionUrl, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HashMap<String, SQLTableData> getMetamodel() {
        HashMap<String, SQLTableData> resultMetamodel = new HashMap<>();

        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // TODO: check if this works
            // Get all tables
            ResultSet rs = stmt.executeQuery("SELECT table_name\n " +
                                                 "FROM information_schema.tables\n" + "';"
            );

            while (rs.next()) { // For each table, get its metamodel and add it to the result
                String tableName = rs.getString(1);
                SQLTableData tableData = getMetamodelSingleTable(tableName);
                resultMetamodel.put(tableName, tableData);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return resultMetamodel;
    }

    @Override
    public SQLTableData getMetamodelSingleTable(String tableName) {
        SQLTableData result = new SQLTableData(tableName);
        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // List of columns of the table
            ResultSet rs = stmt.executeQuery("SELECT *\n " +
                                                 "FROM information_schema.columns\n " +
                                                 "WHERE table_name = '" + tableName + "';"
            );
            List<Pair<String, String>> columnsInfo = new LinkedList<>();
            while (rs.next()) {
                String columnName = rs.getString("column_name");
                String dataType = rs.getString("data_type");
                columnsInfo.add(new Pair<>(columnName, dataType));
            }
            result.setColumns(columnsInfo);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
