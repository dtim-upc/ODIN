package edu.upc.essi.dtim.odin.query.pojos;

import java.util.List;

public class RDFSResult {
    List<String> columns;
    List<String> rows;

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
}
