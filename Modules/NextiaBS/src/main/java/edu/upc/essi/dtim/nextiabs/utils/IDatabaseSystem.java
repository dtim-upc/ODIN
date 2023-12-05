package edu.upc.essi.dtim.nextiabs.utils;

import java.util.HashMap;

public interface IDatabaseSystem {
    void connect(String hostname, String port, String username, String password, String databasename);

    HashMap<String, SQLTableData> getMetamodel();

    SQLTableData getMetamodelSingleTable(String tablename);
}
