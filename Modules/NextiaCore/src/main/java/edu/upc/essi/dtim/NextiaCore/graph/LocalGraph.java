package edu.upc.essi.dtim.NextiaCore.graph;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.upc.essi.dtim.NextiaCore.graph.jena.LocalGraphJenaImpl;

import java.util.List;
import java.util.Map;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LocalGraphJenaImpl.class, name = "LocalGraphJenaImpl"),
})

public interface LocalGraph extends Graph {

    String getGraphicalSchema();
    void setGraphicalSchema(String graphicalSchema);
    public String getLocalGraphAttribute();
    void setLocalGraphAttribute(String localGraphAttribute);

}
