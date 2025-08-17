package edu.upc.essi.dtim.NextiaQR.rewriting.models;

/**
 * Represents an equi-join condition in the ODIN query rewriting algorithm.
 * An equi-join condition specifies that two attributes must be equal.
 */
public class EquiJoin {
    private String leftAttribute;
    private String rightAttribute;

    public EquiJoin(String leftAttribute, String rightAttribute) {
        this.leftAttribute = leftAttribute;
        this.rightAttribute = rightAttribute;
    }

    public String getLeftAttribute() {
        return leftAttribute;
    }

    public void setLeftAttribute(String leftAttribute) {
        this.leftAttribute = leftAttribute;
    }

    public String getRightAttribute() {
        return rightAttribute;
    }

    public void setRightAttribute(String rightAttribute) {
        this.rightAttribute = rightAttribute;
    }

    @Override
    public String toString() {
        return "EquiJoin{" +
                "leftAttribute='" + leftAttribute + '\'' +
                ", rightAttribute='" + rightAttribute + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EquiJoin equiJoin = (EquiJoin) o;
        return (leftAttribute.equals(equiJoin.leftAttribute) && rightAttribute.equals(equiJoin.rightAttribute)) ||
               (leftAttribute.equals(equiJoin.rightAttribute) && rightAttribute.equals(equiJoin.leftAttribute));
    }

    @Override
    public int hashCode() {
        // Use commutative hash code to ensure that EquiJoin(a,b) equals EquiJoin(b,a)
        return leftAttribute.hashCode() + rightAttribute.hashCode();
    }
}