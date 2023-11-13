package edu.upc.essi.dtim.odin.query.pojos;

import lombok.Data;

import java.util.List;

@Data
public class QueryDataSelection {

//    String graphIRI;
    String graphID;
    String graphType;
    List<Classes> classes; // It contains IRIs
    List<Property> properties;

}
