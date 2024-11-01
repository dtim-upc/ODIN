package edu.upc.essi.dtim.odin.query.pojos;

import java.util.List;

public class QueryResult {
    List<String> columns;
    List<String> rows;
    String dataProductUUID;

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

    public String getDataProductUUID() { return dataProductUUID; }
    public void setDataProductUUID(String dataProductUUID) { this.dataProductUUID = dataProductUUID; }
}
