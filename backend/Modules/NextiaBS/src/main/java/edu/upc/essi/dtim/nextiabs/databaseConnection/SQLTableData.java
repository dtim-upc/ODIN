package edu.upc.essi.dtim.nextiabs.databaseConnection;

import org.apache.jena.atlas.lib.Pair;
import java.util.ArrayList;
import java.util.List;

public class SQLTableData {
    private String name;
    private List<Pair<String, String>> columns; // List of pairs <column name, column data type>
    private List<Pair<List<String>, String>> references;

    public SQLTableData(String name){
        this.name = name;
        this.columns = new ArrayList<>();
    }

    public String getName() { return name;}
    public void setName(String name) { this.name = name;}

    public List<Pair<String, String>> getColumns(){
        return columns;
    }
    public void setColumns(List<Pair<String, String>> columns){
        this.columns = columns;
    }
}
