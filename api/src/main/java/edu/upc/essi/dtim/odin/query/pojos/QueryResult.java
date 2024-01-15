package edu.upc.essi.dtim.odin.query.pojos;

import java.util.List;

public class QueryResult {
    List<String> columns;
    List<String> rows;
    String CSVPath;

    public List<String> getColumns() {
        return columns;
    }
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getRows() {
        return rows;
    }
    public void setRows(List<String> rows) {
        this.rows = rows;
    }

    public String getCSVPath() { return CSVPath; }
    public void setCSVPath(String CSVPath) { this.CSVPath = CSVPath; }
}
