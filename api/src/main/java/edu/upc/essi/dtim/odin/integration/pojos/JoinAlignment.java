package edu.upc.essi.dtim.odin.integration.pojos;

import edu.upc.essi.dtim.nextiadi.models.Alignment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinAlignment extends Alignment {

    String domainA;
    String domainLabelA;
    String domainB;
    String domainLabelB;
    String relationship;
    Boolean rightArrow; // represents the direction of the object property. True = A -> B, False = A <- B

    public JoinAlignment(){

        this.rightArrow = true;
        this.relationship = "";
    }

    public void wrap(Alignment a ) {
        this.rightArrow = true;
        this.relationship = "";
        this.iriA = a.getIriA();
        this.iriB = a.getIriB();
        this.labelA = a.getLabelA();
        this.labelB = a.getLabelB();
        this.l = a.getL();
        this.type = a.getType();
    }

}
