package edu.cmu.cs.lti.semreranking;

public class DataInstance {

    public int numParses;

    public String[] tokens;
    public String[] posTags;
    public int size;

    public DataInstance(int numParses, String[] tokens, String[] posTags) {
        this.numParses = numParses;
        this.tokens = tokens;
        this.posTags = posTags;
        this.size = tokens.length;
    }

}
