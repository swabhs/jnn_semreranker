package edu.cmu.cs.lti.semreranking;


public class DataInstance {

    public int numParses;

    public String[] tokens;
    public int size;

    public DataInstance(int numParses, String[] tokens) {
        this.numParses = numParses;
        this.tokens = tokens;
        this.size = tokens.length;
    }

}
