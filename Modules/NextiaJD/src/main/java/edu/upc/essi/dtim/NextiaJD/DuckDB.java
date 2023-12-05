package edu.upc.essi.dtim.NextiaJD;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DuckDB {

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("org.duckdb.DuckDBDriver");
        Connection conn = DriverManager.getConnection("jdbc:duckdb:");
        return conn;
    }
}
