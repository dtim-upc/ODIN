package edu.upc.essi.dtim.odin.query.pojos;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Property {

    String domain;
    String range;
    String iri;
    Boolean isIntegrated;
    String type;


    public boolean isDataTypeProperty(){

        if(range.contains("http://www.w3.org/2001/XMLSchema#")){
            return true;
        }
        return false;

    }


}
