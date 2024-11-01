package edu.upc.essi.dtim.nextiabs.databaseConnection;

import java.util.HashMap;

public interface IDatabaseSystem {
    void connect(String hostname, String port, String username, String password, String databaseName);

    HashMap<String, SQLTableData> getMetamodel();

    SQLTableData getMetamodelSingleTable(String tableName);
}
