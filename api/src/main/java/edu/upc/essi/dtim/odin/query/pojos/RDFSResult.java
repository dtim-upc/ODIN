package edu.upc.essi.dtim.odin.query.pojos;

import lombok.Data;
import org.apache.spark.sql.Row;

import java.util.List;

@Data
public class RDFSResult {

    List<String> columns;
    List<String> rows;

}
