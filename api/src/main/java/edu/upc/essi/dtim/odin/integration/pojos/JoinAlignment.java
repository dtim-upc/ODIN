package edu.upc.essi.dtim.odin.integration.pojos;

import edu.upc.essi.dtim.NextiaCore.discovery.Alignment;
/**
 * Represents a custom alignment class with additional information for joining two domains.
 */
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

    public String getDomainA() {
        return domainA;
    }

    public void setDomainA(String domainA) {
        this.domainA = domainA;
    }

    public String getDomainLabelA() {
        return domainLabelA;
    }

    public void setDomainLabelA(String domainLabelA) {
        this.domainLabelA = domainLabelA;
    }

    public String getDomainB() {
        return domainB;
    }

    public void setDomainB(String domainB) {
        this.domainB = domainB;
    }

    public String getDomainLabelB() {
        return domainLabelB;
    }

    public void setDomainLabelB(String domainLabelB) {
        this.domainLabelB = domainLabelB;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public Boolean getRightArrow() {
        return rightArrow;
    }

    public void setRightArrow(Boolean rightArrow) {
        this.rightArrow = rightArrow;
    }
}
