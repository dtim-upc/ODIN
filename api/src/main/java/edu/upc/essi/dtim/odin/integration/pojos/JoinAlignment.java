package edu.upc.essi.dtim.odin.integration.pojos;

import edu.upc.essi.dtim.nextiadi.models.Alignment;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents a custom alignment class with additional information for joining two domains.
 */
@Getter
@Setter
public class JoinAlignment extends Alignment {

    /**
     * The domain name of the first dataset (domain A).
     */
    String domainA;

    /**
     * The label for the domain A.
     */
    String domainLabelA;

    /**
     * The domain name of the second dataset (domain B).
     */
    String domainB;

    /**
     * The label for the domain B.
     */
    String domainLabelB;

    /**
     * The relationship between domain A and domain B.
     */
    String relationship;

    /**
     * Represents the direction of the object property. True = A -> B, False = A <- B.
     */
    Boolean rightArrow;

    /**
     * Constructs a new JoinAlignment object with default values.
     */
    public JoinAlignment() {
        this.rightArrow = true;
        this.relationship = "";
    }

    /**
     * Wraps an existing Alignment object and sets default values for additional fields.
     *
     * @param a The Alignment object to wrap.
     */
    public void wrap(Alignment a) {
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
