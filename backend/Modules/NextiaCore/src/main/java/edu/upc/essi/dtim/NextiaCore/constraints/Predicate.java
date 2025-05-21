package edu.upc.essi.dtim.NextiaCore.constraints;

import java.util.Objects;

public class Predicate {

    private String leftAttribute;
    private String operator;
    private String rightAttribute;

    public Predicate(String leftAttribute, String operator, String rightAttribute) {
        this.leftAttribute = leftAttribute;
        this.operator = operator;
        this.rightAttribute = rightAttribute;
    }

    public String getLeftAttribute() {
        return leftAttribute;
    }

    public void setLeftAttribute(String leftAttribute) {
        this.leftAttribute = leftAttribute;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getRightAttribute() {
        return rightAttribute;
    }

    public void setRightAttribute(String rightAttribute) {
        this.rightAttribute = rightAttribute;
    }

    @Override
    public String toString() {
        return leftAttribute + " " + operator + " " + rightAttribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Predicate predicate = (Predicate) o;
        return Objects.equals(leftAttribute, predicate.leftAttribute) &&
                Objects.equals(operator, predicate.operator) &&
                Objects.equals(rightAttribute, predicate.rightAttribute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftAttribute, operator, rightAttribute);
    }
}
