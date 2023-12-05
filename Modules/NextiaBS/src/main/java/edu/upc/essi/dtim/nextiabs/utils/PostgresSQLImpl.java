package edu.upc.essi.dtim.nextiabs.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostgresSQLImpl implements IDatabaseSystem {
    Connection connection;
    String hostname, port, username, password, tableName, databasename;

    @Override
    public void connect(String hostname, String port, String username, String password, String databasename) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.password = password;
        this.databasename= databasename;

        String connectionUrl = "jdbc:postgresql://"+hostname+":"+port+"/"+databasename;

        try {
            connection = DriverManager.getConnection(connectionUrl, username, password); //postgres
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HashMap<String, SQLTableData> getMetamodel() {
        HashMap<String, SQLTableData> resultMetamodel = new HashMap<>();

        List<String> tables = new ArrayList<>();

        try {
            Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs;

            tables.add(tableName);

            for (String t: tables) {

                rs = stmt.executeQuery("SELECT *\n" +
                        "  FROM information_schema.columns\n" +
                        "   WHERE table_name   = '"+t+"';"
                );

                while(rs.next()) {
                    String columnNamei = rs.getString("column_name");
                    String dataTypei = rs.getString("data_type");
                    resultMetamodel.get(t).addColumn(columnNamei, dataTypei);
                }
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
            ResultSet rs;

            //?llista de columnes de la taula
            rs = stmt.executeQuery("SELECT *\n" +
                    "  FROM information_schema.columns\n" +
                    "   WHERE table_name   = '"+tableName+"';"
            );

            while(rs.next()) {
                String columnNamei = rs.getString("column_name");
                String dataTypei = rs.getString("data_type");
                result.addColumn(columnNamei, dataTypei);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}
