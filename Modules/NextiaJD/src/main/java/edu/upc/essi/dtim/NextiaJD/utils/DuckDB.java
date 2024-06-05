package edu.upc.essi.dtim.NextiaJD.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DuckDB {

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.duckdb.DuckDBDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("DuckDB driver not found");
        }
        return DriverManager.getConnection("jdbc:duckdb:");
//        return DriverManager.getConnection("jdbc:duckdb:" + "C:\\Projects\\database");
    }
}
