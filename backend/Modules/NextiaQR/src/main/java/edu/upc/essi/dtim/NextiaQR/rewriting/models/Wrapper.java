package edu.upc.essi.dtim.NextiaQR.rewriting.models;

/**
 * Represents a data source wrapper in the ODIN query rewriting algorithm.
 * A wrapper encapsulates a data source and provides access to its data.
 */
public class Wrapper {
    private String wrapper;

    public Wrapper(String wrapper) {
        this.wrapper = wrapper;
    }

    public String getWrapper() {
        return wrapper;
    }

    public void setWrapper(String wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public String toString() {
        return "Wrapper{" +
                "wrapper='" + wrapper + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wrapper wrapper1 = (Wrapper) o;
        return wrapper.equals(wrapper1.wrapper);
    }

    @Override
    public int hashCode() {
        return wrapper.hashCode();
    }
}